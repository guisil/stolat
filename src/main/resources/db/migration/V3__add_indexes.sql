CREATE INDEX idx_albums_musicbrainz_id ON albums(musicbrainz_id);
CREATE INDEX idx_albums_discogs_id ON albums(discogs_id);
CREATE INDEX idx_artists_musicbrainz_id ON artists(musicbrainz_id);
CREATE INDEX idx_album_birthdays_musicbrainz_id ON album_birthdays(musicbrainz_id);
