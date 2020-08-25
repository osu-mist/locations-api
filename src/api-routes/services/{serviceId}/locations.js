import { getLocationsByServiceId } from 'db/awsEs/compound-request-dao';
import { errorHandler } from 'errors/errors';
import { serializeLocations } from 'serializers/locations-serializer';

/**
 * Get service by location Id
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const rawServices = await getLocationsByServiceId(req.params);
    const result = serializeLocations(rawServices, req);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
