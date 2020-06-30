import { errorHandler } from 'errors/errors';
import { getLocations } from '../db/http/locations-dao';
import { serializeLocations } from '../serializers/locations-serializer';

/**
 * Get locations
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const rawLocations = await getLocations(req.query);
    const result = serializeLocations(rawLocations, req);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
