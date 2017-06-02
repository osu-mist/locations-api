import json
import requests

def get_config_data(config_path):
    config_data_file = open(config_path)
    return json.load(config_data_file)

def get_url(config_path):
    config_data = get_config_data(config_path)
    return config_data["hostname"]

def get_access_token(config_path):
    config_data = get_config_data(config_path)
    return config_data["access_token"]

def get_max_invalid_dining(config_path):
    config_data = get_config_data(config_path)
    return config_data["max_invalid_dining"]

def get_min_valid_days(config_path):
    config_data = get_config_data(config_path)
    return config_data["min_valid_days"]

def is_debug_enabled(config_path):
    config_data = get_config_data(config_path)
    return config_data["debug"]
