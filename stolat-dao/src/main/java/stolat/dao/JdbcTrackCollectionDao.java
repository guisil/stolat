package stolat.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import stolat.model.Album;
import stolat.model.Artist;
import stolat.model.Track;

import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static stolat.dao.StolatDatabaseConstants.*;

@Profile("jdbc")
@Repository
@AllArgsConstructor
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void clearTrackCollection() {
        log.info("Clearing album/track collection");
        jdbcTemplate.update("TRUNCATE TABLE " + TRACK_TABLE_FULL_NAME + " CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE " + ALBUM_TABLE_FULL_NAME + " CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE " + ARTIST_TABLE_FULL_NAME + " CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE " + ALBUM_ARTIST_TABLE_FULL_NAME + " CASCADE");
    }

    @Override
    public void updateTrackCollection(List<Track> trackBatch) {
        log.info("Populating album/track collection");

        List<Album> albumBatch = trackBatch
                .stream()
                .map(Track::getAlbum)
                .distinct()
                .collect(Collectors.toList());
        List<Artist> artistBatch = trackBatch
                .stream()
                .map(Track::getAlbum)
                .distinct()
                .map(Album::getArtists)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        //insert artists
        namedParameterJdbcTemplate.batchUpdate(
                getArtistInsertStatement(),
                artistBatch
                        .stream()
                        .map(this::getArtistInsertNamedParameters)
                        .toArray(MapSqlParameterSource[]::new));
        //insert albums
        namedParameterJdbcTemplate.batchUpdate(
                getAlbumInsertStatement(),
                albumBatch
                        .stream()
                        .map(this::getAlbumInsertNamedParameters)
                        .toArray(MapSqlParameterSource[]::new));
        //remove and insert album-artist connections
        albumBatch.forEach(album -> namedParameterJdbcTemplate.batchUpdate(
                    getAlbumArtistDeleteStatement(),
                    new MapSqlParameterSource[] {getAlbumArtistDeleteNamedParameters(album)}));
        albumBatch
                .forEach(album -> {
                    namedParameterJdbcTemplate.batchUpdate(
                            getAlbumArtistInsertStatement(),
                            IntStream.range(0, album.getArtists().size())
                                    .mapToObj(i -> {
                                    	Artist artist = album.getArtists().get(i);
                                    	return getAlbumArtistInsertNamedParameters(album, artist, i);
                                    })
                                    .toArray(MapSqlParameterSource[]::new));
                });
        //insert tracks
        namedParameterJdbcTemplate.batchUpdate(
                getTrackInsertStatement(),
                trackBatch.stream()
                        .map(this::getTrackInsertNamedParameters)
                        .toArray(MapSqlParameterSource[]::new));
    }

    private String getArtistInsertStatement() {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(ARTIST_TABLE_FULL_NAME)
                .append(" VALUES ").append(getArtistInsertValues())
                .append(" ON CONFLICT (").append(ARTIST_MBID_COLUMN).append(") ")
                .append(getArtistOnConflictStatement())
                .toString();
    }

    private String getArtistInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + ARTIST_MBID_COLUMN)
                .add(":" + ARTIST_NAME_COLUMN)
                .add("now()")
                .toString();
    }

    private String getArtistOnConflictStatement() {
        return "DO UPDATE SET " +
                new StringJoiner(",")
                        .add(ARTIST_MBID_COLUMN + "=:" + ARTIST_MBID_COLUMN)
                        .add(ARTIST_NAME_COLUMN + "=:" + ARTIST_NAME_COLUMN)
                        .add(LAST_UPDATED_COLUMN + "=now()")
                        .toString();
    }

    private SqlParameterSource getArtistInsertNamedParameters(Artist artist) {
        return new MapSqlParameterSource()
                .addValue(ARTIST_MBID_COLUMN, artist.getArtistMbId(), Types.OTHER, UUID_SQL_TYPE)
                .addValue(ARTIST_NAME_COLUMN, artist.getArtistName());
    }

    private String getAlbumInsertStatement() {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(ALBUM_TABLE_FULL_NAME)
                .append(" VALUES ").append(getAlbumInsertValues())
                .append(" ON CONFLICT (").append(ALBUM_MBID_COLUMN).append(") ")
                .append(getAlbumOnConflictStatement())
                .toString();
    }

    private String getAlbumInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + ALBUM_MBID_COLUMN)
                .add(":" + ALBUM_NAME_COLUMN)
                .add(":" + ALBUM_SOURCE_COLUMN)
                .add("now()")
                .toString();
    }

    private String getAlbumOnConflictStatement() {
        return "DO UPDATE SET " +
                new StringJoiner(",")
                        .add(ALBUM_MBID_COLUMN + "=:" + ALBUM_MBID_COLUMN)
                        .add(ALBUM_NAME_COLUMN + "=:" + ALBUM_NAME_COLUMN)
                        .add(ALBUM_SOURCE_COLUMN + "=:" + ALBUM_SOURCE_COLUMN)
                        .add(LAST_UPDATED_COLUMN + "=now()")
                        .toString();
    }

    private SqlParameterSource getAlbumInsertNamedParameters(Album album) {
        return new MapSqlParameterSource()
                .addValue(ALBUM_MBID_COLUMN, album.getAlbumMbId(), Types.OTHER, UUID_SQL_TYPE)
                .addValue(ALBUM_NAME_COLUMN, album.getAlbumName())
                .addValue(ALBUM_SOURCE_COLUMN, LOCAL_ALBUM_SOURCE);
    }

    private String getAlbumArtistDeleteStatement() {
        return new StringBuilder()
                .append("DELETE FROM ")
                .append(ALBUM_ARTIST_TABLE_FULL_NAME)
                .append(" WHERE ")
                .append(ALBUM_MBID_COLUMN)
                .append("=")
                .append(":").append(ALBUM_MBID_COLUMN)
                .toString();
    }

    private MapSqlParameterSource getAlbumArtistDeleteNamedParameters(Album album) {
        return new MapSqlParameterSource()
                .addValue(ALBUM_MBID_COLUMN, album.getAlbumMbId(), Types.OTHER, UUID_SQL_TYPE);
    }

    private String getAlbumArtistInsertStatement() {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(ALBUM_ARTIST_TABLE_FULL_NAME)
                .append(" VALUES ").append(getAlbumArtistInsertValues())
                .append(" ON CONFLICT ON CONSTRAINT ")
                .append(ALBUM_ARTIST_TABLE_PKEY)
                .append(" DO NOTHING")
                .toString();
    }

    private String getAlbumArtistInsertValues() {
        return new StringJoiner(",", "(", ")")
                .add(":" + ALBUM_MBID_COLUMN)
                .add(":" + ARTIST_MBID_COLUMN)
                .add(":" + ARTIST_POSITION_COLUMN)
                .toString();
    }

    private SqlParameterSource getAlbumArtistInsertNamedParameters(Album album, Artist artist, int artistPosition) {
        return new MapSqlParameterSource()
                .addValue(ALBUM_MBID_COLUMN, album.getAlbumMbId(), Types.OTHER, UUID_SQL_TYPE)
                .addValue(ARTIST_MBID_COLUMN, artist.getArtistMbId())
                .addValue(ARTIST_POSITION_COLUMN, artistPosition);
    }

    private String getTrackInsertStatement() {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append(TRACK_TABLE_FULL_NAME)
                .append(" VALUES ").append(getTrackInsertValues())
                .append(" ON CONFLICT (").append(TRACK_MBID_COLUMN).append(") ")
                .append(getTrackOnConflictStatement())
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

    private String getTrackOnConflictStatement() {
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
    }

    private SqlParameterSource getTrackInsertNamedParameters(Track track) {
        return new MapSqlParameterSource()
                .addValue(TRACK_MBID_COLUMN, track.getTrackMbId(), Types.OTHER, UUID_SQL_TYPE)
                .addValue(DISC_NUMBER_COLUMN, track.getDiscNumber())
                .addValue(TRACK_NUMBER_COLUMN, track.getTrackNumber())
                .addValue(TRACK_NAME_COLUMN, track.getTrackName())
                .addValue(TRACK_LENGTH_COLUMN, track.getTrackLength())
                .addValue(TRACK_FILE_TYPE_COLUMN, track.getTrackFileType())
                .addValue(TRACK_PATH_COLUMN, track.getTrackRelativePath())
                .addValue(ALBUM_MBID_COLUMN, track.getAlbum().getAlbumMbId(), Types.OTHER, UUID_SQL_TYPE);
    }
}
