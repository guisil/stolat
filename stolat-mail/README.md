# StoLat Mail

Email notification service for StoLat.

It depends on the [StoLat Model](https://github.com/guisil/stolat/tree/develop/stolat-model) module and it requires that the [StoLat Service](https://github.com/guisil/stolat/tree/develop/stolat-service) is running.

When executed, it calls the service in the configured path, fetching the albums included in the local collection which have their birthdays in the current day, and produces an email which is sent using the provided mail configuration.

## Configuration

An example [configuration file](https://github.com/guisil/stolat/blob/develop/stolat-mail/src/main/resources/application.properties.example) is provided. The "service", "spring.mail" and "mail" properties are the ones that need to be changed.