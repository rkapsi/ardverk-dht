package com.ardverk.utils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.ardverk.utils.ExceptionUtils;

/**
 * An utility class for I/O operations.
 */
public class IoUtils {
    
    private IoUtils() {}
    
    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException err) {
                ExceptionUtils.exceptionCaught(err);
            }
        }
        return false;
    }
    
    public static boolean closeAll(Closeable... closeables) {
        boolean success = false;
        if (closeables != null) {
            for (Closeable c : closeables) {
                success |= close(c);
            }
        }
        return success;
    }
    
    public static boolean closeAll(Iterable<? extends Closeable> closeables) {
        boolean success = false;
        if (closeables != null) {
            for (Closeable c : closeables) {
                success |= close(c);
            }
        }
        return success;
    }
    
    public static boolean flush(Flushable flushable) {
        if (flushable != null) {
            try {
                flushable.flush();
                return true;
            } catch (IOException err) {
                ExceptionUtils.exceptionCaught(err);
            }
        }
        return false;
    }
    
    public static boolean flushAll(Flushable... flushables) {
        boolean success = false;
        if (flushables != null) {
            for (Flushable c : flushables) {
                success |= flush(c);
            }
        }
        return success;
    }
    
    public static boolean flushAll(Iterable<? extends Flushable> flushables) {
        boolean success = false;
        if (flushables != null) {
            for (Flushable c : flushables) {
                success |= flush(c);
            }
        }
        return success;
    }
}
