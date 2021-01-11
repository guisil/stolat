INSERT INTO stolat.album_birthday
SELECT
    DISTINCT ON (album_mbid)
    *
FROM (
    SELECT
        album_mbid,
        album_year,
        album_month,
        album_day,
        now() AS last_updated
    FROM
        stolat.album_birthday_intermediate
)
AS birthdays
ORDER BY album_mbid, album_year, album_month, album_day;