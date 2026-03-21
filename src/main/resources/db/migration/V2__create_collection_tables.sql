CREATE TABLE artists (
    id              UUID PRIMARY KEY,
    name            VARCHAR(500) NOT NULL,
    musicbrainz_id  UUID,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE albums (
    id              UUID PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    musicbrainz_id  UUID,
    artist_id       UUID NOT NULL REFERENCES artists(id),
    release_date    DATE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tracks (
    id              UUID PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    track_number    INTEGER NOT NULL,
    disc_number     INTEGER NOT NULL,
    musicbrainz_id  UUID,
    album_id        UUID NOT NULL REFERENCES albums(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE album_birthdays (
    id              UUID PRIMARY KEY,
    album_title     VARCHAR(500) NOT NULL,
    artist_name     VARCHAR(500) NOT NULL,
    musicbrainz_id  UUID NOT NULL UNIQUE,
    release_date    DATE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
