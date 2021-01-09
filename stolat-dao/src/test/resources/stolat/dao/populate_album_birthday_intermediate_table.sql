INSERT INTO stolat.album_birthday_intermediate
    SELECT
        release_group.gid AS album_mbid,
        release_country.date_year AS album_year,
        release_country.date_month AS album_month,
        release_country.date_day AS album_day
    FROM
        musicbrainz.release,
        musicbrainz.release_group,
        musicbrainz.release_country
    WHERE release.id = release_country.release
    AND release.release_group = release_group.id
UNION
    SELECT
        release_group.gid AS album_mbid,
        release_unknown_country.date_year AS album_year,
        release_unknown_country.date_month AS album_month,
        release_unknown_country.date_day AS album_day
    FROM
        musicbrainz.release,
        musicbrainz.release_group,
        musicbrainz.release_unknown_country
    WHERE release.id = release_unknown_country.release
    AND release.release_group = release_group.id;