ALTER TABLE album_birthdays ADD COLUMN discogs_id BIGINT;
CREATE INDEX idx_album_birthdays_discogs_id ON album_birthdays(discogs_id) WHERE discogs_id IS NOT NULL;
