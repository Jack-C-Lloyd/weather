# Weather micro-service

```
$ curl -H "Accept: application/json" -d "the_url=http://brighton.ac.uk" http://localhost:4567/
{ "status": "SUCCESS", "url": "http://brighton.ac.uk", "enc": "be6ce4cd" }

$ curl -H "Accept: application/json" --request POST -d \
  "name=Brighton&lat=50.2225&lon=0.1372&asl=0" http://localhost:4567/locations
{}

$ curl -H "Accept: application/json" --request POST -d \
"ts=2020-12-07T00:00&temp=5.0&hum=9&ws=42&wd=99" http://localhost:4567/records/2
{"recordID":198,"locID":2,"timestamp":"Dec 7, 2020, 12:00:00 AM",
"temperature":5.0,"humidity":9.0,"windSpeed":42.0,"windDirection":99.0}
```