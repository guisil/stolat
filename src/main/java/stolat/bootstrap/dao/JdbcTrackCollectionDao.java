package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.model.Track;

import java.time.Clock;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    private static final String SCHEMA_NAME = "stolat";
    private static final String ALBUM_TABLE_NAME = "local_collection_album";
    private static final String TRACK_TABLE_NAME = "local_collection_track";

//    @Autowired
//    private Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void clearTrackCollection() {
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA_NAME + "." + ALBUM_TABLE_NAME + " CASCADE");
    }

    @Override
    public void populateTrackCollection(Set<Track> trackCollection, boolean force) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
