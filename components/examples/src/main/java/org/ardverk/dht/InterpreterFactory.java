/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.Interpreter;

/**
 * An utility class to create {@link Interpreter}s.
 */
class InterpreterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(InterpreterFactory.class);
    
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