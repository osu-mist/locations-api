import awsAuth from 'aws4';
import config from 'config';
import esb, { boolQuery } from 'elastic-builder';
import rp from 'request-promise-native';

import { serializeLocations } from 'serializers/locations-serializer';

const {
  domain,
  region,
  accessKeyId,
  secretAccessKey,
} = config.get('dataSources.awsEs');

const opts = {
  host: domain,
  path: '/locations/_search',
  service: 'es',
  region,
  method: 'POST',
};

/**
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of locations
 */
const getLocations = async (queryParams) => {
  const requestBody = esb.requestBodySearch();
  const query = boolQuery();

  if (queryParams['filter[name]']) {
    query.filter('attributes.name', queryParams['filter[name]']);
  }

  opts.body = requestBody;
  awsAuth.sign(opts, { accessKeyId, secretAccessKey });
  const rawLocations = await rp(opts);
  console.log(rawLocations);
  const serializedLocations = serializeLocations(rawLocations /* , endpointUri */);
  return serializedLocations;
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
