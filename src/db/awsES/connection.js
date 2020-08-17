import aws from 'aws-sdk';
import config from 'config';
import elasticsearch from 'elasticsearch';
import connectionClass from 'http-aws-es';

import { logger } from 'utils/logger';

const {
  domain,
  region,
  accessKeyId,
  secretAccessKey,
} = config.get('dataSources.awsEs');

/**
 * Returns options for elasticsearch client
 *
 * @returns {object} elasticsearch client options
 */
const clientOptions = () => ({
  host: domain,
  log: 'error',
  connectionClass,
  awsConfig: new aws.Config({
    accessKeyId,
    secretAccessKey,
    region,
  }),
});

/**
 * Validate AWS elasticsearch connection and throw an error if invalid
 *
 * @returns {Promise<object>} resolves if AWS connection can be established and rejects otherwise
 */
const validateAwsEs = async () => {
  try {
    const client = elasticsearch.Client(clientOptions());
    await client.ping({ requestTimeout: 3000 });
  } catch (err) {
    logger.error(err);
    throw new Error('Unable to connect to AWS Elasticsearch data source.');
  }
};

export { validateAwsEs, clientOptions };
