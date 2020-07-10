import _ from 'lodash';
import AWS from 'aws-sdk';
import config from 'config';
import connectionClass from 'http-aws-es';
import esb from 'elastic-builder';
import elasticsearch from 'elasticsearch';

const {
  domain,
  accessKeyId,
  secretAccessKey,
} = config.get('dataSources.awsES');

/**
 * Parses query parameters and generates an elasticsearch query body
 *
 * @param {object} queryParams Query paramaters from GET /locations request
 * @returns {object} Elasticsearch query body
 */
const parseQueryParams = (queryParams) => {
  let q = esb.boolQuery();
  if (queryParams['filter[search]'] !== undefined) {
    q = esb.multiMatchQuery([
      'attributes.name',
      'attributes.arcgisAbbreviation',
      'attributes.bannerAbbreviation',
    ], queryParams['filter[search]']);
  }

  if (queryParams['filter[name]'] !== undefined) {
    q.must(esb.termQuery('attributes.name', queryParams['filter[name]']));
  }

  if (queryParams['filter[name][fuzzy]'] !== undefined) {
    q.must(esb.fuzzyQuery('attributes.name', queryParams['filter[name][fuzzy]']));
  }

  if (queryParams['filter[type]'] !== undefined) {
    q.must(esb.matchQuery('attributes.type', queryParams['filter[type]'][0]));
    // filter[type] is an array for some reason?
  }

  if (queryParams['filter[hasGiRestroom]'] !== undefined) {
    if (queryParams['filter[hasGiRestroom]']) {
      q.must(esb.rangeQuery('attributes.girCount').gt(0));
    } else {
      q.must(esb.matchQuery('attributes.girCount', 0));
    }
  }

  if (queryParams['filter[adaParkingSpaceCount][gte]'] !== undefined) {
    q.must(esb.rangeQuery('attributes.adaParkingSpaceCount')
      .gte(queryParams['filter[adaParkingSpaceCount][gte]']));
  }

  if (queryParams['filter[motoParkingSpaceCount][gte]'] !== undefined) {
    q.must(esb.rangeQuery('attributes.motorcycleParkingSpaceCount')
      .gte(queryParams['filter[motorcycleSpaceCount][gte]']));
  }

  if (queryParams['filter[evParkingSpaceCount][gte]'] !== undefined) {
    q.must(esb.rangeQuery('attributes.evParkingSpaceCount')
      .gte(queryParams['filter[evSpaceCount][gte]']));
  }

  if (queryParams['filter[bannerAbbreviation]'] !== undefined) {
    q.must(esb.matchQuery('attributes.bannerAbbreviation',
      queryParams['filter[bannerAbbreviation]']));
  }

  if (queryParams['filter[arcGisAbbreviation]'] !== undefined) {
    q.must(esb.matchQuery('attributes.arcgisAbbreviation',
      queryParams['filter[arcGisAbbreviation]']));
  }
  /*
  if (queryParams['filter[isOpen]'] !== undefined) {

  }
  if (queryParams['filter[campus][oneOf]'] !== undefined) {

  }
  if (queryParams['filter[parkingZoneGroup][oneOf]'] !== undefined) {

  }
  if (queryParams['filter[distance]'] !== undefined) {

  }
  if (queryParams['filter[coordinates]'] !== undefined) {

  }
  if (queryParams['include'] !== undefined) {

  }
  */
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of locations
 */
// GET /locations only returns 10 objects?
const getLocations = async (queryParams) => {
  const client = elasticsearch.Client({
    host: domain,
    log: 'error',
    connectionClass,
    awsConfig: new AWS.Config({
      accessKeyId,
      secretAccessKey,
      region: 'us-east-2',
    }),
  });

  const bodyQuery = parseQueryParams(queryParams);
  console.log(bodyQuery.query);
  const res = await client.search({
    index: 'locations',
    body: bodyQuery,
  });

  const locations = [];
  _.forEach(res.hits.hits, (location) => {
    // eslint-disable-next-line dot-notation
    const locationSource = location['_source'];
    const locationAttributes = locationSource.attributes;
    locationSource.attributes.abbreviations = {
      arcGis: locationAttributes.arcgisAbbreviation,
      banner: locationAttributes.bannerAbbreviation,
    };
    locationSource.attributes.giRestrooms = {
      count: locationAttributes.girCount,
      limit: locationAttributes.girLimit,
      locations: (locationAttributes.girLocations)
        ? locationAttributes.girLocations.split(', ')
        : null,
    };
    locationSource.attributes.parkingSpaces = {
      evSpaceCount: locationAttributes.evParkingSpaceCount,
      adaSpaceCount: locationAttributes.adaParkingSpaceCount,
      motorcyclesSpaceCount: locationAttributes.motorcycleParkingSpaceCount,
    };
    locations.push(locationSource);
  });
  return locations;
};

/**
 * Return a specific location by unique ID
 *
 * @param {string} id Unique location ID
 * @returns {Promise} Promise object represents a specific location
 */
const getLocationById = async (id) => id; /* {
  const options = { uri: `${sourceUri}/${id}`, json: true };
  const rawLocation = await rp(options);
  if (!rawLocation) {
    return undefined;
  }
  const serializedLocation = serializeLocation(rawLocation, endpointUri);
  return serializedLocation;

}; */

export { getLocations, getLocationById };
