import config from 'config';
import _ from 'lodash';

import { logger } from 'utils/logger';

const { dataSources } = config.get('dataSources');
const awsEs = dataSources.includes('awsEs')
  ? require('db/awsEs/connection').validateAwsEs
  : null;

/** Validate database configuration */
const validateDataSource = () => {
  const validationMethods = { awsEs };

  _.each(dataSources, (dataSourceType) => {
    if (dataSourceType in validationMethods) {
      validationMethods[dataSourceType]().catch((err) => {
        logger.error(err);
        process.exit(1);
      });
    } else {
      throw new Error(`Data source type: '${dataSourceType}' is not recognized.`);
    }
  });
};

export { validateDataSource };
