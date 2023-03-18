import { Client } from '@elastic/elasticsearch';
import esb from 'elastic-builder';

import { clientOptions } from 'db/awsEs/connection';
import { parseQuery } from 'utils/parse-query';

const buildQueryBody = (queryParams) => {
  const parsedParams = parseQuery(queryParams);
  const q = esb.boolQuery();
  if (parsedParams.name !== undefined) {
    if (parsedParams.name.operator === 'fuzzy') {
      q.must(esb.matchQuery('attributes.name', parsedParams.name.value).fuzziness(10));
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

const buildIdQueryBody = (queryParams) => {
  const q = esb.boolQuery();
  q.must(esb.matchQuery('id', queryParams.serviceId));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return a list of services
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of services
 */
const getServices = async (queryParams) => {
  const client = Client(clientOptions());
  const res = await client.search({
    index: 'services',
    body: buildQueryBody(queryParams),
  });
  return res.body.hits.hits;
};

/**
 * Return a specific service by unique ID
 *
 * @param {string} queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a specific service
 */
const getServiceById = async (queryParams) => {
  const client = Client(clientOptions());
  const res = await client.search({
    index: 'services',
    body: buildIdQueryBody(queryParams),
  });
  return res.body.hits.hits[0];
};

export { getServices, getServiceById };
