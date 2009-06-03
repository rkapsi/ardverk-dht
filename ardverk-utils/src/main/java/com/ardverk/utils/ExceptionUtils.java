package com.ardverk.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An utility class to make working with {@link Exception}s easier.
 */
public class ExceptionUtils {
    
    private ExceptionUtils() {}
    
    /**
     * An utility method to wrap {@link Throwable}s in {@link IOException}s
     * unless they are already an {@link IOException} in which case it will
     * simply cast and return it.
     */
    public static IOException toIoException(Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        
        if (cause instanceof IOException) {
            return (IOException)cause;
        }
        
        return new IOException(cause);
    }
    
    /**
     * Returns true if the given {@link Throwable} is or rather was caused
     * by an instance of the given {@link Class}.
     */
    public static boolean isCausedBy(Throwable t, Class<? extends Throwable> clazz) {
        return getCause(t, clazz) != null;
    }
    
    /**
     * Returns the first {@link Exception} from the cause chain that is an
     * instance of the given {@link Class}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T getCause(Throwable t, Class<T> clazz) {
        while(t != null) {
            if (clazz.isInstance(t)) {
                return (T)t;
            }
            t = t.getCause();
        }
        return null;
    }
    
    
    
    /**
     * Turns the given {@link Throwable} in a nicely formatted {@link String}.
     */
    public static String toString(Throwable t) {
        if (t == null) {
            return null;
        }
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        t.printStackTrace(pw);
        IoUtils.close(out);
        return out.toString();
    }
}
