import { Client } from 'elasticsearch';
import esb from 'elastic-builder';
import _ from 'lodash';

import { clientOptions } from 'db/awsEs/connection';

const buildIdQueryBody = (queryParams, type) => {
  const q = esb.boolQuery();
  const id = (type === 'locations') ? queryParams.locationId : queryParams.serviceId;
  q.must(esb.matchQuery('id', id));
  return esb.requestBodySearch().query(q).toJSON();
};

const buildBulkIdQueryBody = (ids, type) => {
  const q = esb.boolQuery();
  q.must(esb.idsQuery(type, ids));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return services related to a specific location
 *
 * @param {string} queryParams Query parameters from GET /locations/{locationId}/services request
 * @returns {Promise} Promise object represents a specific location
 */
const getServicesByLocationId = async (queryParams) => {
  const client = Client(clientOptions());
  const locationRes = await client.search({
    index: 'locations',
    body: buildIdQueryBody(queryParams, 'locations'),
  });

  const { _source: rawLocation } = locationRes.hits.hits[0];
  const serviceIds = [];
  _.forEach(rawLocation.relationships.services.data, (value) => serviceIds.push(value.id));

  const serviceRes = await client.search({
    index: 'services',
    body: buildBulkIdQueryBody(serviceIds, 'services'),
  });
  return serviceRes.hits.hits;
};

export { getServicesByLocationId };
