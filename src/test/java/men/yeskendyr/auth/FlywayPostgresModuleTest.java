package men.yeskendyr.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;
import org.junit.jupiter.api.Test;

class FlywayPostgresModuleTest {

    @Test
    void postgresDatabaseTypeIsAvailable() {
        assertThat(PostgreSQLDatabaseType.class).isNotNull();
    }
}
