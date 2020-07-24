import aws from 'aws-sdk';
import config from 'config';
import connectionClass from 'http-aws-es';
import elasticsearch from 'elasticsearch';

import { logger } from 'utils/logger';

const { domain, accessKeyId, secretAccessKey } = config.get('dataSources.awsES');
/**
 * Validate http connection and throw an error if invalid
 *
 * @returns {Promise<object>} resolves if http connection can be established and rejects otherwise
 */
const validateAwsEs = async () => {
  try {
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
    await client.ping({ requestTimeout: 3000 });
  } catch (err) {
    logger.error(err);
    throw new Error('Unable to connect to AWS Elasticsearch data source.');
  }
};

export { validateAwsEs };
