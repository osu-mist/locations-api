import { Client } from 'elasticsearch';
import esb from 'elastic-builder';
import _ from 'lodash';

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
      q.must(esb.fuzzyQuery('attributes.name', parsedParams.name.value));
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
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise<object>} Promise object represents a list of locations
 */
const getLocations = async (queryParams) => {
  const client = Client(clientOptions());
  const res = await client.search({
    index: 'locations',
    body: buildQueryBody(queryParams),
  });
  return res.hits.hits;
};

/**
 * Return a specific location by unique ID
 *
 * @param {string} queryParams Query parameters from GET /locations/{locationId} request
 * @returns {Promise} Promise object represents a specific location
 */
const getLocationById = async (queryParams) => {
  const client = Client(clientOptions());
  const res = await client.search({
    index: 'locations',
    body: buildIdQueryBody(queryParams),
  });
  return res.hits.hits[0];
};

export { getLocations, getLocationById };
