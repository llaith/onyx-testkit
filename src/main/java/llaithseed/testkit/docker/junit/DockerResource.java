package llaithseed.testkit.docker.junit;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.PortBinding;
import llaithseed.testkit.util.TestUtil;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static llaithseed.testkit.util.TestUtil.nullToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <p>
 * JUnit rule starting a docker container before the test and killing it
 * afterwards.
 * </p>
 * <p>
 * Uses spotify/docker-client.
 * Adapted from https://gist.github.com/mosheeshel/c427b43c36b256731a0b
 * </p>
 * author: Geoffroy Warin (geowarin.github.io)
 */
public abstract class DockerResource<C extends DockerConfig<C,R>, R extends DockerResource<C,R>> extends ExternalResource {

    private static final Logger logger = getLogger(DockerResource.class);

    protected final C config;

    protected final DockerClient client;

    protected final ContainerCreation container;

    protected ContainerInfo info;

    public DockerResource(final C config) {

        try {

            this.config = config;

            logger.debug("Building docker client from env");
            this.client = DefaultDockerClient
                    .fromEnv()
                    .build();

            logger.debug("pulling image: " + config.imageName());
            this.client.pull(config.imageName());

            logger.debug("creating container");
            this.container = this.client.createContainer(this.config.toConfig());

        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialise docker container", e);
        }

    }

    @Override
    protected void before() throws Throwable {

        super.before();

        try {

            this.client.startContainer(container.id());

            this.info = this.client.inspectContainer(container.id());

            try {

                if (this.config.waitingStrategies() != null)
                    this.config.waitingStrategies().waitForContainer(this.getThis());

            } catch (Exception e) {

                throw new ContainerTimeoutException("Docker container never became ready", e);

            }

        } catch (Exception e) {

            this.after();

            throw TestUtil.wrap(e);

        }

    }

    @Override
    protected void after() {

        super.after();

        try {

            this.client.killContainer(container.id());

            this.client.removeContainer(
                    container.id(),
                    DockerClient.RemoveContainerParam.removeVolumes());

            this.client.close();

        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException("Unable to stop/remove docker container " + container.id(), e);
        }

    }

    public DockerClient getClient() {

        return this.client;

    }

    public C getConfig() {

        return this.config;

    }

    public String getContainerHost() {

        return client.getHost();

    }

    public int getMappedPort(final String containerPort) {

        // again, force this syntax for consistency
        if (!requireNonNull(containerPort).endsWith("/tcp") || containerPort.endsWith("/udp"))
            throw new IllegalArgumentException("The port must end in '/tcp' or '/udp'");

        final List<PortBinding> ports = nullToEmpty(this.info.networkSettings().ports()).get(containerPort);

        return ports.isEmpty() ?
                -1 :
                Integer.parseInt(ports.get(0).hostPort());


    }

    public abstract R getThis();

}
