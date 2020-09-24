import _ from 'lodash';

import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

/**
 * Helper function to generate pagination params
 *
 * @param {number} pageNumber page number
 * @param {number} pageSize page size
 * @returns {object} pagination parameters object
 */
const pageParamsBuilder = (pageNumber, pageSize) => (
  { 'page[number]': pageNumber, 'page[size]': pageSize }
);

/**
 * Generate JSON API serializer options
 *
 * @param {object[]} serializerArgs JSON API serializer arguments
 * @returns {object} JSON API serializer options
 */
const serializerOptions = (serializerArgs) => {
  const {
    identifierField,
    resourceKeys,
    pagination,
    resourcePath,
    topLevelPath,
    topLevelSelfLink,
    query,
    keyForAttribute,
    enableDataLinks,
    transformFunction,
    included,
    includedType,
  } = serializerArgs;

  const resourceUrl = resourcePathLink(apiBaseUrl, resourcePath);
  const topLevelUrl = (topLevelPath)
    ? resourcePathLink(apiBaseUrl, topLevelPath)
    : resourceUrl;

  const options = {
    pluralizeType: false,
    attributes: resourceKeys,
    id: identifierField,
    keyForAttribute: keyForAttribute || 'camelCase',
    dataLinks: {
      self: (row) => {
        if (enableDataLinks) {
          return resourcePathLink(resourceUrl, row[identifierField]);
        }
        return null;
      },
    },
    topLevelLinks: { self: topLevelSelfLink },
    nullIfMissing: true,
  };

  if (transformFunction) options.transform = transformFunction;

  if (included) {
    options[includedType] = included;
    options.attributes.push(includedType);
  }

  if (pagination) {
    const {
      pageNumber,
      totalPages,
      nextPage,
      prevPage,
      pageSize,
      totalResults,
    } = pagination;

    options.topLevelLinks = _.assign(options.topLevelLinks, {
      first: paramsLink(topLevelUrl, { ...query, ...pageParamsBuilder(1, pageSize) }),
      last: paramsLink(topLevelUrl, { ...query, ...pageParamsBuilder(totalPages, pageSize) }),
      next: nextPage
        ? paramsLink(topLevelUrl, { ...query, ...pageParamsBuilder(nextPage, pageSize) })
        : null,
      prev: prevPage
        ? paramsLink(topLevelUrl, { ...query, ...pageParamsBuilder(prevPage, pageSize) })
        : null,
    });

    options.meta = {
      totalResults,
      totalPages,
      currentPageNumber: pageNumber,
      currentPageSize: pageSize,
    };
  }

  return options;
};

export { serializerOptions };
