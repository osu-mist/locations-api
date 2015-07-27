# Web API Skeleton

Locations API.


## Tasks

List all tasks runnable from root project:

    $ gradle tasks

### IntelliJ IDEA

Generate IntelliJ IDEA project:

    $ gradle idea

Open with `File` -> `Open Project`.

### Build

Build the project:

    $ gradle build

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

### Run

Run the project:

    $ gradle run



## Resources

The Web API definition is contained in the [Swagger specification](swagger.yaml).

### GET /

This sample resource returns a short message:

    $ nc localhost 8008 << HERE
    > GET / HTTP/1.0
    > 
    > HERE
    HTTP/1.1 200 OK
    Date: Mon, 20 Jul 2015 21:51:49 GMT
    Content-Type: text/plain
    Content-Length: 11
    
    hello world
