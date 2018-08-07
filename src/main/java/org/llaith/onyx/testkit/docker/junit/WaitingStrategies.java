package org.llaith.onyx.testkit.docker.junit;

/**
 *
 */
public interface WaitingStrategies<C extends DockerConfig<C,R>, R extends DockerResource<C,R>> {

    WaitingStrategies<C,R> addStrategy(WaitingStrategy<C,R> strategy);

}
