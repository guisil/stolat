CREATE TABLE IF NOT EXISTS album_birthday (
    album_mbid UUID PRIMARY KEY REFERENCES release_group(gid),
    artist_mbid UUID NOT NULL REFERENCES artist(gid),
    album_year INTEGER,
    album_month INTEGER,
    album_day INTEGER,
    last_updated TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_album_birthday_month
    ON album_birthday(album_month, album_day);