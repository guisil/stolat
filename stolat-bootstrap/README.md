# StoLat Bootstrap

Command line application (based on Picocli and Spring Boot) which provides functionality for bootstrapping the StoLat database.

It depends on the [StoLat Model](https://github.com/guisil/stolat/tree/develop/stolat-model) and on the [StoLat DAO](https://github.com/guisil/stolat/tree/develop/stolat-dao) modules.

For this to make sense, an instance of the [MusicBrainz database](https://musicbrainz.org/doc/MusicBrainz_Database) should be in place. There are several ways to achieve this. One of the simplest options is probably to set up the whole [MusicBrainz Server](https://musicbrainz.org/doc/MusicBrainz_Server), although most of its infrastructure is not necessary for this purpose. To set up only the database, the [mbdata project](https://github.com/lalinsky/mbdata/) is a good option (including replication).

## Execution

The 'stolat' database schema will be created and updated to the correct version (using Flyway) when running the application, independently of the passed options. 

The default execution (without any option) populates the album birthday data and updates the album/track collection. 

The following options are available:

* `-b` or `--album-birthday` populates the album birthday data
* `-c` or `--album-collection` populates the track/album collection with the music files found within the configured source folder
* `-t` or `--truncate` truncates the tables before populating them (only relevant for the `-c` option)
* `-f` or `--force` forces an update if an album/track already exists in the database (only relevant for the `-c` option)
* `-p` or `--path` (followed by a path) overrides the configured path for the source folder (only relevant for the `-c` option)

## Configuration

An example [configuration file](https://github.com/guisil/stolat/blob/develop/stolat-bootstrap/src/main/resources/application.properties.example) is provided. The "spring.datasource" and the "filesystem" properties are the ones that need to be changed.