package org.llaith.onyx.testkit.docker.junit;

/**
 *
 */
public class GenericResource extends DockerResource<GenericConfig,GenericResource> {

    public GenericResource(final GenericConfig config) {
        super(config);
    }

    @Override
    public GenericResource getThis() {
        return this;
    }

}
