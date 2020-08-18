package stolat.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DaoConstants {

    public static final String CLEAR_ALBUM_BIRTHDAY_SCRIPT = "clear_album_birthday_table.sql";
    public static final String POPULATE_ALBUM_BIRTHDAY_INTERMEDIATE_SCRIPT = "populate_album_birthday_intermediate_table.sql";
    public static final String POPULATE_ALBUM_BIRTHDAY_SCRIPT = "populate_album_birthday_table.sql";
}
