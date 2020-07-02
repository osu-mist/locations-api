import awsAuth from 'aws4';
import config from 'config';
import rp from 'request-promise-native';

import { logger } from 'utils/logger';

const { host, accessKeyId, secretAccessKey } = config.get('dataSources.http');
const httpOptions = { host, path: '/', json: true };

/**
 * Validate http connection and throw an error if invalid
 *
 * @returns {Promise} resolves if http connection can be established and rejects otherwise
 */
const validateHttp = async () => {
  awsAuth.sign(httpOptions, { accessKeyId, secretAccessKey });
  try {
    // using GET for authentication temporarily, HEAD returns 403
    await rp.get({ ...{ uri: `https://${host}` }, ...httpOptions });
  } catch (err) {
    logger.error(err);
    throw new Error('Unable to connect to HTTP data source');
  }
};

export { httpOptions, validateHttp };
