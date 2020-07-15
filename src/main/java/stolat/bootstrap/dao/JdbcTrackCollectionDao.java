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

@Repository
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    private static final String SCHEMA_NAME = "stolat";
    private static final String ALBUM_TABLE_NAME = "local_collection_album";
    private static final String TRACK_TABLE_NAME = "local_collection_track";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void clearTrackCollection() {
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME + " CASCADE");
    }

    @Override
    public void populateTrackCollection(Set<Track> trackCollection, boolean force) {
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
                .append("stolat.local_collection_album")
                .append(" VALUES (:album_mbid, :album_name, :album_source, :artist_mbid, :artist_name, :last_updated)")
                .append(" ON CONFLICT (album_mbid) ")
                .append( getAlbumOnConflictStatement(album, force))
                .toString();
    }

    private String getAlbumOnConflictStatement(Album album, boolean force) {
        if (force) {
            return "DO UPDATE SET" +
                    " album_mbid = :album_mbid," +
                    " album_name = :album_name," +
                    " album_source = :album_source," +
                    " artist_mbid = :artist_mbid," +
                    " artist_name = :artist_name," +
                    " last_updated = :last_updated";
        } else {
            return "DO NOTHING";
        }
    }

    private SqlParameterSource getAlbumInsertNamedParameters(Album album) {
        return new MapSqlParameterSource()
                .addValue("album_mbid", album.getAlbumMusicBrainzId(), Types.OTHER, "uuid")
                .addValue("album_name", album.getAlbumName())
                .addValue("album_source", "local")
                .addValue("artist_mbid", album.getArtistMusicBrainzId(), Types.OTHER, "uuid")
                .addValue("artist_name", album.getArtistName())
                .addValue("last_updated", Timestamp.from(Instant.now()));
    }

    private String getTrackInsertStatement(Track track, boolean force) {
        return new StringBuilder()
                .append("INSERT INTO ")
                .append("stolat.local_collection_track")
                .append(" VALUES (:track_mbid, :disc_number, :track_number, :track_name, :track_length, :track_file_type, :track_path, :album_mbid, :last_updated)")
                .append(" ON CONFLICT (track_mbid) ")
                .append( getTrackOnConflictStatement(force))
                .toString();
    }

    private String getTrackOnConflictStatement(boolean force) {
        if (force) {
            return "DO UPDATE SET" +
                    " track_mbid = :track_mbid," +
                    " disc_number = :disc_number," +
                    " track_number = :track_number," +
                    " track_name = :track_name," +
                    " track_length = :track_length," +
                    " track_file_type = :track_file_type," +
                    " track_path = :track_path," +
                    " album_mbid = :album_mbid," +
                    " last_updated = :last_updated";
        } else {
            return "DO NOTHING";
        }
    }

    private SqlParameterSource getTrackInsertNamedParameters(Track track) {
        return new MapSqlParameterSource()
                .addValue("track_mbid", track.getTrackMusicBrainzId())
                .addValue("disc_number", track.getDiscNumber())
                .addValue("track_number", track.getTrackNumber())
                .addValue("track_name", track.getTrackName())
                .addValue("track_length", track.getTrackLength())
                .addValue("track_file_type", track.getTrackFileType())
                .addValue("track_path", track.getTrackRelativePath())
                .addValue("album_mbid", track.getAlbum().getAlbumMusicBrainzId())
                .addValue("last_updated", Timestamp.from(Instant.now()));
    }
}
