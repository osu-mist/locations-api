import { errorBuilder, errorHandler } from 'errors/errors';
import { getLocationById } from '../../db/awsES/locations-dao';
import { serializeLocation } from '../../serializers/locations-serializer';

/**
 * Get location by unique ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { id } = req.params;
    const rawLocation = await getLocationById(id);
    if (!rawLocation) {
      errorBuilder(res, 404, 'A location with the specified ID was not found.');
    } else {
      const result = serializeLocation(rawLocation, req);
      res.send(result);
    }
  } catch (err) {
    errorHandler(res, err);
  }
};

export { get };
