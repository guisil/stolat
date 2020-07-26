-- ALBUM BIRTHDAY

CREATE TABLE stolat.album_birthday (
    album_mbid UUID PRIMARY KEY,
    album_name VARCHAR NOT NULL,
    artist_mbid UUID NOT NULL,
    artist_name VARCHAR NOT NULL,
    album_year INTEGER,
    album_month INTEGER,
    album_day INTEGER,
    last_updated TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_album_birthday_month
    ON album_birthday(album_month, album_day);

-- ALBUM COLLECTION

CREATE TABLE stolat.local_collection_album (
    album_mbid UUID PRIMARY KEY,
    album_name VARCHAR NOT NULL,
    album_source VARCHAR NOT NULL,
    artist_mbid UUID NOT NULL,
    artist_name VARCHAR NOT NULL,
    last_updated TIMESTAMP
);

CREATE TABLE stolat.local_collection_track (
    track_mbid UUID PRIMARY KEY,
    disc_number INTEGER NOT NULL,
    track_number INTEGER NOT NULL,
    track_name VARCHAR NOT NULL,
    track_length INTEGER NOT NULL,
    track_file_type VARCHAR NOT NULL,
    track_path VARCHAR NOT NULL,
    album_mbid UUID REFERENCES local_collection_album(album_mbid),
    last_updated TIMESTAMP
);