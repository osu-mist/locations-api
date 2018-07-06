package edu.oregonstate.mist.locations.db

import groovy.transform.InheritConstructors

/**
 * Raised for miscellaneous errors in DAO classes,
 * such as a data source being empty.
 */
@InheritConstructors
class DAOException extends RuntimeException {}
