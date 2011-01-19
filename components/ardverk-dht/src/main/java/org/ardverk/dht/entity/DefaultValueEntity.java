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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.io.Outcome;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.storage.Value;
import org.ardverk.dht.storage.ValueTuple;


public class DefaultValueEntity extends AbstractLookupEntity implements ValueEntity {
    
    private final Outcome outcome;
    
    private final ValueTuple[] values;
    
    public DefaultValueEntity(Outcome outcome, ValueTuple[] values) {
        super(outcome.getId(), outcome.getTimeInMillis(), 
                TimeUnit.MILLISECONDS);
        
        this.outcome = outcome;
        this.values = values;
    }
    
    @Override
    public Contact getSender() {
        return getValueTuple().getSender();
    }
    
    @Override
    public Contact getCreator() {
        return getValueTuple().getCreator();
    }

    @Override
    public Value getValue() {
        return getValueTuple().getValue();
    }
    
    @Override
    public ValueTuple getValueTuple() {
        return CollectionUtils.first(values);
    }
    
    @Override
    public ValueTuple[] getValueTuples() {
        return values;
    }
    
    public Outcome getOutcome() {
        return outcome;
    }
}