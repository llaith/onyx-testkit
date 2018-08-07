package org.llaith.onyx.testkit.docker.junit;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

import static org.llaith.onyx.testkit.docker.junit.GenericWaitingStrategies.waitForLog;
import static org.junit.Assert.assertEquals;

/**
 * Note, bug with recursive
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206881405-Slightly-OT-generics-problem
 */
public class TimeoutRabbitIntegrationIT {

    public static class PretendTestClass {

        @ClassRule
        public static final GenericResource rule = GenericConfig
                .builder()
                .image("rabbitmq:management")
                .ports("5672")
                .waitFor(5, 10, 5, wait -> wait.addStrategy(waitForLog("You won't find this text in the log!")))
                .build();

        @Test
        public void testConnectsToDocker() throws Exception {

            assertEquals("Port is incorrect", "5672", rule.getContainerHost());

        }


    }

    @Test
    public void testClassruleFailureThrowsTimeout() throws Exception {

        Result result = JUnitCore.runClasses(PretendTestClass.class);

        final List<Failure> failures = result.getFailures();

        final Throwable exception = failures.get(0).getException();

        Assert.assertTrue(
                String.format(
                        "Expected exception: (%s) but received exception: (%s)",
                        ContainerTimeoutException.class,
                        exception.getClass()),
                ContainerTimeoutException.class.isAssignableFrom(exception.getClass()));

    }

}
