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

package org.ardverk.dht.storage;


/**
 * A default implementation of {@link Status}.
 */
public class DefaultStatus implements Status {
    
    private static enum Code {
        SUCCESS(100),
        FAILURE(200),
        CONFLICT(201);
        
        private final int value;
        
        private Code(int value) {
            this.value = value;
        }
    }
    
    /**
     * A generic {@link Status} if the STORE operation completed successfully.
     */
    public static final Status SUCCESS = new DefaultStatus(Code.SUCCESS, null);
    
    /**
     * A generic {@link Status} if the STORE operation failed.
     */
    public static final Status FAILURE = new DefaultStatus(Code.FAILURE, null);
    
    /**
     * Factory method to create {@link Status}.
     */
    public static Status valueOf(int code, String message, ValueTuple tuple) {
        if (tuple == null) {
            switch (code) {
                case 100:
                    if (SUCCESS.stringValue().equalsIgnoreCase(message)) {
                        return SUCCESS;
                    }
                    break;
                case 200:
                    if (FAILURE.stringValue().equalsIgnoreCase(message)) {
                        return FAILURE;
                    }
                    break;
            }
        }
        
        return new DefaultStatus(code, message, tuple);
    }
    
    /**
     * Creates a {@link Status} for the case there was a conflict.
     */
    public static Status conflict(ValueTuple tuple) {
        return new DefaultStatus(Code.CONFLICT, tuple);
    }
    
    private final int code;
    
    private final String message;
    
    private final ValueTuple tuple;
    
    private DefaultStatus(Code code, ValueTuple tuple) {
        this(code.value, code.name(), tuple);
    }
    
    private DefaultStatus(int code, String message, ValueTuple tuple) {
        this.code = code;
        this.message = message;
        this.tuple = tuple;
    }
    
    @Override
    public int intValue() {
        return code;
    }
    
    @Override
    public String stringValue() {
        return message;
    }
    
    @Override
    public ValueTuple getValueTuple() {
        return tuple;
    }

    @Override
    public int hashCode() {
        return 31*code + message.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Status)) {
            return false;
        }
        
        Status other = (Status)o;
        return code == other.intValue();
    }
    
    @Override
    public String toString() {
        return code + ", " + message;
    }
}