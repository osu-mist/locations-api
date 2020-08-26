import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const locationResourceProp = openapi.components.schemas.LocationResource.properties;
const locationResourceType = locationResourceProp.type.enum[0];
const locationResourceKeys = _.keys(locationResourceProp.attributes.properties);
const locationResourcePath = 'locations';

/**
 * Serialize locationResources to JSON API
 *
 * @param {object} rawLocation Raw data row from data source
 * @returns {object} Formatted location object to be passed into serializer
 */
const formatLocation = (rawLocation) => {
  const { _source: locationSource } = rawLocation;
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
  locationSource.type = rawLocation.type;
  return {
    ...{ id: locationSource.id, type: rawLocation.type },
    ...locationSource.attributes,
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

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    pagination,
    topLevelPath,
    resourcePath: locationResourcePath,
    topLevelSelfLink,
    query,
    enableDataLinks: true,
  };

  // format and flatten attributes object in rawLocations for serializer
  const newRawLocations = [];
  _.forEach(rawLocations, (rawLocation) => newRawLocations.push(formatLocation(rawLocation)));

  return new JsonApiSerializer(
    locationResourceType,
    serializerOptions(serializerArgs),
  ).serialize(newRawLocations);
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

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    resourcePath: locationResourcePath,
    topLevelSelfLink,
    topLevelPath,
    query,
    enableDataLinks: true,
  };

  // format and flatten attributes object in rawLocation for serializer
  const formattedLocation = formatLocation(rawLocation);

  return new JsonApiSerializer(
    locationResourceType,
    serializerOptions(serializerArgs),
  ).serialize(formattedLocation);
};
export { serializeLocations, serializeLocation };
