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

package com.ardverk.dht.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ardverk.lang.Arguments;

import com.ardverk.dht.KUID;

public class DefaultDatabase extends AbstractDatabase {

    private final Map<KUID, ValueTuple> database 
        = new ConcurrentHashMap<KUID, ValueTuple>();
    
    private final DatabaseConfig config;
    
    public DefaultDatabase() {
        this(new DefaultDatabaseConfig());
    }
    
    public DefaultDatabase(DatabaseConfig config) {
        this.config = Arguments.notNull(config, "config");
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() {
        return config;
    }

    @Override
    public ValueTuple get(KUID valueId) {
        return database.get(valueId);
    }

    @Override
    public Condition store(ValueTuple tuple) {
        KUID key = tuple.getId();
        
        if (!tuple.isEmpty()) {
            database.put(key, tuple);
        } else {
            database.remove(key);
        }
        
        return DefaultCondition.SUCCESS;
    }
    
    @Override
    public int size() {
        return database.size();
    }
    
    @Override
    public ValueTuple[] values() {
        return database.values().toArray(new ValueTuple[0]);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        for (ValueTuple entity : database.values()) {
            buffer.append(entity).append("\n");
        }
        
        return buffer.toString();
    }
}