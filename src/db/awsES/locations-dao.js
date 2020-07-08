import _ from 'lodash';
import AWS from 'aws-sdk';
import config from 'config';
import connectionClass from 'http-aws-es';
import esb from 'elastic-builder';
import elasticsearch from 'elasticsearch';

const {
  domain,
  accessKeyId,
  secretAccessKey,
} = config.get('dataSources.awsES');

/**
 * Return a list of Locations
 * @param queryParams Object containing query parameters
 * @returns {Promise} Promise object represents a list of locations
 */
const getLocations = async (queryParams) => {
  const client = elasticsearch.Client({
    host: domain,
    log: 'error',
    connectionClass,
    awsConfig: new AWS.Config({
      accessKeyId,
      secretAccessKey,
      region: 'us-east-2',
    }),
  });
  const q = esb.boolQuery();
  if (queryParams['filter[name]']) {
    q.must(esb.matchQuery('attributes.name', queryParams['filter[name]']));
  }

  const res = await client.search({
    index: 'locations',
    body: esb.requestBodySearch().query(q).toJSON(),
  });

  const rawLocations = [];
  _.forEach(res.hits.hits, (value) => {
    // eslint-disable-next-line dot-notation
    rawLocations.push(value['_source']);
  });
  return rawLocations;
};

/**
 * Return a specific location by unique ID
 *
 * @param {string} id Unique location ID
 * @returns {Promise} Promise object represents a specific location
 */
const getLocationById = async (id) => id; /* {
  const options = { uri: `${sourceUri}/${id}`, json: true };
  const rawLocation = await rp(options);
  if (!rawLocation) {
    return undefined;
  }
  const serializedLocation = serializeLocation(rawLocation, endpointUri);
  return serializedLocation;

}; */

export { getLocations, getLocationById };
