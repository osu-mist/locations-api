import _ from 'lodash';
import aws from 'aws-sdk';
import config from 'config';
import connectionClass from 'http-aws-es';
import elasticsearch from 'elasticsearch';
import esb from 'elastic-builder';

import { parseQuery } from 'utils/parse-query';

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

  if (parsedParams.type !== undefined) {
    q.must(esb.matchQuery('attributes.type', parsedParams.type[0]));
  }

  if (parsedParams.hasGiRestroom !== undefined) {
    if (parsedParams.hasGiRestroom) {
      q.must(esb.rangeQuery('attributes.girCount').gt(0));
    } else {
      q.must(esb.matchQuery('attributes.girCount', 0));
    }
  }

  if (parsedParams.adaParkingSpaceCount && parsedParams.adaParkingSpaceCount.operator === '>=') {
    q.must(esb.rangeQuery('attributes.adaParkingSpaceCount')
      .gte(parsedParams.adaParkingSpaceCount.value));
  }

  if (parsedParams.motorcycleParkingSpaceCount && parsedParams.motorcycleParkingSpaceCount.operator === '>=') {
    q.must(esb.rangeQuery('attributes.motorcycleParkingSpaceCount')
      .gte(parsedParams.motorcycleParkingSpaceCount.value));
  }

  if (parsedParams.evParkingSpaceCount && parsedParams.evParkingSpaceCount.operator === '>=') {
    q.must(esb.rangeQuery('attributes.evParkingSpaceCount')
      .gte(parsedParams.evParkingSpaceCount.value));
  }

  if (parsedParams.bannerAbbreviation !== undefined) {
    q.must(esb.matchQuery('attributes.bannerAbbreviation', parsedParams.bannerAbbreviation));
  }

  if (parsedParams.arcGisAbbreviation !== undefined) {
    q.must(esb.matchQuery('attributes.arcgisAbbreviation', parsedParams.arcGisAbbreviation));
  }

  // unable to test because nothing is open :(
  if (parsedParams.isOpen !== undefined) {
    if (parsedParams.isOpen) {
      const currentDayIndex = new Date().getDay();
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].start`).lte('now'));
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].end`).gte('now'));
    } else {
      q.mustNot(esb.existsQuery('attributes.openHours'));
    }
  }

  if (parsedParams.campus && parsedParams.campus.operator === 'oneOf') {
    q.must(esb.termsQuery('attributes.campus', parsedParams.campus.value));
  }

  if (parsedParams.parkingZoneGroup && parsedParams.parkingZoneGroup.operator === 'oneOf') {
    q.must(esb.termsQuery('attributes.parkingZoneGroup', parsedParams.parkingZoneGroup.value));
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
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise<object>} Promise object represents a list of locations
 */
// GET /locations only returns 10 objects?
const getLocations = async (queryParams) => {
  const client = elasticsearch.Client({
    host: domain,
    log: 'error',
    connectionClass,
    awsConfig: new aws.Config({
      accessKeyId,
      secretAccessKey,
      region: 'us-east-2',
    }),
  });

  const res = await client.search({
    index: 'locations',
    body: buildQueryBody(queryParams),
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
    if (locationAttributes.geoLocation) {
      locationSource.attributes.coordinates = {
        lat: locationAttributes.geoLocation.latitude,
        long: locationAttributes.geoLocation.longitude,
      };
    } else {
      locationSource.attributes.coordinates = null;
    }
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
