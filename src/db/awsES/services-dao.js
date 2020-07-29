import _ from 'lodash';
import elasticsearch from 'elasticsearch';
import esb from 'elastic-builder';

import { clientOptions } from 'db/awsEs/connection';
import { parseQuery } from 'utils/parse-query';

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
  const client = elasticsearch.Client(clientOptions());
  const res = await client.search({
    index: 'services',
    body: buildQueryBody(queryParams),
  });

  const services = [];

  _.forEach(res.hits.hits, (service) => {
    const { _source: locationSource } = service;
    services.push(locationSource);
  });
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
