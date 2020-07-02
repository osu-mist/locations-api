import awsAuth from 'aws4';
import config from 'config';
import esb from 'elastic-builder';
import rp from 'request-promise-native';

import { serializeLocations } from 'serializers/locations-serializer';

const {
  host,
  accessKeyId,
  secretAccessKey,
} = config.get('dataSources.http');

const path = '/locations/_search';
const opts = {
  uri: `https://${host}${path}`,
  host,
  path,
  service: 'es',
  method: 'POST',
};

/**
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of locations
 */
const getLocations = async (queryParams) => {
  const q = esb.boolQuery();
  if (queryParams['filter[name]']) {
    q.must(esb.termQuery('attributes.name', queryParams['filter[name]']));
  }

  opts.json = esb.requestBodySearch().query(q).toJSON();
  awsAuth.sign(opts, { accessKeyId, secretAccessKey });
  console.log(opts);
  const rawLocations = await rp(opts);
  console.log(rawLocations);
  const serializedLocations = serializeLocations(rawLocations, host);
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
