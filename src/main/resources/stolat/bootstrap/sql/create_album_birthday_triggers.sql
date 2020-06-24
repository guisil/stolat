/* Function which inserts/updates the row in the 'album_birthday' table
 * corresponding to the given album (release_group) musicbrainz ID
 * with the newly calculated earliest release date.
 */

CREATE OR REPLACE FUNCTION upsert_album_birthday(current_album_mbid UUID)
    RETURNS void AS $$
    BEGIN
    
        INSERT INTO album_birthday
            SELECT DISTINCT ON (album_mbid)
            *
            FROM (
                SELECT
                    release_group.gid AS album_mbid,
                    artist.gid AS artist_mbid,
                    release_country.date_year AS album_year,
                    release_country.date_month AS album_month,
                    release_country.date_day AS album_day,
                    now() AS last_updated
                FROM
                    release,
                    release_group,
                    release_country,
                    artist
                WHERE release.id = release_country.release
                AND release.release_group = release_group.id
                AND artist.id = release_group.artist_credit
                AND release_group.gid = current_album_mbid
            UNION
                SELECT release_group.gid AS album_mbid,
                    artist.gid AS artist_mbid,
                    release_unknown_country.date_year AS album_year,
                    release_unknown_country.date_month AS album_month,
                    release_unknown_country.date_day AS album_day,
                    now() AS last_updated
                FROM
                    release,
                    release_group,
                    release_unknown_country,
                    artist
                WHERE release.id = release_unknown_country.release
                AND release.release_group = release_group.id
                AND artist.id = release_group.artist_credit
                AND release_group.gid = current_album_mbid
            )
            AS foo
            ORDER BY album_mbid, album_year, album_month, album_day
        ON CONFLICT (album_mbid)
            DO UPDATE SET
                album_year = EXCLUDED.album_year,
                album_month = EXCLUDED.album_month,
                album_day = EXCLUDED.album_day,
                last_updated = EXCLUDED.last_updated;

    END;
$$ LANGUAGE plpgsql;

/* Creates the function to be called by the trigger.
 * It calls the 'upsert_album_birthday' function,
 * passing it the musicbrainz ID of the album involved in the operation.
 */

CREATE OR REPLACE FUNCTION call_upsert_album_birthday()
    RETURNS trigger AS $$
    BEGIN
    
        IF TG_OP = 'DELETE' THEN
            PERFORM upsert_album_birthday((
                SELECT release_group.gid
                FROM
                    release_group,
                    release
                WHERE release.id = OLD.release
                AND release_group.id = release.release_group
            ));
        ELSE
            PERFORM upsert_album_birthday((
            SELECT release_group.gid
                FROM
                    release_group,
                    release
                WHERE release.id = NEW.release
                AND release_group.id = release.release_group
            ));
        END IF;
        
        RETURN NEW;
        
    END;
$$ LANGUAGE plpgsql;

/* Creates the trigger for the 'release_country' table. */

CREATE TRIGGER trigger_call_upsert_album_birthday_country
    AFTER INSERT OR UPDATE OR DELETE ON release_country
        FOR EACH ROW EXECUTE FUNCTION call_upsert_album_birthday();

/* Creates the trigger for the 'release_unknown_country' table. */

CREATE TRIGGER trigger_call_upsert_album_birthday_unknown_country
    AFTER INSERT OR UPDATE OR DELETE ON release_unknown_country
        FOR EACH ROW EXECUTE FUNCTION call_upsert_album_birthday();


/* Creates the trigger function and trigger to be called each time a release_group is removed,
 * removing the corresponding entry from the 'album_birthday' table.
 */
 
CREATE OR REPLACE FUNCTION call_remove_album_birthday_for_release_group()
    RETURNS trigger AS $$
    BEGIN
    
        DELETE FROM album_birthday
        WHERE album_mbid = OLD.gid;

        RETURN NEW;
        
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_call_remove_album_birthday_for_release_group
    BEFORE DELETE ON release_group
        FOR EACH ROW EXECUTE FUNCTION call_remove_album_birthday_for_release_group();


/* Creates the trigger function and trigger to be called each time an artist is removed,
 * removing the corresponding entries from the 'album_birthday' table.
 */

CREATE OR REPLACE FUNCTION call_remove_album_birthday_for_artist()
    RETURNS trigger AS $$
    BEGIN
    
        DELETE FROM album_birthday
        WHERE artist_mbid = OLD.gid;

        RETURN NEW;
        
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_call_remove_album_birthday_for_artist
    BEFORE DELETE ON artist
        FOR EACH ROW EXECUTE FUNCTION call_remove_album_birthday_for_artist();