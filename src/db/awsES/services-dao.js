import _ from 'lodash';
import AWS from 'aws-sdk';
import config from 'config';
import connectionClass from 'http-aws-es';
import esb from 'elastic-builder';
import elasticsearch from 'elasticsearch';

import { parseQuery } from 'utils/parse-query';

const { domain, accessKeyId, secretAccessKey } = config.get('dataSources.awsES');

const buildQueryBody = (queryParams) => {
  const parsedParams = parseQuery(queryParams);
  const q = esb.boolQuery();
  if (parsedParams.name !== undefined) {
    if (parsedParams.name.operator === 'fuzzy') {
      q.must(esb.matchQuery('attributes.name', parsedParams.name.value));
    } else {
      q.must(esb.termQuery('attributes.name.keyword', parsedParams.name));
    }
  }
  if (parsedParams.isOpen !== undefined) {
    if (parsedParams.isOpen) {
      const currentDayIndex = new Date().getDay();
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].start`).lte('now'));
      q.must(esb.rangeQuery(`attributes.openHours[${currentDayIndex}].end`).gte('now'));
    } else {
      q.mustNot(esb.existsQuery('attributes.openHours'));
    }
  }
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return a list of services
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of services
 */
const getServices = async (queryParams) => {
  const client = elasticsearch.Client({
    host: domain,
    log: 'error',
    connectionClass,
    awsConfig: new AWS.Config({
      accessKeyId,
      secretAccessKey,
      region: 'us-east-2',
    }),
  });
  const res = await client.search({
    index: 'services',
    body: buildQueryBody(queryParams),
  });

  const services = [];
  // eslint-disable-next-line dot-notation
  _.forEach(res.hits.hits, (service) => services.push(service['_source']));
  return services;
};

/**
 * Return a specific service by unique ID
 *
 * @param {string} id Unique service ID
 * @returns {Promise} Promise object represents a specific service
 */
const getServiceById = async (id) => id;

export { getServices, getServiceById };
