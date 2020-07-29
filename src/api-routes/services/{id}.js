import { errorBuilder, errorHandler } from 'errors/errors';
import { getServiceById } from '../../db/awsEs/services-dao';
import { serializeService } from '../../serializers/services-serializer';

/**
 * Get service by unique ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { id } = req.params;
    const rawService = await getServiceById(id);
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
