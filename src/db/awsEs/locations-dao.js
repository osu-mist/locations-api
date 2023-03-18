import _ from 'lodash';
import { Client } from '@elastic/elasticsearch';
import esb from 'elastic-builder';

import { clientOptions } from 'db/awsEs/connection';
import { parseQuery } from 'utils/parse-query';

/**
 * Parses query parameters and generates an elasticsearch query body
 *
 * @param {object} queryParams Query parameters from GET /locations request
 * @returns {object} Elasticsearch query body
 */
const buildQueryBody = (queryParams) => {
  const parsedParams = parseQuery(queryParams);
  let q = esb.boolQuery();
  if (parsedParams.search !== undefined) {
    q = esb.multiMatchQuery([
      'attributes.name',
      'attributes.arcgisAbbreviation',
      'attributes.bannerAbbreviation',
    ], parsedParams.search);
  }

  if (parsedParams.name !== undefined) {
    if (parsedParams.name.operator === 'fuzzy') {
      q.must(esb.matchQuery('attributes.name', parsedParams.name.value).fuzziness(10));
    } else {
      q.must(esb.termQuery('attributes.name.keyword', parsedParams.name));
    }
  }

  if (parsedParams.hasGiRestroom !== undefined) {
    if (parsedParams.hasGiRestroom) {
      q.must(esb.rangeQuery('attributes.girCount').gt(0));
    } else {
      q.must(esb.matchQuery('attributes.girCount', 0));
    }
  }

  if (parsedParams.coordinates !== undefined) {
    const [lat, lon] = parsedParams.coordinates.split(',');
    q.must(
      esb.geoDistanceQuery()
        .field('attributes.geoLocation')
        .distance(`${parsedParams.distance}${parsedParams.distanceUnit}`)
        .geoPoint(esb.geoPoint().lat(lat).lon(lon)),
    );
  }

  const parkingSpaceTypes = [
    'adaParkingSpaceCount',
    'motorcycleParkingSpaceCount',
    'evParkingSpaceCount',
  ];
  _.forEach(parkingSpaceTypes, (parkingSpaceType) => {
    if (parsedParams[parkingSpaceType] && parsedParams[parkingSpaceType].operator === '>=') {
      q.must(esb.rangeQuery(`attributes.${parkingSpaceType}`)
        .gte(parsedParams.adaParkingSpaceCount.value));
    }
  });

  const abbreviations = ['bannerAbbreviation', 'arcGisAbbreviation'];
  _.forEach(abbreviations, (abbreviation) => {
    if (parsedParams[abbreviation] !== undefined) {
      q.must(esb.matchQuery(`attributes.${abbreviation}`, parsedParams[abbreviation]));
    }
  });

  const oneOfQueries = ['parkingZoneGroup', 'type', 'campus'];
  _.forEach(oneOfQueries, (field) => {
    if (parsedParams[field] && parsedParams[field].operator === 'oneOf') {
      q.must(esb.termsQuery(`attributes.${field}`, parsedParams[field].value));
    }
  });

  if (parsedParams.isOpen !== undefined) {
    if (parsedParams.isOpen) {
      const currentDayIndex = new Date().getDay();
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].start`).lte('now'));
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].end`).gte('now'));
    } else {
      q.mustNot(esb.existsQuery('attributes.openHours'));
    }
  }
  return esb.requestBodySearch().query(q).toJSON();
};

const buildIdQueryBody = (queryParams) => {
  const q = esb.boolQuery();
  q.must(esb.matchQuery('id', queryParams.locationId));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Generates a query body object to get all documents that match the given IDs
 *
 * @param {string[]} ids An array of document IDs to search for
 * @param {string} type The IDs' document type. Should be either 'locations' or 'services'
 * @returns {object} Elasticsearch query body
 */
const buildBulkIdQueryBody = (ids, type) => {
  const q = esb.boolQuery();
  q.must(esb.idsQuery(type, ids));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Adds included services to raw Locations data rows
 *
 * @param {object} res Response object from the GET Locations call
 * @returns {object} Returns the GET Locations response with the related services included
 */
const includeServices = async (res) => {
  const client = Client(clientOptions());
  const servicePromises = [];
  _.forEach(res.hits.hits, ({ _source: locationSource }) => {
    const serviceIds = _.map(locationSource.relationships.services.data, 'id');
    const serviceRes = client.search({
      index: 'services',
      body: buildBulkIdQueryBody(serviceIds, 'services'),
    });
    servicePromises.push(serviceRes);
  });
  await Promise.all(servicePromises).then((resolvedServices) => {
    let index = 0;
    _.forEach(res.hits.hits, ({ _source: locationSource }) => {
      locationSource.services = _.map(resolvedServices[index].hits.hits, '_source');
      index += 1;
    });
  });
  return res.hits.hits;
};

/**
 * Return a list of Locations
 * @param {object} req Request object
 * @returns {Promise<object>} Promise object represents a list of locations
 */
const getLocations = async (req) => {
  const { query } = req;
  const client = new Client(clientOptions());
  const res = await client.search({
    index: 'locations',
    body: buildQueryBody(query),
  });
  if (query.include && query.include[0] === 'services') {
    const response = await includeServices(res);
    return response;
  }

  return res.body.hits.hits;
};

/**
 * Return a specific location by unique ID
 *
 * @param {object} req Request object
 * @returns {Promise} Promise object represents a specific location
 */
const getLocationById = async (req) => {
  const { params, query } = req;
  const client = new Client(clientOptions());
  const res = await client.search({
    index: 'locations',
    body: buildIdQueryBody(params),
  });
  if (query.include && query.include[0] === 'services') {
    const response = await includeServices(res);
    return response[0];
  }
  return res.body.hits.hits[0];
};

export { getLocations, getLocationById };
