import { Client } from 'elasticsearch';
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

  if (parsedParams.type !== undefined) {
    qu.must(esb.matchQuery('attributes.type', parsedParams.type[0]));
  }

  if (parsedParams.hasGiRestroom !== undefined) {
    if (parsedParams.hasGiRestroom) {
      qu.must(esb.rangeQuery('attributes.girCount').gt(0));
    } else {
      qu.must(esb.matchQuery('attributes.girCount', 0));
    }
  }

  if (parsedParams.adaParkingSpaceCount && parsedParams.adaParkingSpaceCount.operator === '>=') {
    qu.must(esb.rangeQuery('attributes.adaParkingSpaceCount')
      .gte(parsedParams.adaParkingSpaceCount.value));
  }

  if (parsedParams.motorcycleParkingSpaceCount
      && parsedParams.motorcycleParkingSpaceCount.operator === '>=') {
    qu.must(esb.rangeQuery('attributes.motorcycleParkingSpaceCount')
      .gte(parsedParams.motorcycleParkingSpaceCount.value));
  }

  if (parsedParams.evParkingSpaceCount && parsedParams.evParkingSpaceCount.operator === '>=') {
    qu.must(esb.rangeQuery('attributes.evParkingSpaceCount')
      .gte(parsedParams.evParkingSpaceCount.value));
  }

  if (parsedParams.bannerAbbreviation !== undefined) {
    qu.must(esb.matchQuery('attributes.bannerAbbreviation', parsedParams.bannerAbbreviation));
  }

  if (parsedParams.arcGisAbbreviation !== undefined) {
    qu.must(esb.matchQuery('attributes.arcgisAbbreviation', parsedParams.arcGisAbbreviation));
  }

  if (parsedParams.isOpen !== undefined) {
    if (parsedParams.isOpen) {
      const currentDayIndex = new Date().getDay();
      qu.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].start`).lte('now'));
      qu.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].end`).gte('now'));
    } else {
      qu.mustNot(esb.existsQuery('attributes.openHours'));
    }
  }

  if (parsedParams.campus && parsedParams.campus.operator === 'oneOf') {
    qu.must(esb.termsQuery('attributes.campus', parsedParams.campus.value));
  }

  if (parsedParams.parkingZoneGroup && parsedParams.parkingZoneGroup.operator === 'oneOf') {
    qu.must(esb.termsQuery('attributes.parkingZoneGroup', parsedParams.parkingZoneGroup.value));
  }

  /*
  geo distance query not working.
  Maybe has to do with the naming of the lat and lon fields in index?

  if (parsedParams.coordinates !== undefined) {
    const latitude = parsedParams.coordinates[0];
    const longitude = parsedParams.coordinates[1];
    const distance = `${parsedParams.distance}${parsedParams.distanceUnit}`;
    console.log(parsedParams.coordinates);
    q.must(esb.geoDistanceQuery()
      .field('attributes.geoLocation')
      .distance(distance)
      .geoPoint(esb.geoPoint().lat(latitude).lon(longitude)));
  }
  */

  /* Will implement after GET /locations
  if (queryParams['include'] !== undefined) {}
  */
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
