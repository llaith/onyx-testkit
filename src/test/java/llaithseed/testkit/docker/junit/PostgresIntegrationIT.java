package llaithseed.testkit.docker.junit;

import org.junit.ClassRule;
import org.junit.Test;
import llaithseed.testkit.docker.junit.ext.pgsql.PostgresConfig;
import llaithseed.testkit.docker.junit.ext.pgsql.PostgresResource;

import java.sql.ResultSet;

import static junit.framework.TestCase.assertTrue;
import static llaithseed.testkit.docker.junit.GenericWaitingStrategies.waitForPort;
import static llaithseed.testkit.docker.junit.ext.pgsql.WaitForPostgresStrategy.waitForSelect;
import static llaithseed.testkit.util.TestUtil.rethrow;

public class PostgresIntegrationIT {

    @ClassRule
    public static PostgresResource pg =
            PostgresConfig.builder()
                          .image("postgres:9.6")
                          .postgresPort("5432/tcp")
                          .waitFor(60, 6, 10, (wait) -> {
                              wait.addStrategy(waitForPort("5432/tcp"));
                              wait.addStrategy(waitForSelect("SELECT 1"));
                          })
                          .build();

    @Test
    public void testConnectsToPostgres() throws Exception {

        pg.executeSQL(
                "SELECT 2",
                statement -> rethrow(() -> {

                    final ResultSet resultSet = statement.getResultSet();

                    while (resultSet.next()) {

                        String val = resultSet.getString(1);

                        assertTrue("Select from postgres failed", "2".equals(val));

                    }

                }));

    }

}
