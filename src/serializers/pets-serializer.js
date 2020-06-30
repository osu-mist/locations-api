import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

<<<<<<< HEAD:src/api/v2/serializers/services-serializer.js
const petResourceProp = openapi.definitions.ServiceResource.properties;
=======
const petResourceProp = openapi.components.schemas.PetResource.properties;
>>>>>>> skeleton/openapi3:src/serializers/pets-serializer.js
const petResourceType = petResourceProp.type.enum[0];
const petResourceKeys = _.keys(petResourceProp.attributes.properties);
const petResourcePath = 'pets';
const petResourceUrl = resourcePathLink(apiBaseUrl, petResourcePath);

/**
 * Serialize petResources to JSON API
 *
 * @param {object[]} rawServices Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized petResources object
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
  ).serialize(rawServices);
};

/**
 * Serialize petResource to JSON API
 *
 * @param {object} rawService Raw data row from data source
 * @param {boolean} req Express request object
 * @returns {object} Serialized petResource object
 */
const serializeService = (rawService, req) => {
  const { query } = req;

  // TODO use req.path
  const baseUrl = req.method === 'POST'
    ? petResourceUrl
    : resourcePathLink(petResourceUrl, rawService.id);
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
  ).serialize(rawService);
};
export { serializeServices, serializeService };
