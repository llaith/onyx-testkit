package llaithseed.testkit.docker.junit.ext.pgsql;

import llaithseed.testkit.docker.junit.GenericWaitingStrategies;
import org.slf4j.Logger;
import llaithseed.testkit.docker.junit.WaitingStrategy;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by psanecki on 22/03/17.
 */
public class WaitForPostgresStrategy {

    private static final Logger logger = getLogger(WaitForPostgresStrategy.class);

    public static WaitingStrategy<PostgresConfig,PostgresResource> waitForSelect(final String sql) {

        return (resource) -> {

            logger.debug("Executing sql: " + sql);

            resource.executeSQL(sql, statement -> logger.debug("Successful execution of sql: " + sql));

        };

    }

    public static WaitingStrategy<PostgresConfig,PostgresResource> waitForPostgressExt(final String extension) {

        return (resource) -> {

            final String[] cmd = new String[] {
                    "psql",
                    "--username", resource.getUsername(),
                    "-d", resource.getDatabaseName(),
                    "-c", "select * from (select extname from pg_extension where extname='" + extension + "') as s;"};

            logger.debug("Executing commandline psql: " + Arrays.toString(cmd));

            GenericWaitingStrategies
                    .<PostgresConfig,PostgresResource>waitExecResult(cmd, "(1 row)")
                    .waitForContainer(resource);

        };

    }

}
