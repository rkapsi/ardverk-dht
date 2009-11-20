package com.ardverk.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;

import com.ardverk.logging.LoggerUtils;

public class IoUtils {

    private static final Logger LOG 
        = LoggerUtils.getLogger(IoUtils.class);
    
    private IoUtils() {}
    
    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException err) {
                LOG.error("IOException", err);
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
    
    public static boolean closeAll(Collection<? extends Closeable> closeables) {
        boolean success = false;
        if (closeables != null) {
            for (Closeable c : closeables) {
                success |= close(c);
            }
        }
        return success;
    }
}
