package com.ardverk.logging;

import java.util.logging.Logger;

public class LoggerUtils {

    private LoggerUtils() {}
    
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
