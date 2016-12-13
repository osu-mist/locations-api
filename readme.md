# Locations API.

## Skeleton

This API is based on the web-api-skeleton. For more documentation on the skeleton and the framework, see the github repo: https://github.com/osu-mist/web-api-skeleton

## Configure

Copy [configuration-example.yaml](configuration-example.yaml) to `configuration.yaml`. Modify as necessary, being careful to avoid committing sensitive data.

Please refer to [Location API](https://wiki.library.oregonstate.edu/confluence/display/CO/Location+API) for `locations` session configuration

Build the project:

    $ gradle build

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

## Run

Run the project:

    $ gradle run

## Resources

The Web API definition is contained in the [Swagger specification](swagger.yaml).

### GET /locations/dining

This resource returns all dining information and generates locations-dining.json for elasticsearch.

    $ curl https://localhost:8080/api/v0/locations/dining --cacert doej.pem --user "username:password"

```json
{
    "links":null,
    "data":[
        {"id":"935d84996ac29e8942f06ac6a6a26637",
         "type":"locations",
         "attributes":{ "name":"Trader Bing's",
                        "abbreviation":null,
                        "latitude":"44.565050",
                        "longitude":"-123.282096",
                        "summary":"Zone: Austin Hall",
                        "description":null,
                        "address":null,
                        "city":null,
                        "state":null,
                        "zip":null,
                        "county":null,
                        "telephone":null,
                        "fax":null,
                        "thumbnails":null,
                        "images":null,
                        "departments":null,
                        "website":null,
                        "sqft":null,
                        "calendar":null,
                        "campus":"corvallis",
                        "type":"dining",
                        "openHours":{"1":[{"start":"2016-06-20T14:30:00Z","end":"2016-06-20T22:30:00Z"}],"2":[{"start":"2016-06-20T14:30:00Z","end":"2016-06-20T22:30:00Z"}],"3":[{"start":"2016-06-20T14:30:00Z","end":"2016-06-20T22:30:00Z"}],"4":[{"start":"2016-06-20T14:30:00Z","end":"2016-06-20T22:30:00Z"}],"5":[],"6":[],"7":[]}},
            "links":{"self":"https://api.oregonstate.edu/v1/locations/935d84996ac29e8942f06ac6a6a26637"}},
            ...]
}
```
