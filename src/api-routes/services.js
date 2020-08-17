import { getServices } from 'db/awsEs/services-dao';
import { errorHandler } from 'errors/errors';
import { serializeServices } from 'serializers/services-serializer';

/**
 * Get services
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const rawServices = await getServices(req.query);
    const result = serializeServices(rawServices, req);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
