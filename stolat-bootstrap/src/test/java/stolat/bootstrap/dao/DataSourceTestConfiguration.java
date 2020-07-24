package stolat.bootstrap.dao;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import stolat.dao.AlbumBirthdayDao;
import stolat.dao.JdbcAlbumBirthdayDao;
import stolat.dao.JdbcTrackCollectionDao;
import stolat.dao.TrackCollectionDao;

import javax.sql.DataSource;

@ActiveProfiles("jdbc")
@TestConfiguration
public class DataSourceTestConfiguration {

    @Autowired
    private DataSource dataSource;

    @Bean
    JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .locations("classpath:/db/migration")
                .defaultSchema("stolat")
                .schemas("stolat", "musicbrainz")
                .load();
    }

    @Bean
    AlbumBirthdayDao albumBirthdayDao() {
        return new JdbcAlbumBirthdayDao(
                jdbcTemplate());
    }

    @Bean
    TrackCollectionDao trackCollectionDao() {
        return new JdbcTrackCollectionDao(
                jdbcTemplate(),
                namedParameterJdbcTemplate());
    }
}
