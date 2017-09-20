package llaithseed.testkit.docker.junit;

/**
 *
 */
public interface WaitingStrategy<C extends DockerConfig<C,R>, R extends DockerResource<C,R>> {

    void waitForContainer(final R resource);

}
