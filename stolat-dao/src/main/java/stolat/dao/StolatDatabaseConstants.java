package stolat.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StolatDatabaseConstants {

    public static final String SCHEMA_NAME = "stolat";
    public static final String BIRTHDAY_TABLE_NAME = "album_birthday";
    public static final String BIRTHDAY_TABLE_INTERMEDIATE_NAME = "album_birthday_intermediate";
    public static final String BIRTHDAY_TABLE_FULL_NAME = SCHEMA_NAME + "." + BIRTHDAY_TABLE_NAME;
    public static final String BIRTHDAY_TABLE_INTERMEDIATE_FULL_NAME = SCHEMA_NAME + "." + BIRTHDAY_TABLE_INTERMEDIATE_NAME;
    public static final String ALBUM_TABLE_NAME = "local_collection_album";
    public static final String ALBUM_TABLE_FULL_NAME = SCHEMA_NAME + "." + ALBUM_TABLE_NAME;
    public static final String TRACK_TABLE_NAME = "local_collection_track";
    public static final String TRACK_TABLE_FULL_NAME = SCHEMA_NAME + "." + TRACK_TABLE_NAME;

    public static final String ALBUM_MBID_COLUMN = "album_mbid";
    public static final String ALBUM_NAME_COLUMN = "album_name";
    public static final String ALBUM_SOURCE_COLUMN = "album_source";
    public static final String ARTIST_MBID_COLUMN = "artist_mbid";
    public static final String ARTIST_NAME_COLUMN = "artist_name";

    public static final String ALBUM_YEAR_COLUMN = "album_year";
    public static final String ALBUM_MONTH_COLUMN = "album_month";
    public static final String ALBUM_DAY_COLUMN = "album_day";

    public static final String TRACK_MBID_COLUMN = "track_mbid";
    public static final String DISC_NUMBER_COLUMN = "disc_number";
    public static final String TRACK_NUMBER_COLUMN = "track_number";
    public static final String TRACK_NAME_COLUMN = "track_name";
    public static final String TRACK_LENGTH_COLUMN = "track_length";
    public static final String TRACK_FILE_TYPE_COLUMN = "track_file_type";
    public static final String TRACK_PATH_COLUMN = "track_path";

    public static final String LAST_UPDATED_COLUMN = "last_updated";

    public static final String LOCAL_ALBUM_SOURCE = "local";
    public static final String MBID_SQL_TYPE = "uuid";
}
