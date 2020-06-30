import _ from 'lodash';

import { parseQuery } from 'utils/parse-query';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Return a list of pets
 *
 * @param {object} query Query parameters
 * @returns {Promise<object>[]} Promise object represents a list of pets
 */
const getPets = async (query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    const { rawPets } = await connection.execute(contrib.getPets(parsedQuery));
    return rawPets;
  } finally {
    connection.close();
  }
};

/**
 * Return a specific pet by unique ID
 *
 * @param {string} id Unique pet ID
 * @returns {Promise<object>} Promise object represents a specific pet or return undefined if term
 *                            is not found
 */
const getPetById = async (id) => {
  const connection = await getConnection();
  try {
    const { rawPets } = await connection.execute(contrib.getPetById(id), id);

    if (_.isEmpty(rawPets)) {
      return undefined;
    }
    if (rawPets.length > 1) {
      throw new Error('Expect a single object but got multiple results.');
    } else {
      const [rawPet] = rawPets;
      return rawPet;
    }
  } finally {
    connection.close();
  }
};

export { getPets, getPetById };
