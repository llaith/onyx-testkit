package org.llaith.onyx.testkit.util;

/**
 *
 */
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static RuntimeException wrap(final Throwable t) {
        return wrap(t.getMessage() + " (rethrown)", t);
    }

    public static RuntimeException wrap(final String message, final Throwable t) {
        if (t instanceof Error) throw (Error)t;
        if (t instanceof RuntimeException) return (RuntimeException)t;
        return new UncheckedException(message, t);
    }

    public UncheckedException(String message) {
        super(message);
    }

    protected UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

}
