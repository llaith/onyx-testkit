package org.llaith.onyx.testkit.docker.junit.ext.pgsql;

import org.llaith.onyx.testkit.docker.junit.DockerResource;
import org.llaith.onyx.testkit.util.TestUtil;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class PostgresResource extends DockerResource<PostgresConfig,PostgresResource> {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    private static final Logger logger = getLogger(PostgresResource.class);

    private static Class<?> driver;

    static {

        try {

            driver = Class.forName(POSTGRES_DRIVER);

        } catch (ClassNotFoundException e) {

            logger.error("Failed to load postgres driver", e);

        }

    }

    public static PostgresResource buildWithDefaults() {

        return new PostgresResource(PostgresConfig.builderWithDefaults());

    }

    public PostgresResource(final PostgresConfig config) {

        super(config);

    }

    @Override
    public PostgresResource getThis() {
        return this;
    }

    public void executeSQL(final String sql, final Consumer<Statement> consumer) {

        if (driver == null) throw new IllegalStateException("No postgres driver available");

        try (
                final Connection connection = DriverManager.getConnection(
                        this.getJdbcUrl(),
                        this.getUsername(),
                        this.getPassword());

                final Statement statement = connection.createStatement()) {

            statement.execute(sql);

            consumer.accept(statement);

        } catch (Exception e) {
            throw TestUtil.wrap(e);
        }

    }

    public String getJdbcUrl() {

        return String.format(
                "jdbc:postgresql://%s:%d/%s",
                this.getContainerHost(),
                this.getMappedPort(this.config.getPostgresPort()),
                this.getDatabaseName());

    }

    public String getDatabaseName() {
        return this.config.getDatabaseName();
    }

    public String getUsername() {
        return this.config.getPostgresUser();
    }

    public String getPassword() {
        return this.config.getPostgresPass();
    }

}