package org.llaith.onyx.testkit.docker.junit;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public abstract class DockerConfig<C extends DockerConfig<C,R>, R extends DockerResource<C,R>> {

    private String imageName;

    private Set<String> ports = new HashSet<>();

    private List<String> env = new ArrayList<>();

    private String[] cmd;

    private WaitingStrategiesImpl<C,R> waitingStrategies;

    @SuppressWarnings("squid:S1313")
    private static final String INTERNAL_IP = "0.0.0.0"; //NOPMD

    public C image(String imageName) {
        this.imageName = imageName;
        return this.getThis();
    }

    public C ports(String... ports) {

        this.ports.addAll(asList(requireNonNull(ports)));

        // force consistent syntax through all references to ports
        this.ports.forEach(port -> {
            if (port.endsWith("/tcp") || port.endsWith("/udp")) throw new IllegalArgumentException(
                    "Port should end with '/tcp' or '/udp'");
        });

        return this.getThis();
    }

    public C env(String... env) {
        this.env.addAll(asList(requireNonNull(env)));
        return this.getThis();
    }

    public C cmd(String... cmd) {
        this.cmd = cmd;
        return this.getThis();
    }

    public boolean containsPort(final String port) {

        // again, force this syntax for consistency
        if (!requireNonNull(port).endsWith("/tcp") || port.endsWith("/udp"))
            throw new IllegalArgumentException("The port must end in '/tcp' or '/udp'");

        return this.ports.contains(port);

    }

    public DockerConfig addPort(final String port) {

        // again, forcing this syntax for consistency
        if (!requireNonNull(port).endsWith("/tcp") || port.endsWith("/udp"))
            throw new IllegalArgumentException("The port must end in '/tcp' or '/udp'");

        this.ports.add(port);

        return this;

    }

    public C waitFor(final Consumer<WaitingStrategies<C,R>> consumer) {

        return this.waitForEx(new WaitingStrategiesImpl<>(), consumer);

    }

    public C waitFor(final int maxWaitSecs, final int callRatePerMin, final int timeoutSeconds, final Consumer<WaitingStrategies<C,R>> consumer) {

        return this.waitForEx(new WaitingStrategiesImpl<>(maxWaitSecs, callRatePerMin, timeoutSeconds), consumer);

    }

    private C waitForEx(final WaitingStrategiesImpl<C,R> waitingStrategies, final Consumer<WaitingStrategies<C,R>> consumer) {

        if (this.waitingStrategies != null) throw new IllegalStateException("Waiting strategies already set");

        this.waitingStrategies = waitingStrategies;

        consumer.accept(this.waitingStrategies);

        return this.getThis();

    }

    public String imageName() {
        return this.imageName;
    }

    public String[] ports() {
        return this.ports.toArray(new String[0]);
    }

    public String[] env() {
        return this.env.toArray(new String[0]);
    }

    public String[] cmd() {
        return this.cmd;
    }

    public WaitingStrategiesImpl<C,R> waitingStrategies() {
        return this.waitingStrategies;
    }

    public ContainerConfig toConfig() {

        Map<String,List<PortBinding>> portBindings = this.ports
                .stream()
                .collect(Collectors.toMap(
                        port -> port,
                        port -> singletonList(PortBinding.randomPort(INTERNAL_IP))));

        final HostConfig hostConfig = HostConfig
                .builder()
                .portBindings(portBindings)
                .build();

        final ContainerConfig.Builder configBuilder = ContainerConfig
                .builder()
                .hostConfig(hostConfig)
                .image(imageName)
                .networkDisabled(false)
                .exposedPorts(this.ports);

        if (env != null && !env.isEmpty()) configBuilder.env(env);

        if (cmd != null && cmd.length > 0) configBuilder.cmd(cmd);

        return configBuilder.build();

    }

    public abstract C getThis();

    public abstract R build();

}
