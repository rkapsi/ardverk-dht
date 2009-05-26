package com.ardverk.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ardverk.logging.LoggerUtils;

public class IoUtils {

    private static final Logger LOG 
        = LoggerUtils.getLogger(IoUtils.class);
    
    private IoUtils() {}
    
    public static boolean close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                return true;
            }
        } catch (IOException err) {
            LOG.log(Level.SEVERE, "IOException", err);
        }
        return false;
    }
}
