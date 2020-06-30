import _ from 'lodash';
import config from 'config';
import uuidv1 from 'uuid';

import { parseQuery } from 'utils/parse-query';
import { readJsonFile, writeJsonFile } from './fs-operations';

const { dbPath } = config.get('dataSources.json');

/**
 * Return a list of pets
 *
 * @param {object} query Query parameters
 * @returns {Promise} Promise object represents a list of pets
 */
const getPets = async (query) => {
  let rawPets = readJsonFile(dbPath).pets;
  const parsedQuery = parseQuery(query);
  const { species, hasOwner, age } = parsedQuery;

  rawPets = species ? _.filter(rawPets, { species }) : rawPets;
  if (hasOwner !== undefined) {
    if (!hasOwner) {
      rawPets = _.filter(rawPets, { owner: '' });
    } else {
      rawPets = _.remove(rawPets, (value) => value.owner !== '');
    }
  }
  if (age) {
    if (age.operator === 'gt') {
      rawPets = _.remove(rawPets, (pet) => pet.age > age.value);
    } else if (age.operator === 'lt') {
      rawPets = _.remove(rawPets, (pet) => pet.age < age.value);
    }
  }
  return rawPets;
};

/**
 * Return a specific pet by unique ID
 *
 * @param {string} id Unique pet ID
 * @returns {Promise} Promise object represents a specific pet
 */
const getPetById = async (id) => {
  const rawPets = readJsonFile(dbPath).pets;
  const rawPet = _.find(rawPets, { id });
  if (!rawPet) {
    return undefined;
  }
  return rawPet;
};

/**
 * Posts a new pet
 *
 * 1. Reads the JSON DB as an array of objects
 * 2. Inserts posted pet into the array
 * 3. Overwrites JSON DB with new file
 *
 * @param {object} body Request body
 * @returns {Promise} Promise object represents the posted pet
 */
const postPet = async (body) => {
  // Read DB
  const rawPets = readJsonFile(dbPath).pets;
  const newPet = body.data.attributes;

  // Write new pet to DB
  newPet.id = uuidv1();
  rawPets.push(newPet);
  writeJsonFile(dbPath, { pets: rawPets });

  // Return new pet
  return newPet;
};

export {
  getPets,
  getPetById,
  postPet,
};
