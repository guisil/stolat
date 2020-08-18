INSERT INTO stolat.album_birthday_intermediate
    SELECT
        release_group.gid AS album_mbid,
        release_group.name AS album_name,
        artist.gid AS artist_mbid,
        artist.name AS artist_name,
        release_country.date_year AS album_year,
        release_country.date_month AS album_month,
        release_country.date_day AS album_day
    FROM
        musicbrainz.release,
        musicbrainz.release_group,
        musicbrainz.release_country,
        musicbrainz.artist_credit_name,
        musicbrainz.artist
    WHERE release.id = release_country.release
    AND release.release_group = release_group.id
    AND artist.id = artist_credit_name.artist
    AND artist_credit_name.artist_credit = release_group.artist_credit
UNION
    SELECT
        release_group.gid AS album_mbid,
        release_group.name AS album_name,
        artist.gid AS artist_mbid,
        artist.name AS artist_name,
        release_unknown_country.date_year AS album_year,
        release_unknown_country.date_month AS album_month,
        release_unknown_country.date_day AS album_day
    FROM
        musicbrainz.release,
        musicbrainz.release_group,
        musicbrainz.release_unknown_country,
        musicbrainz.artist_credit_name,
        musicbrainz.artist
    WHERE release.id = release_unknown_country.release
    AND release.release_group = release_group.id
    AND artist.id = artist_credit_name.artist
    AND artist_credit_name.artist_credit = release_group.artist_credit;