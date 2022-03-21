package stolat.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import stolat.model.Album;
import stolat.model.AlbumBirthday;
import stolat.model.Artist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static stolat.dao.StolatDatabaseConstants.*;

public class AlbumBirthdayListExtractor implements ResultSetExtractor<List<AlbumBirthday>> {

    @Override
    public List<AlbumBirthday> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        List<AlbumBirthday> birthdays = new ArrayList<>();
        UUID currentAlbumMbid = null;
        String currentAlbumName = null;
        String currentAlbumArtistDisplayName = null;
        int currentAlbumYear = -1;
        int currentAlbumMonth = -1;
        int currentAlbumDay = -1;
        List<Artist> currentArtists = new ArrayList<>();
        while (resultSet.next()) {
            UUID albumMbid = resultSet.getObject(ALBUM_MBID_COLUMN, UUID.class);
            String albumName = resultSet.getString(ALBUM_NAME_COLUMN);
            String albumArtistDisplayName = resultSet.getString(ALBUM_ARTIST_DISPLAY_NAME_COLUMN);
            int albumYear = resultSet.getInt(ALBUM_YEAR_COLUMN);
            int albumMonth = resultSet.getInt(ALBUM_MONTH_COLUMN);
            int albumDay = resultSet.getInt(ALBUM_DAY_COLUMN);
            Artist artist = new Artist(
                    resultSet.getObject(ARTIST_MBID_COLUMN, UUID.class),
                    resultSet.getString(ARTIST_NAME_COLUMN));
            if (!albumMbid.equals(currentAlbumMbid)) {
                if (currentAlbumMbid != null) { // new object
                    birthdays.add(new AlbumBirthday(
                            new Album(currentAlbumMbid, currentAlbumName, currentArtists, currentAlbumArtistDisplayName),
                            currentAlbumYear, currentAlbumMonth, currentAlbumDay));
                }
                currentAlbumMbid = albumMbid;
                currentAlbumName = albumName;
                currentAlbumArtistDisplayName = albumArtistDisplayName;
                currentAlbumYear = albumYear;
                currentAlbumMonth = albumMonth;
                currentAlbumDay = albumDay;
                currentArtists = new ArrayList<>();
            }
            currentArtists.add(artist);
        }
        if (currentAlbumMbid != null) { // last object
            birthdays.add(new AlbumBirthday(
                    new Album(currentAlbumMbid, currentAlbumName, currentArtists, currentAlbumArtistDisplayName),
                    currentAlbumYear, currentAlbumMonth, currentAlbumDay));
        }

        return birthdays;
    }
}
