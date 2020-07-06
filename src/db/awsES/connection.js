import AWS from 'aws-sdk';
import elasticsearch from 'elasticsearch';
import connectionClass from 'http-aws-es';
import config from 'config';

import { logger } from 'utils/logger';

const { domain, accessKeyId, secretAccessKey } = config.get('dataSources.awsES');
const esOptions = {
  host: domain,
  log: 'error',
  connectionClass,
  awsConfig: new AWS.Config({
    accessKeyId,
    secretAccessKey,
    region: 'us-east-2',
  }),
};
/**
 * Validate http connection and throw an error if invalid
 *
 * @returns {Promise} resolves if http connection can be established and rejects otherwise
 */
const validateAwsES = async () => {
  try {
    const client = elasticsearch.Client(esOptions);
    await client.ping({ requestTimeout: 3000 });
  } catch (err) {
    logger.error(err);
    throw new Error('Unable to connect to AWS Elasticsearch data source');
  }
};

export { esOptions, validateAwsES };
