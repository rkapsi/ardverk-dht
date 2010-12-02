package com.ardverk.dht;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.slf4j.Logger;

import bsh.Interpreter;

import com.ardverk.dht.logging.LoggerUtils;

/**
 * An utility class to create {@link Interpreter}s.
 */
class InterpreterFactory {

    private static final Logger LOG = LoggerUtils.getLogger(InterpreterFactory.class);
    
    static {
        Interpreter.DEBUG = LOG.isDebugEnabled();
        Interpreter.TRACE = LOG.isTraceEnabled();
    }
    
    private InterpreterFactory() {}
    
    public static Interpreter create() {
        Reader in = createCommandLineReader();
        return new Interpreter(in, System.out, System.err, true);
    }
    
    // HACK: Why is CommandLineReader a package private class?
    public static Reader createCommandLineReader() {
        try {
            Class<?> clazz = Class.forName("bsh.CommandLineReader");
            Constructor<?> constructor = clazz.getConstructor(Reader.class);
            constructor.setAccessible(true);
            return (Reader)constructor.newInstance(new InputStreamReader(System.in));
        } catch (Exception err) {
            throw new IllegalStateException("Exception", err);
        }
    }
}
