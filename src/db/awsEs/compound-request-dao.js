import { Client } from '@elastic/elasticsearch';
import esb from 'elastic-builder';
import _ from 'lodash';

import { clientOptions } from 'db/awsEs/connection';

/**
 * Generates a query body object to get a specific location or service by ID.
 *
 * @param {string} queryParams Query parameters from the endpoint request
 * @param {string} type The ID's document type. Should be either 'locations' or 'services'
 * @returns {object} Elasticsearch query body
 */
const buildIdQueryBody = (queryParams, type) => {
  const q = esb.boolQuery();
  const id = (type === 'locations') ? queryParams.locationId : queryParams.serviceId;
  q.must(esb.matchQuery('id', id));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Generates a query body object to get all documents that match the given IDs
 *
 * @param {string[]} ids An array of document IDs to search for
 * @param {string} type The IDs' document type. Should be either 'locations' or 'services'
 * @returns {object} Elasticsearch query body
 */
const buildBulkIdQueryBody = (ids, type) => {
  const q = esb.boolQuery();
  q.must(esb.idsQuery(type, ids));
  return esb.requestBodySearch().query(q).toJSON();
};

/**
 * Return services related to a specific location
 *
 * @param {string} queryParams Query parameters from GET /locations/{locationId}/services request
 * @returns {Promise} Promise object represents the related services
 */
const getServicesByLocationId = async (queryParams) => {
  const client = Client(clientOptions());
  const locationRes = await client.search({
    index: 'locations',
    body: buildIdQueryBody(queryParams, 'locations'),
  });

  const { _source: rawLocation } = locationRes.hits.hits[0];
  const serviceIds = _.map(rawLocation.relationships.services.data, 'id');

  const serviceRes = await client.search({
    index: 'services',
    body: buildBulkIdQueryBody(serviceIds, 'services'),
  });
  return serviceRes.body.hits.hits;
};

/**
 * Return locations related to a specific service
 *
 * @param {string} queryParams Query parameters from GET /services/{serviceId}/locations request
 * @returns {Promise} Promise object represents the related locations
 */
const getLocationsByServiceId = async (queryParams) => {
  const client = Client(clientOptions());
  const serviceRes = await client.search({
    index: 'services',
    body: buildIdQueryBody(queryParams, 'services'),
  });

  const { _source: rawService } = serviceRes.hits.hits[0];
  const locationIds = _.map(rawService.relationships.location.data, 'id');

  const locationRes = await client.search({
    index: 'locations',
    body: buildBulkIdQueryBody(locationIds, 'locations'),
  });
  return locationRes.body.hits.hits;
};

export { getServicesByLocationId, getLocationsByServiceId };
