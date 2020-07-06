import config from 'config';
import rp from 'request-promise-native';

import { serializeService, serializeServices } from 'serializers/services-serializer';

const { domain } = config.get('dataSources.awsES');
const { endpointUri } = config.get('server');

/**
 * Return a list of services
 * @param query Object containing query parameters
 * @returns {Promise} Promise object represents a list of services
 */
const getServices = async (query) => {
  const options = { uri: domain, json: true, query };
  const rawServices = await rp(options);
  const serializedServices = serializeServices(rawServices, endpointUri);
  return serializedServices;
};

/**
 * Return a specific service by unique ID
 *
 * @param {string} id Unique service ID
 * @returns {Promise} Promise object represents a specific service
 */
const getServiceById = async (id) => {
  const options = { uri: `${domain}/${id}`, json: true };
  const rawService = await rp(options);
  if (!rawService) {
    return undefined;
  }
  const serializedService = serializeService(rawService, endpointUri);
  return serializedService;
};

export { getServices, getServiceById };
