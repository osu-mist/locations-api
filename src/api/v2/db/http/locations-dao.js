import config from 'config';
import rp from 'request-promise-native';

import { serializeLocations, serializeLocation } from 'api/v2/serializers/locations-serializer';

const { sourceUri } = config.get('httpDataSource');
const { endpointUri } = config.get('server');

/**
 * Return a list of pets
 *
 * @returns {Promise} Promise object represents a list of pets
 */
const getLocations = async () => {
  const options = { uri: sourceUri, json: true };
  const rawLocations = await rp(options);
  const serializedLocations = serializeLocations(rawLocations, endpointUri);
  return serializedLocations;
};

/**
 * Return a specific pet by unique ID
 *
 * @param {string} id Unique pet ID
 * @returns {Promise} Promise object represents a specific pet
 */
const getLocationById = async (id) => {
  const options = { uri: `${sourceUri}/${id}`, json: true };
  const rawLocation = await rp(options);
  if (!rawLocation) {
    return undefined;
  }
  const serializedLocation = serializeLocation(rawLocation, endpointUri);
  return serializedLocation;
};

export { getLocations, getLocationById };
