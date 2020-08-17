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
const locationResourceUrl = resourcePathLink(apiBaseUrl, locationResourcePath);

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
  if (locationAttributes.geoLocation) {
    locationSource.attributes.coordinates = {
      lat: locationAttributes.geoLocation.lat,
      lon: locationAttributes.geoLocation.lon,
    };
  } else {
    locationSource.attributes.coordinates = null;
  }
  locationSource.type = rawLocation.type;
  return {
    ...{ id: locationSource.id, type: rawLocation.type },
    ...locationSource.attributes,
  };
};

/**
 * Serialize locationResources to JSON API
 *
 * @param {object[]} rawLocations Raw data rows from data source
 * @param {object} req Express request object
 * @returns {object} Serialized locationResources object
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
  const topLevelSelfLink = paramsLink(locationResourceUrl, query);
  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    pagination,
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
  const { query } = req;
  // TODO use req.path
  const baseUrl = req.method === 'POST'
    ? locationResourceUrl
    // eslint-disable-next-line dot-notation
    : resourcePathLink(locationResourceUrl, rawLocation['_id']);
  const topLevelSelfLink = paramsLink(baseUrl, query);

  const serializerArgs = {
    identifierField: 'id',
    resourceKeys: locationResourceKeys,
    resourcePath: locationResourcePath,
    topLevelSelfLink,
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
