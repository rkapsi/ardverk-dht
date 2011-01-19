/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.storage;

import org.ardverk.dht.storage.Database.Condition;

/**
 * A default implementation of {@link Condition}.
 */
public class DefaultCondition implements Condition {
    
    public static final DefaultCondition SUCCESS 
        = new DefaultCondition(100, "SUCCESS");
    
    public static final DefaultCondition FAILURE 
        = new DefaultCondition(200, "FAILURE");

    public static Condition valueOf(int code, String message) {
        switch (code) {
            case 100:
                return SUCCESS;
            case 200:
                return FAILURE;
        }
        
        return new DefaultCondition(code, message);
    }
    
    private final int code;
    
    private final String message;
    
    private DefaultCondition(int code, String message) {
        this.code = code;
        this.message = message;
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
    public int hashCode() {
        return 31*code + message.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Condition)) {
            return false;
        }
        
        Condition other = (Condition)o;
        return code == other.intValue();
    }
    
    @Override
    public String toString() {
        return code + ", " + message;
    }
}