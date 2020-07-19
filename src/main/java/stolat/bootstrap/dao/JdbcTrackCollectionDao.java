package stolat.bootstrap.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.model.Album;
import stolat.bootstrap.model.Track;

import java.sql.Types;
import java.util.List;
import java.util.StringJoiner;

import static stolat.bootstrap.dao.StolatDatabaseConstants.*;

@Profile("jdbc")
@Repository
@AllArgsConstructor
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void clearTrackCollection() {
        log.info("Clearing album/track collection");
        jdbcTemplate.update("TRUNCATE TABLE " + ALBUM_TABLE_FULL_NAME + " CASCADE");
    }

    @Override
    public void updateTrackCollection(List<Track> trackBatch, boolean force) {
        log.info("Populating album/track collection{}", force ? " (forcing update)" : "");
        namedParameterJdbcTemplate.batchUpdate(
                getAlbumInsertStatement(force),
                trackBatch.stream()
                        .map(Track::getAlbum)
                        .distinct()
                        .map(this::getAlbumInsertNamedParameters)
                        .toArray(MapSqlParameterSource[]::new));
        namedParameterJdbcTemplate.batchUpdate(
                getTrackInsertStatement(force),
                trackBatch.stream()
                        .map(this::getTrackInsertNamedParameters)
                        .toArray(MapSqlParameterSource[]::new));
    }

    private String getAlbumInsertStatement(boolean force) {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(ALBUM_TABLE_FULL_NAME)
                .append(" VALUES ").append(getAlbumInsertValues())
                .append(" ON CONFLICT (").append(ALBUM_MBID_COLUMN).append(") ")
                .append(getAlbumOnConflictStatement(force))
                .toString();
    }

    private String getAlbumInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + ALBUM_MBID_COLUMN)
                .add(":" + ALBUM_NAME_COLUMN)
                .add(":" + ALBUM_SOURCE_COLUMN)
                .add(":" + ARTIST_MBID_COLUMN)
                .add(":" + ARTIST_NAME_COLUMN)
                .add("now()")
                .toString();
    }

    private String getAlbumOnConflictStatement(boolean force) {
        if (force) {
            return "DO UPDATE SET " +
                    new StringJoiner(",")
                            .add(ALBUM_MBID_COLUMN + "=:" + ALBUM_MBID_COLUMN)
                            .add(ALBUM_NAME_COLUMN + "=:" + ALBUM_NAME_COLUMN)
                            .add(ALBUM_SOURCE_COLUMN + "=:" + ALBUM_SOURCE_COLUMN)
                            .add(ARTIST_MBID_COLUMN + "=:" + ARTIST_MBID_COLUMN)
                            .add(ARTIST_NAME_COLUMN + "=:" + ARTIST_NAME_COLUMN)
                            .add(LAST_UPDATED_COLUMN + "=now()")
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
                .addValue(ARTIST_NAME_COLUMN, album.getArtistName());
    }

    private String getTrackInsertStatement(boolean force) {
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
                .add("now()")
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
                            .add(LAST_UPDATED_COLUMN + "=now()")
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
                .addValue(ALBUM_MBID_COLUMN, track.getAlbum().getAlbumMusicBrainzId(), Types.OTHER, MBID_SQL_TYPE);
    }
}
