package com.ardverk.utils;

import java.io.Closeable;
import java.io.IOException;

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
}
