package org.llaith.onyx.testkit.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class TestUtil {

    public static RuntimeException wrap(final Throwable t) {
        if (t instanceof Error) throw (Error)t;
        if (t instanceof RuntimeException) return (RuntimeException)t;
        return new RuntimeException(t.getMessage() + " (rethrown)", t);
    }

    @FunctionalInterface
    public interface Block<E extends Exception> {

        void execute() throws E;

    }

    public static void rethrow(final Block<Exception> block) {

        try {

            block.execute();

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    @FunctionalInterface
    public interface ReturnBlock<R, E extends Exception> {

        R execute() throws E;

    }

    public static <X> X rethrowOrReturn(final ReturnBlock<X,Exception> block) {

        try {

            return block.execute();

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    @Nonnull
    public static <K, V> Map<K,V> nullToEmpty(@Nullable final Map<K,V> map) {

        return map == null ? Collections.emptyMap() : map;

    }

    public static boolean isBlankOrNull(@Nullable final String s) {
        return s == null || s.trim()
                             .length() < 1;
    }

    public static String readStringFromByteBuffer(final ByteBuffer buffer) {

        final byte[] bytes = new byte[buffer.remaining()];

        buffer.get(bytes);

        return new String(bytes);

    }

}
