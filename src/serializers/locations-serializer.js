import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const locationResourceProp = openapi.components.schemas.LocationResource.properties;
const serviceResourceProp = openapi.components.schemas.ServiceResource.properties;
const locationResourceType = locationResourceProp.type.enum[0];
const locationResourceKeys = _.keys(locationResourceProp.attributes.properties);
const serviceResourceKeys = _.keys(serviceResourceProp.attributes.properties);
const locationResourcePath = 'locations';

/**
 * Flattens the attributes of an included service for use in the serializer
 *
 * @param {object} services services attribute from the raw data row
 * @returns {object} Object containing flattened service attributes
 */
const flattenAttributes = (services) => {
  const newServices = [];
  _.forEach(services, (service) => {
    const newService = {
      ...service,
      ...service.attributes,
    };
    delete newService.attributes;
    newServices.push(newService);
  });
  return newServices;
};

/**
 * Format raw location rows for use in serializer
 *
 * @param {object} locationSource _source attribute from raw data row
 * @returns {object} Formatted location object to be passed into serializer
 */
const formatLocation = (locationSource) => {
  const locationAttributes = locationSource.attributes;
  locationSource.attributes.abbreviations = {
    arcGis: locationAttributes.arcGisAbbreviation,
    banner: locationAttributes.bannerAbbreviation,
  };
  locationSource.attributes.giRestrooms = {
    count: locationAttributes.girCount,
    limit: locationAttributes.girLimit,
    locations: (locationAttributes.girLocations)
      ? locationAttributes.girLocations.split(', ')
      : null,
  };
  locationSource.attributes.parkingSpaces = {
    evSpaceCount: locationAttributes.evParkingSpaceCount,
    adaSpaceCount: locationAttributes.adaParkingSpaceCount,
    motorcyclesSpaceCount: locationAttributes.motorcycleParkingSpaceCount,
  };
  locationSource.attributes.coordinates = (locationAttributes.geoLocation)
    ? { lat: locationAttributes.geoLocation.lat, lon: locationAttributes.geoLocation.lon }
    : null;
  return {
    id: locationSource.id,
    type: locationSource.type,
    ...locationSource.attributes,
    services: flattenAttributes(locationSource.services),
  };
};

const includedArgs = {
  ref: 'id',
  type: 'services',
  attributes: [...serviceResourceKeys],
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
 * Serialize locationResources to JSON API
 *
 * @param {object[]} rawLocations Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized locationResources object
 */
const serializeLocations = (rawLocations, req) => {
  const { query, path } = req;
  const { topLevelPath, topLevelSelfLink } = getTopLevelData(query, path);

  // Add pagination links and meta information to options if pagination is enabled
  const pageQuery = {
    size: query['page[size]'],
    number: query['page[number]'],
  };

  const pagination = paginate(rawLocations, pageQuery);
  pagination.totalResults = rawLocations.length;
  rawLocations = pagination.paginatedRows;

  // format and flatten attributes object in rawLocations for serializer
  const formattedLocations = [];
  _.forEach(rawLocations, (rawLocation) => {
    const { _source: locationSource } = rawLocation;
    formattedLocations.push(formatLocation(locationSource));
  });

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    pagination,
    topLevelPath,
    resourcePath: locationResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
    included: (formattedLocations[0].services) ? includedArgs : undefined,
    includedType: 'services',
  };

  return new JsonApiSerializer(
    locationResourceType,
    serializerOptions(serializerArgs),
  ).serialize(formattedLocations);
};

/**
 * Serialize locationResource to JSON API
 *
 * @param {object} rawLocation Raw data row from data source
 * @param {boolean} req Express request object
 * @returns {object} Serialized locationResource object
 */
const serializeLocation = (rawLocation, req) => {
  const { query, path } = req;
  const { topLevelPath, topLevelSelfLink } = getTopLevelData(query, path);
  const { _source: locationSource } = rawLocation;

  // format and flatten attributes object in rawLocation for serializer
  const formattedLocation = formatLocation(locationSource);

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    resourcePath: locationResourcePath,
    topLevelSelfLink,
    topLevelPath,
    query,
    enableDataLinks: true,
    included: (formattedLocation.services) ? includedArgs : undefined,
    includedType: 'services',
  };

  return new JsonApiSerializer(
    locationResourceType,
    serializerOptions(serializerArgs),
  ).serialize(formattedLocation);
};
export { serializeLocations, serializeLocation };
