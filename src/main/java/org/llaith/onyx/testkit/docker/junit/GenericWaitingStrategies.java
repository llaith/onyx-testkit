package org.llaith.onyx.testkit.docker.junit;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ExecCreation;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static com.spotify.docker.client.DockerClient.LogsParam.follow;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;
import static org.slf4j.LoggerFactory.getLogger;
import static org.llaith.onyx.testkit.util.TestUtil.readStringFromByteBuffer;
import static org.llaith.onyx.testkit.util.TestUtil.rethrow;
import static org.llaith.onyx.testkit.util.TestUtil.rethrowOrReturn;

/**
 *
 */
public class GenericWaitingStrategies {

    private static final Logger logger = getLogger(GenericWaitingStrategies.class);

    public static <C extends DockerConfig<C,R>, R extends DockerResource<C,R>> WaitingStrategy<C,R> waitForLog(
            final String match) {

        return (resource) -> {

            logger.debug("waiting for log match on: " + match);

            final LogStream logs = rethrowOrReturn(() -> resource.client.logs(resource.container.id(), follow(), stdout()));

            while (!Thread.currentThread().isInterrupted()) {

                final String line = readStringFromByteBuffer(logs.next().content());

                logger.trace("Read logger line: " + line);

                if (line.contains(match)) {

                    logger.debug("Successfully found log entry of: " + match);

                    break;

                }

            }

        };

    }

    public static <C extends DockerConfig<C,R>, R extends DockerResource<C,R>> WaitingStrategy<C,R> waitForPort(
            final String port) {

        return (resource) -> {

            logger.debug("Waiting for port: " + port);

            final SocketAddress address = new InetSocketAddress(resource.getContainerHost(), resource.getMappedPort(port));

            rethrow(() -> SocketChannel.open(address));

            logger.debug("Successful opened port: " + port);

        };

    }

    public static <C extends DockerConfig<C,R>, R extends DockerResource<C,R>> WaitingStrategy<C,R> waitExecResult(
            final String[] command, final String result) {

        return (resource) -> rethrow(() -> {

            logger.debug(String.format("Waiting for result of: %s from exec of command: %s",
                                       result,
                                       Arrays.toString(command)));

            final ExecCreation execCreation = resource.client.execCreate(
                    resource.container.id(),
                    command,
                    DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());

            final LogStream output = resource.client.execStart(execCreation.id());

            final String execOutput = output.readFully();

            logger.trace("Found result: " + execOutput);

            if (!execOutput.contains(result))
                throw new IllegalStateException(String.format(
                        "Expected result :[%s] from command: [%s] not found.",
                        Arrays.toString(command),
                        result));

            logger.debug("Successful execution of command: " + Arrays.toString(command));


        });

    }

}
