// import config from 'config';
import { Client } from '@elastic/elasticsearch';

import { logger } from 'utils/logger';

// const {
//   domain,
//   region,
//   accessKeyId,
//   secretAccessKey,
// } = config.get('dataSources.awsEs');

/**
 * Returns options for elasticsearch client
 *
 * @returns {object} elasticsearch client options
 */
// const clientOptions = () => ({
//   host: domain,
//   log: 'error',
//   connectionClass,
//   awsConfig: new aws.Config({
//     accessKeyId,
//     secretAccessKey,
//     region,
//   }),
// });

const clientOptions = () => ({
  node: 'http://localhost:9201',
  maxRetries: 5,
  sniffOnStart: true,
});

/**
 * Validate AWS elasticsearch connection and throw an error if invalid
 *
 * @returns {Promise<object>} resolves if AWS connection can be established and rejects otherwise
 */
const validateAwsEs = async () => {
  try {
    const client = new Client(clientOptions());
    await client.ping({ requestTimeout: 3000 });
  } catch (err) {
    logger.error(err);
    throw new Error('Unable to connect to AWS Elasticsearch data source.');
  }
};

export { validateAwsEs, clientOptions };
