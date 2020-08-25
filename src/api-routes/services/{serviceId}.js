import { getServiceById } from 'db/awsEs/services-dao';
import { errorBuilder, errorHandler } from 'errors/errors';
import { serializeService } from 'serializers/services-serializer';

/**
 * Get service by unique ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const rawService = await getServiceById(req.params);
    if (!rawService) {
      errorBuilder(res, 404, 'A service with the specified ID was not found.');
    } else {
      const result = serializeService(rawService, req);
      res.send(result);
    }
  } catch (err) {
    errorHandler(res, err);
  }
};

export { get };
