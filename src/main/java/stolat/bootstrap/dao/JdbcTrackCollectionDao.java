package stolat.bootstrap.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import stolat.bootstrap.model.Track;

import java.util.List;

@Repository
@Slf4j
public class JdbcTrackCollectionDao implements TrackCollectionDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void clearTrackCollection() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void populateTrackCollection(List<Track> trackCollection) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
