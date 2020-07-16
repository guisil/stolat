package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Set;
import java.util.StringJoiner;

import static stolat.bootstrap.dao.DatabaseConstants.*;

@Repository
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void clearTrackCollection() {
        log.info("Clearing album/track collection");
        jdbcTemplate.execute("TRUNCATE TABLE " + ALBUM_TABLE_FULL_NAME + " CASCADE");
    }

    @Override
    public void populateTrackCollection(Set<Track> trackCollection, boolean force) {
        log.info("Populating album/track collection{}", force ? " (forcing update)" : "");
        trackCollection.forEach(track -> {
            namedParameterJdbcTemplate.update(
                    getAlbumInsertStatement(track.getAlbum(), force),
                    getAlbumInsertNamedParameters(track.getAlbum()));
            namedParameterJdbcTemplate.update(
                    getTrackInsertStatement(track, force),
                    getTrackInsertNamedParameters(track));
        });
    }

    private String getAlbumInsertStatement(Album album, boolean force) {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(ALBUM_TABLE_FULL_NAME)
                .append(" VALUES ").append(getAlbumInsertValues())
                .append(" ON CONFLICT (").append(ALBUM_MBID_COLUMN).append(") ")
                .append(getAlbumOnConflictStatement(album, force))
                .toString();
    }

    private String getAlbumInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + ALBUM_MBID_COLUMN)
                .add(":" + ALBUM_NAME_COLUMN)
                .add(":" + ALBUM_SOURCE_COLUMN)
                .add(":" + ARTIST_MBID_COLUMN)
                .add(":" + ARTIST_NAME_COLUMN)
                .add(":" + LAST_UPDATED_COLUMN)
                .toString();
    }

    private String getAlbumOnConflictStatement(Album album, boolean force) {
        if (force) {
            return "DO UPDATE SET " +
                    new StringJoiner(",")
                            .add(ALBUM_MBID_COLUMN + "=:" + ALBUM_MBID_COLUMN)
                            .add(ALBUM_NAME_COLUMN + "=:" + ALBUM_NAME_COLUMN)
                            .add(ALBUM_SOURCE_COLUMN + "=:" + ALBUM_SOURCE_COLUMN)
                            .add(ARTIST_MBID_COLUMN + "=:" + ARTIST_MBID_COLUMN)
                            .add(ARTIST_NAME_COLUMN + "=:" + ARTIST_NAME_COLUMN)
                            .add(LAST_UPDATED_COLUMN + "=:" + LAST_UPDATED_COLUMN)
                            .toString();
        } else {
            return "DO NOTHING";
        }
    }

    private SqlParameterSource getAlbumInsertNamedParameters(Album album) {
        return new MapSqlParameterSource()
                .addValue(ALBUM_MBID_COLUMN, album.getAlbumMusicBrainzId(), Types.OTHER, MBID_SQL_TYPE)
                .addValue(ALBUM_NAME_COLUMN, album.getAlbumName())
                .addValue(ALBUM_SOURCE_COLUMN, LOCAL_ALBUM_SOURCE)
                .addValue(ARTIST_MBID_COLUMN, album.getArtistMusicBrainzId(), Types.OTHER, MBID_SQL_TYPE)
                .addValue(ARTIST_NAME_COLUMN, album.getArtistName())
                .addValue(LAST_UPDATED_COLUMN, Timestamp.from(Instant.now()));
    }

    private String getTrackInsertStatement(Track track, boolean force) {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(TRACK_TABLE_FULL_NAME)
                .append(" VALUES ").append(getTrackInsertValues())
                .append(" ON CONFLICT (").append(TRACK_MBID_COLUMN).append(") ")
                .append(getTrackOnConflictStatement(force))
                .toString();
    }

    private String getTrackInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + TRACK_MBID_COLUMN)
                .add(":" + DISC_NUMBER_COLUMN)
                .add(":" + TRACK_NUMBER_COLUMN)
                .add(":" + TRACK_NAME_COLUMN)
                .add(":" + TRACK_LENGTH_COLUMN)
                .add(":" + TRACK_FILE_TYPE_COLUMN)
                .add(":" + TRACK_PATH_COLUMN)
                .add(":" + ALBUM_MBID_COLUMN)
                .add(":" + LAST_UPDATED_COLUMN)
                .toString();
    }

    private String getTrackOnConflictStatement(boolean force) {
        if (force) {
            return "DO UPDATE SET " +
                    new StringJoiner(",")
                            .add(TRACK_MBID_COLUMN + "=:" + TRACK_MBID_COLUMN)
                            .add(DISC_NUMBER_COLUMN + "=:" + DISC_NUMBER_COLUMN)
                            .add(TRACK_NUMBER_COLUMN + "=:" + TRACK_NUMBER_COLUMN)
                            .add(TRACK_NAME_COLUMN + "=:" + TRACK_NAME_COLUMN)
                            .add(TRACK_LENGTH_COLUMN + "=:" + TRACK_LENGTH_COLUMN)
                            .add(TRACK_FILE_TYPE_COLUMN + "=:" + TRACK_FILE_TYPE_COLUMN)
                            .add(TRACK_PATH_COLUMN + "=:" + TRACK_PATH_COLUMN)
                            .add(ALBUM_MBID_COLUMN + "=:" + ALBUM_MBID_COLUMN)
                            .add(LAST_UPDATED_COLUMN + "=:" + LAST_UPDATED_COLUMN)
                            .toString();
        } else {
            return "DO NOTHING";
        }
    }

    private SqlParameterSource getTrackInsertNamedParameters(Track track) {
        return new MapSqlParameterSource()
                .addValue(TRACK_MBID_COLUMN, track.getTrackMusicBrainzId(), Types.OTHER, MBID_SQL_TYPE)
                .addValue(DISC_NUMBER_COLUMN, track.getDiscNumber())
                .addValue(TRACK_NUMBER_COLUMN, track.getTrackNumber())
                .addValue(TRACK_NAME_COLUMN, track.getTrackName())
                .addValue(TRACK_LENGTH_COLUMN, track.getTrackLength())
                .addValue(TRACK_FILE_TYPE_COLUMN, track.getTrackFileType())
                .addValue(TRACK_PATH_COLUMN, track.getTrackRelativePath())
                .addValue(ALBUM_MBID_COLUMN, track.getAlbum().getAlbumMusicBrainzId(), Types.OTHER, MBID_SQL_TYPE)
                .addValue(LAST_UPDATED_COLUMN, Timestamp.from(Instant.now()));
    }
}
