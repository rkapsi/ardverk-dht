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

package org.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.ArdverkException;
import org.ardverk.dht.lang.Identifier;


abstract class AbstractLookupException extends ArdverkException implements Identifier {
    
    private static final long serialVersionUID = -2767832375265292182L;

    private final Outcome outcome;
    
    public AbstractLookupException(Outcome outcome) {
        super(outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        this.outcome = outcome;
    }

    /**
     * Returns the {@link Outcome} as it was at the point of time when
     * the {@link AbstractLookupException} occurred.
     */
    public Outcome getOutcome() {
        return outcome;
    }

    @Override
    public KUID getId() {
        return outcome.getId();
    }
}