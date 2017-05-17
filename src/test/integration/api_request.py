import json
import requests
import urllib2
import ssl
from configuration_load import *

from requests.adapters import HTTPAdapter
from requests.packages.urllib3.poolmanager import PoolManager
import ssl

def get_extension_offices(url, access_token):
    query_params = {'page[size]': 1000}
    return query_request(url, access_token, "get", query_params).json()

def query_request(url, access_token, verb, query_params):
    headers = {'Authorization': access_token}
    request = requests.request(verb, url, params=query_params, headers=headers, verify=False)
    return request

def unauth_request(url):
    request = requests.get(url, verify=False)
    return request.status_code

def response_time(url, access_token):
    query_params = {'q': 'Oxford'}
    headers = {'Authorization': access_token}
    request = requests.get(url, params=query_params, headers=headers, verify=False)
    response_time = request.elapsed.total_seconds()

    print "API response time: ", response_time, " seconds"
    return response_time
