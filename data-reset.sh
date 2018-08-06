#!/bin/bash

# Refresh data
curl -k -X GET -u $USER:$PASSWORD "https://localhost:8012/api/v1/locations/services"
curl -k -X GET -u $USER:$PASSWORD "https://localhost:8012/api/v1/locations/combined"

# Delete indices
curl -s -XDELETE http://localhost:9200/locations/ ; echo
curl -s -XDELETE http://localhost:9200/services/ ; echo

# Upload template mappings
curl -s -H "Content-Type: application/json" -XPOST http://localhost:9200/_template/template_2 --data-binary "@ES-services-index-template.json"; echo
curl -s -H "Content-Type: application/json" -XPOST http://localhost:9200/_template/template_1 --data-binary "@ES-locations-index-template.json"; echo

# Upload documents to ES
curl -s -H "Content-Type: application/json" -XPOST localhost:9200/locations/locations/_bulk --data-binary "@locations-combined.json"; echo
curl -s -H "Content-Type: application/json" -XPOST localhost:9200/services/services/_bulk --data-binary "@services.json"; echo
