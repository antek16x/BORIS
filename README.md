# BORIS (BOrder cRossIng System)

## Application to track vehicles and recognize when they cross the border

#### Important information

The application has 2 predefined profiles (prod and dev) whose settings are contained in the application-prod.yml and
application-dev.yml files, respectively.
The prod profile runs on a PostgreSQL database and the dev profile uses an H2 database. The application has been
equipped with a mechanism that asks an external
service about the position of a given vehicle every 5 minutes. The external service is only theoretical and the path to
it is in the application.yml file, preceded
by the prefix `"spring.webflux.client.baseUrl"`. The application has also been equipped with REST-API, API documentation
with "Try it out" mode can be found at
`localhost:8080/docs/index.html` after starting the application.

#### Additional information

The ```getVehiclePosition``` method of an external service takes a parameter ```String vehicleReg``` (vehicle
registration plate) which is passed to the URL path. We assume that the GET request should return JSON matching the
example below.

```json
{
  "position": {
    "coordinate": {
      "longitude": "Double",
      "latitude": "Double"
    },
    "country": "String (ISO 3166-1 alfa-3)",
    "timestamp": "Instant"
  }
}
```

#### Installation

For proper operation of the application you need `docker` and `docker-compose.yml` files that are in the repository.
There are 2 files in the repository:

* `docker-compose-dev.yml` - the file only runs the AxonServer container
* `docker-compose-prod.yml` - the file runs AxonServer and PostgreSQL container

To run these files just go to the path where they are located and execute the command

```bash
docker compose -f name_of_one_of_the_two_files up
```
