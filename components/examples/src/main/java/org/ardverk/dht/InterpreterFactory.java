/*
 * Copyright 2009-2012 Roger Kapsi
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.ardverk.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * An utility class to create {@link Interpreter}s.
 */
public class InterpreterFactory {

  private static final Logger LOG 
    = LoggerFactory.getLogger(InterpreterFactory.class);
  
  static {
    Interpreter.DEBUG = LOG.isDebugEnabled();
    Interpreter.TRACE = LOG.isTraceEnabled();
  }
  
  private InterpreterFactory() {}
  
  public static Interpreter create() {
    Reader in = createCommandLineReader();
    return new Interpreter(in, System.out, System.err, true);
  }
  
  public static Interpreter create(Class<?> clazz, String name) {
    Reader reader = getValueAsStream(clazz, name);
    try {
      return create(reader);
    } finally {
      IoUtils.close(reader);
    }
  }
  
  public static Interpreter create(Reader reader) {
    Interpreter interpreter = create();
    
    if (reader != null) {
      try {
        interpreter.eval(reader);
      } catch (EvalError err) {
        throw new IllegalStateException(err);
      }
    }
    
    return interpreter;
  }
  
  private static Reader getValueAsStream(Class<?> clazz, String name) {
    InputStream in = clazz.getResourceAsStream(name);
    if (in != null) {
      return new BufferedReader(new InputStreamReader(in));
    }
    return null;
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