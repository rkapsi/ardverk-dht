package com.ardverk.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    
    private ExceptionUtils() {}
    
    /**
     * Takes the given Exception, wraps it in an {@link IOException}
     * and returns it.
     */
    public static IOException toIOException(Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        
        if (cause instanceof IOException) {
            return (IOException)cause;
        }
        
        return new IOException(cause);
    }
    
    /**
     * Returns true if the message of the given {@link Throwable}
     * contains any of the other messages.
     */
    public static boolean isIgnorable(Throwable t, String... messages) {
        String msg = t.getMessage();
        if (msg != null) {
            for (int i = messages.length-1; i >= 0; --i) {
                if (msg.startsWith(messages[i])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if Throwable <tt>t</tt> was caused by an another
     * Exception that is of type <tt>clazz</tt>
     */
    public static boolean isCausedBy(Throwable t, Class<? extends Throwable> clazz) {
        return getCause(t, clazz) != null;
    }
    
    /**
     * Returns the first Exception from the exception cause 
     * chain that is of type <tt>clazz</tt> or null if the
     * given Throwable was not caused a such Exception.
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
     * Returns the stack trace of the given {@link Throwable}
     * as a String.
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
