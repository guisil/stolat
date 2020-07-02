\set ON_ERROR_STOP 1

BEGIN;

CREATE TABLE local_collection_album (
    album_mbid UUID PRIMARY KEY,
    album_name VARCHAR NOT NULL,
    album_source VARCHAR NOT NULL,
    artist_mbid UUID NOT NULL,
    artist_name VARCHAR NOT NULL,
    last_updated TIMESTAMP
);

CREATE TABLE local_collection_track (
    track_mbid UUID PRIMARY KEY,
    track_number INTEGER NOT NULL,
    track_name VARCHAR NOT NULL,
    track_length INTEGER NOT NULL,
    track_file_type VARCHAR NOT NULL,
    track_path VARCHAR NOT NULL,
    album_mbid UUID REFERENCES local_collection_album(album_mbid),
    last_updated TIMESTAMP
);

COMMIT;