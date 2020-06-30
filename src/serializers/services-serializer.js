import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const serviceResourceProp = openapi.definitions.ServiceResource.properties;
const serviceResourceType = serviceResourceProp.type.enum[0];
const serviceResourceKeys = _.keys(serviceResourceProp.attributes.properties);
const serviceResourcePath = 'services';
const serviceResourceUrl = resourcePathLink(apiBaseUrl, serviceResourcePath);

/**
 * Serialize serviceResources to JSON API
 *
 * @param {object[]} rawServices Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized serviceResources object
 */
const serializeServices = (rawServices, req) => {
  const { query } = req;

  // Add pagination links and meta information to options if pagination is enabled
  const pageQuery = {
    size: query['page[size]'],
    number: query['page[number]'],
  };

  const pagination = paginate(rawServices, pageQuery);
  pagination.totalResults = rawServices.length;
  rawServices = pagination.paginatedRows;

  // TODO use req.path
  const topLevelSelfLink = paramsLink(serviceResourceUrl, query);
  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: serviceResourceKeys,
    pagination,
    resourcePath: serviceResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    serviceResourceType,
    serializerOptions(serializerArgs),
  ).serialize(rawServices);
};

/**
 * Serialize serviceResource to JSON API
 *
 * @param {object} rawService Raw data row from data source
 * @param {boolean} req Express request object
 * @returns {object} Serialized serviceResource object
 */
const serializeService = (rawService, req) => {
  const { query } = req;

  // TODO use req.path
  const baseUrl = req.method === 'POST'
    ? serviceResourceUrl
    : resourcePathLink(serviceResourceUrl, rawService.id);
  const topLevelSelfLink = paramsLink(baseUrl, query);

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: serviceResourceKeys,
    resourcePath: serviceResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    serviceResourceType,
    serializerOptions(serializerArgs),
  ).serialize(rawService);
};
export { serializeServices, serializeService };
