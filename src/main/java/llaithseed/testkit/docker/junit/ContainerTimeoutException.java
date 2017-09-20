package llaithseed.testkit.docker.junit;

/**
 *
 */
public class ContainerTimeoutException extends RuntimeException {

    public ContainerTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
