# Locations API.


## Skeleton 

This API is based on the web-api-skeleton. For more documentation on the skeleton and the framework, see the github repo: https://github.com/osu-mist/web-api-skeleton

## Configure

Copy [configuration-example.yaml](configuration-example.yaml) to `configuration.yaml`. Modify as necessary, being careful to avoid committing sensitive data.


Build the project:

    $ gradle build

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

## Run

Run the project:

    $ gradle run

## Resources

The Web API definition is contained in the [Swagger specification](swagger.yaml).

### GET /locations/{id}

This resource returns the information for a given building:

    $ wget http://localhost:8008/locations/743

    {
        "id":743,
        "name":"Valley Library",
        "abbrev":"VLib",
        "shortDescription":"brief desc",
        "description":"longer desc",
        "address":"201 SW Waldo Place",
        "latitude":"44.5650618928",
        "longitude":"-123.27603917",
        "adaEntrance":"ENTRIES: North entry: level to 2nd floor;.\r\nFLOORS: All floors; elevator; access north section power-assisted doors of 1st through Reserve Book staff area.",
        "thumbnail":"valley_library_thumbnail.jpg",
        "largerImage":"valley_library.jpg"
    }
