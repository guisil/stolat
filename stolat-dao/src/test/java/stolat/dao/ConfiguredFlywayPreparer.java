package stolat.dao;

import io.zonky.test.db.postgres.embedded.DatabasePreparer;
import io.zonky.test.db.postgres.embedded.FlywayPreparer;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link DatabasePreparer}, mostly based on
 * {@link FlywayPreparer}, but accepting a few more configuration parameters.
 */
public class ConfiguredFlywayPreparer implements DatabasePreparer {
    private final FluentConfiguration flyway;
    private final List<String> locations;
    private final List<String> schemas;
    private final String defaultScheam;

    private ConfiguredFlywayPreparer(
            FluentConfiguration flyway, List<String> locations,
            List<String> schemas, String defaultSchema) {
        this.flyway = flyway;
        this.locations = locations;
        this.schemas = schemas;
        this.defaultScheam = defaultSchema;
    }

    public static ConfiguredFlywayPreparer forClasspathLocationAndSchemas(
            List<String> locations, List<String> schemas, String defaultSchema) {
        FluentConfiguration f = Flyway.configure()
                .locations(locations.toArray(String[]::new))
                .schemas(schemas.toArray(String[]::new))
                .defaultSchema(defaultSchema);
        return new ConfiguredFlywayPreparer(f, locations, schemas, defaultSchema);
    }

    @Override
    public void prepare(DataSource ds) throws SQLException {
        flyway.dataSource(ds);
        flyway.load().migrate();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConfiguredFlywayPreparer)) {
            return false;
        }
        return Objects.equals(locations, ((ConfiguredFlywayPreparer) obj).locations)
                && Objects.equals(schemas, ((ConfiguredFlywayPreparer) obj).schemas)
                && Objects.equals(defaultScheam, ((ConfiguredFlywayPreparer) obj).defaultScheam);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(locations);
    }
}
