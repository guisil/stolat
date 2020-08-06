# StoLat Service

A RESTful service which fetches the albums with the birthday within a specified period (month/day).

It depends on the [StoLat Model](https://github.com/guisil/stolat/tree/develop/stolat-model) and on the [StoLat DAO](https://github.com/guisil/stolat/tree/develop/stolat-dao) modules and it requires the 'stolat' database schema to be populated (otherwise no results are retrieved), so the [StoLat Boostrap application](https://github.com/guisil/stolat/tree/develop/stolat-bootstrap) needs to be executed beforehand.

## Calling the service

The service path is `/stolat/birthdays` and it fetches the albums included in the local collection which have their birthday in the specified period (if any). Examples (assuming the current day is August the 6th):
 
 * `http://host:port/stolat/birthdays` fetches the birthday albums for the current day (August the 6th)
 * `http://host:port/stolat/birthdays?from=--08-01` fetches the birthday albums between August the 1st and the current day (August the 6th)
 * `http://host:port/stolat/birthdays?to=--08-31` fetches the birthday albums between the current day (August the 6th) and August the 31st
 * `http://host:port/stolat/birthdays?from=--08-01&to=--08-31` fetches the birthday albums between August the 1st and August the 31st

## Configuration

An example [configuration file](https://github.com/guisil/stolat/blob/develop/stolat-service/src/main/resources/application.properties.example) is provided. The "spring.datasource" properties are the ones that need to be changed.