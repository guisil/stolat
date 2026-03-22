ALTER TABLE album_birthdays ADD COLUMN album_id UUID;
ALTER TABLE album_birthdays ADD COLUMN release_date_source VARCHAR(50);
UPDATE album_birthdays SET release_date_source = 'MUSICBRAINZ';
ALTER TABLE album_birthdays ALTER COLUMN release_date_source SET NOT NULL;
ALTER TABLE album_birthdays ALTER COLUMN musicbrainz_id DROP NOT NULL;
DROP INDEX idx_album_birthdays_musicbrainz_id;
CREATE UNIQUE INDEX idx_album_birthdays_musicbrainz_id ON album_birthdays(musicbrainz_id) WHERE musicbrainz_id IS NOT NULL;
CREATE UNIQUE INDEX idx_album_birthdays_album_id ON album_birthdays(album_id) WHERE album_id IS NOT NULL;
