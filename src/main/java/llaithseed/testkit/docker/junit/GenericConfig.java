package llaithseed.testkit.docker.junit;

/**
 *
 */
public class GenericConfig extends DockerConfig<GenericConfig,GenericResource> {

    public static GenericConfig builder() {
        return new GenericConfig();
    }

    @Override
    public GenericConfig getThis() {
        return this;
    }

    @Override
    public GenericResource build() {
        return new GenericResource(this);
    }

}
