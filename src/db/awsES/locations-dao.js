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
  let qu = esb.boolQuery();
  if (parsedParams.search !== undefined) {
    qu = esb.multiMatchQuery([
      'attributes.name',
      'attributes.arcgisAbbreviation',
      'attributes.bannerAbbreviation',
    ], parsedParams.search);
  }

  if (parsedParams.name !== undefined) {
    if (parsedParams.name.operator === 'fuzzy') {
      qu.must(esb.fuzzyQuery('attributes.name', parsedParams.name.value));
    } else {
      qu.must(esb.termQuery('attributes.name.keyword', parsedParams.name));
    }
  }

  if (parsedParams.hasGiRestroom !== undefined) {
    if (parsedParams.hasGiRestroom) {
      qu.must(esb.rangeQuery('attributes.girCount').gt(0));
    } else {
      qu.must(esb.matchQuery('attributes.girCount', 0));
    }
  }

  const parkingSpaceTypes = [
    'adaParkingSpaceCount',
    'motorcycleParkingSpaceCount',
    'evParkingSpaceCount',
  ];
  _.forEach(parkingSpaceTypes, (parkingSpaceType) => {
    if (parsedParams[parkingSpaceType] && parsedParams[parkingSpaceType].operator === '>=') {
      qu.must(esb.rangeQuery(`attributes.${parkingSpaceType}`)
        .gte(parsedParams.adaParkingSpaceCount.value));
    }
  });

  const abbreviations = ['bannerAbbreviation', 'arcGisAbbreviation'];
  _.forEach(abbreviations, (abbreviation) => {
    if (parsedParams[abbreviation] !== undefined) {
      qu.must(esb.matchQuery(`attributes.${abbreviation}`, parsedParams[abbreviation]));
    }
  });

  const oneOfQueries = ['parkingZoneGroup', 'type', 'campus'];
  _.forEach(oneOfQueries, (field) => {
    if (parsedParams[field] && parsedParams[field].operator === 'oneOf') {
      qu.must(esb.termsQuery(`attributes.${field}`, parsedParams[field].value));
    }
  });

  if (parsedParams.isOpen !== undefined) {
    if (parsedParams.isOpen) {
      const currentDayIndex = new Date().getDay();
      qu.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].start`).lte('now'));
      qu.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].end`).gte('now'));
    } else {
      qu.mustNot(esb.existsQuery('attributes.openHours'));
    }
  }
  return esb.requestBodySearch().query(qu).toJSON();
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
 * @param {string} id Unique location ID
 * @returns {Promise} Promise object represents a specific location
 */
const getLocationById = async (id) => id;

export { getLocations, getLocationById };
