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
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
