package stolat.dao;

import org.springframework.jdbc.core.RowMapper;
import stolat.model.Album;
import stolat.model.AlbumBirthday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static stolat.dao.StolatDatabaseConstants.*;

public class AlbumBirthdayRowMapper implements RowMapper<AlbumBirthday> {

    @Override
    public AlbumBirthday mapRow(ResultSet resultSet, int i) throws SQLException {
        final UUID albumMbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
        final String albumName = resultSet.getString(ALBUM_NAME_COLUMN);
        final UUID artistMbid = resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class);
        final String artistName = resultSet.getString(ARTIST_NAME_COLUMN);
        final int albumYear = resultSet.getInt(ALBUM_YEAR_COLUMN);
        final int albumMonth = resultSet.getInt(ALBUM_MONTH_COLUMN);
        final int albumDay = resultSet.getInt(ALBUM_DAY_COLUMN);
        return new AlbumBirthday(
                new Album(albumMbid, albumName, artistMbid, artistName),
                albumYear, albumMonth, albumDay);
    }
}
