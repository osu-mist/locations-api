import { getServicesByLocationId } from 'db/awsEs/compound-request-dao';
import { errorHandler } from 'errors/errors';
import { serializeServices } from 'serializers/services-serializer';

/**
 * Get service by location Id
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const rawServices = await getServicesByLocationId(req.params);
    const result = serializeServices(rawServices, req);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
