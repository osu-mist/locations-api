const appRoot = require('app-root-path');
const config = require('config');
const fs = require('fs');
const yaml = require('js-yaml');
const decodeUriComponent = require('decode-uri-component');
const url = require('url');

const { protocol, hostname } = config.get('server');
const { basePath } = yaml.safeLoad(fs.readFileSync(`${appRoot}/openapi.yaml`, 'utf8'));

/**
 * @summary Self link builder
 * @function
 * @param {string} id resource ID
 * @param {string} resourcePath resource path
 * @returns A self link URL
 */
const selfLink = (id, resourcePath) => url.format({
  protocol,
  hostname,
  pathname: `${basePath}/${resourcePath}/${id}`,
});

/**
 * @summary Top level query link builder
 * @function
 * @param {object} query
 * @param {string} resourcePath resource path
 * @returns A decoded url formatted with query parameters in the query object.
 */
const querySelfLink = (query, resourcePath) => decodeUriComponent(url.format({
  protocol,
  hostname,
  pathname: `${basePath}/${resourcePath}`,
  query,
}));

/**
 * @summary Paginated link builder
 * @function
 * @param {number} pageNumber Page number of results
 * @param {number} pageSize Number of results to return
 * @param {string} resourcePath resource path
 * @returns A decoded paginated link URL
 */
const paginatedLink = (pageNumber, pageSize, resourcePath) => querySelfLink({
  'page[number]': pageNumber,
  'page[size]': pageSize,
}, resourcePath);

module.exports = { selfLink, paginatedLink, querySelfLink };
