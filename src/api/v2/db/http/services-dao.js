import config from 'config';
import rp from 'request-promise-native';

import { serializeService, serializeServices } from 'api/v2/serializers/services-serializer';

const { sourceUri } = config.get('httpDataSource');
const { endpointUri } = config.get('server');

/**
 * Return a list of pets
 * @param query Object containing query parameters
 * @returns {Promise} Promise object represents a list of pets
 */
const getServices = async (query) => {
  const options = { uri: sourceUri, json: true };
  const rawServices = await rp(options);
  const serializedServices = serializeServices(rawServices, endpointUri);
  return serializedServices;
};

/**
 * Return a specific pet by unique ID
 *
 * @param {string} id Unique pet ID
 * @returns {Promise} Promise object represents a specific pet
 */
const getServiceById = async (id) => {
  const options = { uri: `${sourceUri}/${id}`, json: true };
  const rawService = await rp(options);
  if (!rawService) {
    return undefined;
  }
  const serializedService = serializeService(rawService, endpointUri);
  return serializedService;
};

export { getServices, getServiceById };
