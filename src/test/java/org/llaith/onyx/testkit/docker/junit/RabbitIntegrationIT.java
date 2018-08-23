package org.llaith.onyx.testkit.docker.junit;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.llaith.onyx.testkit.docker.junit.GenericWaitingStrategies.waitForLog;

public class RabbitIntegrationIT {

    @ClassRule
    public static GenericResource rabbitRule =
            GenericConfig.builder()
                         .image("rabbitmq:management")
                         .ports("5672")
                         .waitFor(60, 6, 10, (wait) -> wait.addStrategy(waitForLog("Server startup complete")))
                         .build();

    @Test
    public void testConnectsToDocker() throws IOException, TimeoutException {

        final ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(rabbitRule.getContainerHost());
        factory.setPort(rabbitRule.getMappedPort("5672/tcp"));

        factory.newConnection();

    }

}
