import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const petResourceProp = openapi.definitions.LocationResource.properties;
const petResourceType = petResourceProp.type.enum[0];
const petResourceKeys = _.keys(petResourceProp.attributes.properties);
const petResourcePath = 'pets';
const petResourceUrl = resourcePathLink(apiBaseUrl, petResourcePath);

/**
 * Serialize petResources to JSON API
 *
 * @param {object[]} rawLocations Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized petResources object
 */
const serializeLocations = (rawLocations, req) => {
  const { query } = req;

  // Add pagination links and meta information to options if pagination is enabled
  const pageQuery = {
    size: query['page[size]'],
    number: query['page[number]'],
  };

  const pagination = paginate(rawLocations, pageQuery);
  pagination.totalResults = rawLocations.length;
  rawLocations = pagination.paginatedRows;

  // TODO use req.path
  const topLevelSelfLink = paramsLink(petResourceUrl, query);
  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: petResourceKeys,
    pagination,
    resourcePath: petResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    petResourceType,
    serializerOptions(serializerArgs),
  ).serialize(rawLocations);
};

/**
 * Serialize petResource to JSON API
 *
 * @param {object} rawLocation Raw data row from data source
 * @param {boolean} req Express request object
 * @returns {object} Serialized petResource object
 */
const serializeLocation = (rawLocation, req) => {
  const { query } = req;

  // TODO use req.path
  const baseUrl = req.method === 'POST'
    ? petResourceUrl
    : resourcePathLink(petResourceUrl, rawLocation.id);
  const topLevelSelfLink = paramsLink(baseUrl, query);

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: petResourceKeys,
    resourcePath: petResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    petResourceType,
    serializerOptions(serializerArgs),
  ).serialize(rawLocation);
};
export { serializeLocations, serializeLocation };
