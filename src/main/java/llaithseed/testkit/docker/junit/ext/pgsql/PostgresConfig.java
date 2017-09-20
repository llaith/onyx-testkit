package llaithseed.testkit.docker.junit.ext.pgsql;

import llaithseed.testkit.docker.junit.DockerConfig;

import static java.util.Objects.requireNonNull;
import static llaithseed.testkit.docker.junit.GenericWaitingStrategies.waitForPort;
import static llaithseed.testkit.docker.junit.ext.pgsql.WaitForPostgresStrategy.waitForSelect;
import static llaithseed.testkit.util.TestUtil.isBlankOrNull;

/**
 *
 */
public class PostgresConfig extends DockerConfig<PostgresConfig,PostgresResource> {

    public static PostgresConfig builder() {
        return new PostgresConfig();
    }

    public static PostgresConfig builderWithDefaults() {

        return builder()
                .image("postgres:9.6")
                .postgresPort("5432/tcp")
                .waitFor(60, 6, 10, (wait) -> {
                    wait.addStrategy(waitForPort("5432/tcp"));
                    wait.addStrategy(waitForSelect("SELECT 1"));
                });

    }

    String postgresPort; // default postgres is "5432/tcp", but force this to be set for consistency
    String postgresUser = "postgres";
    String postgresPassword = "";
    String databaseName = "postgres";

    public PostgresConfig postgresPort(final String postgresPort) {

        if (!requireNonNull(postgresPort).endsWith("/tcp"))
            throw new IllegalArgumentException("The postgres port must end in '/tcp'");

        this.postgresPort = postgresPort;

        this.addPort(this.postgresPort);

        return this;

    }

    public PostgresConfig postgresUser(final String postgresUser) {
        this.postgresUser = requireNonNull(postgresUser);
        return this;
    }

    public PostgresConfig postgresPassword(final String postgresPassword) {
        this.postgresPassword = requireNonNull(postgresPassword);
        return this;
    }

    public PostgresConfig databaseName(final String databaseName) {
        this.databaseName = requireNonNull(databaseName);
        return this;
    }

    @Override
    public PostgresConfig getThis() {
        return this;
    }

    @Override
    public PostgresResource build() {

        if (isBlankOrNull(this.postgresPort))
            throw new IllegalStateException("the postgresPort value has not been set");

        return new PostgresResource(this);

    }

}