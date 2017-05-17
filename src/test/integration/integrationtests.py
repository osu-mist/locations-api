import unittest
import sys
import json
import logging
from api_request import *
from configuration_load import *

class dw_tests(unittest.TestCase):

    def test_extension(self):
        offices = get_extension_offices(extensions_url, access_token)

        # check that we have extension locations
        self.assertGreater(len(offices["data"]), 10)

        for office in offices["data"]:
            self.assertIsNotNone(office["id"])
            self.assertEqual(office["type"], "locations")
            self.assertIsNotNone(office["attributes"]["name"])
            self.assertEqual(office["attributes"]["type"], "building")
            self.assertIsNotNone(office["attributes"]["county"])
            self.assertIsNotNone(office["attributes"]["zip"])
            self.assertIsNotNone(office["attributes"]["fax"])
            self.assertIsNotNone(office["attributes"]["website"])
            self.assertIsNotNone(office["attributes"]["website"])

            # Refactor into a separate method?
            self.assertTrue(self.check_lat_lon(office))

    def test_dining(self):
        invalid_dining_count = 0

        # check that we have extension locations
        self.assertGreater(len(restaurants["data"]), 10)

        for diner in restaurants["data"]:
            if not self.is_valid_dining(diner) or not self.has_valid_open_hours(diner):
                invalid_dining_count += 1

        logger.debug("invalid dining locations: " + str(invalid_dining_count))
        self.assertLessEqual(invalid_dining_count, max_invalid_dining)
        return

    def is_valid_dining(self, location):
        return location["attributes"]["name"] is not None and \
                location["attributes"]["summary"] is not None and \
                self.check_lat_lon(location)

    def check_lat_lon(self, location):
        return location["attributes"]["geoLocation"] is not None and \
                location["attributes"]["geoLocation"]["lat"] is not None and \
                location["attributes"]["geoLocation"]["lon"] is not None and \
                (type(location["attributes"]["geoLocation"]["lon"]) is float) and \
                (type(location["attributes"]["geoLocation"]["lat"]) is float)

    def has_valid_open_hours(self, location):
        valid_day_range = range(1,7)
        invalid_days = 0

        if "openHours" not in location["attributes"]:
            logger.debug("doesn't have hours " + str(location["attributes"]["name"]))
            return False

        for day, open_hour_list in location["attributes"]["openHours"].iteritems():
            valid_day_index = int(day) in valid_day_range
            invalid_time_slot_count = len([i for i in open_hour_list if not self.is_valid_open_slot(i)])
            logger.debug("day in range: " + str(valid_day_index))
            if invalid_time_slot_count > 1 or not valid_day_index:
                invalid_days += 1

        logger.debug("invalid days: " + str(invalid_days) + " check against: " + str(min_valid_days))
        return (7 - invalid_days) >= min_valid_days

    def is_valid_open_slot(self, open_slot):
        return open_slot is not None and open_slot["start"] is not None and open_slot["end"] is not None

    def testOpenHoursUTC(self):
        test_slot = None
        test_diners = [diner for diner in restaurants["data"] if self.has_valid_open_hours(diner)]
        open_hours = test_diners[0]["attributes"]["openHours"] 

        logger.debug("test utc with diner: " + test_diners[0]["attributes"]["name"])

        for day, open_hour_list in open_hours.iteritems():
            valid_slots = [i for i in open_hour_list if self.is_valid_open_slot(i)]
            if len(valid_slots) > 0:
                test_slot = valid_slots[0]
                logger.debug("test day slot day: " + str(day) + ", slot: " + str(test_slot))
                break

        regex = "[0-9]{4}-[0-9]{2}-[0-9]{2}[T][0-9]{2}:[0-9]{2}:[0-9]{2}[Z]"
        self.assertRegexpMatches(test_slot["start"], regex)
        self.assertRegexpMatches(test_slot["end"], regex)

    # Tests that different verbs return expected responses
    def test_verbs(self):
        query_params = {}

        self.assertEqual(query_request(extensions_url, access_token, "get", query_params).status_code, 200)

        self.assertEqual(query_request(extensions_url, access_token, "post", query_params).status_code, 405)
        self.assertEqual(query_request(extensions_url, access_token, "put", query_params).status_code, 405)
        self.assertEqual(query_request(extensions_url, access_token, "delete", query_params).status_code, 405)

    # Tests that a request with auth header returns a 401
    def test_unauth(self):
        self.assertEqual(unauth_request(extensions_url), 401)

    # Tests that API response time is less than a value
    def test_response_time(self):
        self.assertLess(response_time(extensions_url, access_token), 2)
        self.assertLess(response_time(dining_url, access_token), 3)

if __name__ == '__main__':
    options_tpl = ('-i', 'config_path')
    del_list = []

    for i,config_path in enumerate(sys.argv):
        if config_path in options_tpl:
            del_list.append(i)
            del_list.append(i+1)

    del_list.reverse()

    for i in del_list:
        del sys.argv[i]

    # set debug level
    if is_debug_enabled(config_path):
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.WARNING)

    logger = logging.getLogger(__name__)

    # load config variables
    url = get_url(config_path)
    access_token = get_access_token(config_path)
    max_invalid_dining = get_max_invalid_dining(config_path)
    min_valid_days = get_min_valid_days(config_path)

    # load data needed for testing
    extensions_url = url + "/extension"
    dining_url = url + "/dining"
    restaurants = get_extension_offices(dining_url, access_token)

    unittest.main()
