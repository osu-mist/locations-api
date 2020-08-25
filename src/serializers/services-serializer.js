import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const serviceResourceProp = openapi.components.schemas.ServiceResource.properties;
const serviceResourceType = serviceResourceProp.type.enum[0];
const serviceResourceKeys = _.keys(serviceResourceProp.attributes.properties);
const serviceResourcePath = 'services';

/**
 * Format and flatten raw service object for serializer
 *
 * @param {object} rawService Raw data row from data source
 * @returns {object} Formatted service object to be passed into serializer
 */
const formatService = (rawService) => {
  const { _source: serviceSource } = rawService;
  return {
    ...{ id: serviceSource.id, type: rawService.type },
    ...serviceSource.attributes,
  };
};

/**
 * Helper function to get the top level path and self link
 *
 * @param {string} path The path of the api call
 * @param {object} query Query param object
 * @returns {object} Object containing topLevelPath and topLevelSelfLink
 */
const getTopLevelData = (query, path) => {
  const topLevelPath = path.split('/').slice(2, path.length).join('/');
  const topLevelSelfLink = paramsLink(resourcePathLink(apiBaseUrl, topLevelPath), query);
  return { topLevelPath, topLevelSelfLink };
};

/**
 * Serialize serviceResources to JSON API
 *
 * @param {object[]} rawServices Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized serviceResources object
 */
const serializeServices = (rawServices, req) => {
  const { query, path } = req;
  const { topLevelPath, topLevelSelfLink } = getTopLevelData(query, path);

  // Add pagination links and meta information to options if pagination is enabled
  const pageQuery = {
    size: query['page[size]'],
    number: query['page[number]'],
  };

  const pagination = paginate(rawServices, pageQuery);
  pagination.totalResults = rawServices.length;
  rawServices = pagination.paginatedRows;

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: serviceResourceKeys,
    pagination,
    topLevelPath,
    resourcePath: serviceResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  // flatten attributes object in rawServices for serializer
  const newRawServices = [];
  _.forEach(rawServices, (rawService) => newRawServices.push(formatService(rawService)));

  return new JsonApiSerializer(
    serviceResourceType,
    serializerOptions(serializerArgs),
  ).serialize(newRawServices);
};

/**
 * Serialize serviceResource to JSON API
 *
 * @param {object} rawService Raw data row from data source
 * @param {boolean} req Express request object
 * @returns {object} Serialized serviceResource object
 */
const serializeService = (rawService, req) => {
  const { query, path } = req;
  const { topLevelPath, topLevelSelfLink } = getTopLevelData(query, path);

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: serviceResourceKeys,
    resourcePath: serviceResourcePath,
    topLevelSelfLink,
    topLevelPath,
    query,
    enableDataLinks: true,
  };

  // flatten attributes object in rawServices for serializer
  const formattedService = formatService(rawService);

  return new JsonApiSerializer(
    serviceResourceType,
    serializerOptions(serializerArgs),
  ).serialize(formattedService);
};
export { serializeServices, serializeService };
