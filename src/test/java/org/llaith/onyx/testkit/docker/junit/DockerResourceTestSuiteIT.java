package org.llaith.onyx.testkit.docker.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
        RabbitIntegrationIT.class,
        TimeoutRabbitIntegrationIT.class,
        PostgresIntegrationIT.class})
public class DockerResourceTestSuiteIT {
    // run this from intellij to not get weird failures due to the nested junit class in the timeout example
}
