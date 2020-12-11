# Weather micro-services

This repository contains two Java Spark applications that provide RESTful webservices
relating to weather data. The `weather-data` app also contains two sample HTML front ends to
the data.

## `weather-data`

This folder contains a maven project that builds the `weather-data` micro-service. It provides a
REST API in front of an SQLite database containing records of weather observations. As you have seen
a number of similar Spark applications in lectures and lab exercise, you should be able to get an
idea of how everything works by reading the code. Each record contains the following data:

+ a unique record id,
+ a location id,
+ the time at which the recording was made,
+ the temperature,
+ the humidity,
+ the wind speed, and
+ the wind direction.

Each location consists of the following data:

+ a unique location id,
+ its name, and
+ its latitude and longitude.

To run the service you must first populate the database by running the `main` method in
the class `loader.Main`. Do this in your ide or by running the maven target `mvn exec:java@loader`.

Now you can run the service by running the `main` method of the class `ci646.weather.Application`.
This starts the Spark application listening for HTTP requests at the address `http://localhost:4567`.
The webservice delivers JSON data via the following endpoints:

| Endpoint | Verb | Description |
| -------- | ---- | ----------- |
| `/locations` | `GET` | Retrieve an array of all locations |
| `/locations` | `POST` | Create a new location. `POST` data parameters expected are `name` (a string), `lat` (the latitude, floating point number), `lon` (the logitude, a floating point number), `asl` (a floating point number). The response will contain the new location. |
| `/locations/<loc>` | `GET` | Returns the location(s) matching `<loc>`. If `<loc>` is a number, the response will be the single location with this id, if one exists. If `<loc>` is a string, the response will be an array of location objects whose names fuzzily match that string. |
| `/records` | `GET` | Retrieve an array of all records. |
| `/records/<id>` | `GET` | Retrieve an array of all records with location id equal to `<id>`. |
| `/records/<id>/<from>/<to>` | `GET` | Retrieve an array of all records with location id equal to `<id>` and a timestamp that falls between `<from>` and `<to>`. These timestamps must be supplied in the format `yyyy-MM-ddTHH:mm`. For example `2020-12-01T00:00`. |
| `/records/<id>` | `POST` | Create a new record. `POST` data parameters expected are `ts` (a timestamp in the format given above), `temp` (the temperature, a floating point number), `hum` (the humidity, a floating point number), `ws` (the wind speed, a floating point number), `wd` (the wind direction, a floating point number). The response will contain the new record. |

You can use the UNIX command line tool `curl` to call these endpoints with the right kinds of request. For example,

```
$ curl -H "Accept: application/json"  http://localhost:4567/locations
{ "status": "SUCCESS", "url": "http://brighton.ac.uk", "enc": "be6ce4cd" }

$ curl -H "Accept: application/json" --request POST -d \
  "name=Kolkata, India&lat=22.5726&lon=88.3639&asl=0" http://localhost:4567/locations
{}

$ curl -H "Accept: application/json" --request POST -d \
"ts=2020-12-07T00:00&temp=5.0&hum=9&ws=42&wd=99" http://localhost:4567/records/2
{"recordID":198,"locID":2,"timestamp":"Dec 7, 2020, 12:00:00 AM",
"temperature":5.0,"humidity":9.0,"windSpeed":42.0,"windDirection":99.0}
```

## Alternative front-ends to `weather-data`

The `weather-data` contains two simple web front ends to the data created using Javascript libraries. The first of these is a scatter plot of
all records for a given location. This is in the file `src/main/resources/public/chart.html`. It uses the `D3` library to call the webservice
endpoints to retrieve a list of locations then, when the user selects a location, retrieve all records. This visualisation is entirely independent
of the Spark application so you can open the file directly in your browser to see it work. The second visualisation is a spreadsheet,
`src/main/resources/public/chart.html`.

## `weather-avg`

The next micro-service, `weather-avg`, is one that makes calls to `weather-data` in order to do its work. There is no data to load, so after reading the code
run the `main` method in the class `ci646.weatheravg.Application`. This starts the webservice running at the address `http://localhost:5678`. This is the same
address as the previous service but uses a different port so you can run them simultaneously (in fact, you have to, since `weather-avg` depends on
`weather-data`). This webservice returns JSON obects (and arrays of them) with the following data:

+ a location,
+ a starting point, which is a timestamp in the format given above,
+ an end point, and
+ the average temperature in that time.

The webservice has the following endpoints:

| Endpoint | Verb | Description |
| `/<locid>` | `GET` | Returns the average temperature over all records made at the location with id `<locid>`. |
| `/<locid>/<year>/<month>` | `GET` | Returns the average temperature over all records made at the location with id `<locid>` in the year `<year>` (four digits) and month `<month>` (two digits). |
| `/<locid>/<year>/<month>/<day>` | `GET` | Returns the average temperature over all records made at the location with id `<locid>` in the year `<year>` (four digits), month `<month>` (two digits) and day `<day>` (two digits). |

Experiment with calling the endpoints from the commandline:

```
$ curl .........
```

## Exercise

Currently, `weather-avg` only supplies average values for temperature records. Extend this service to return average values for humidity, wind speed and wind
direction. You will need to add a token to the endpoints to specify which type of value is required, e.g. adding an extra paremeter that must be one
of `TE`, `HU`, `WS` or `WD`. You will also need to extend the helper methods `recordsToAverage` and `averageForRange`.
Remember to set the `TYPE` field in the instance of `Average` before returning it.