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

package org.ardverk.dht.utils;

import java.util.Comparator;

import org.ardverk.dht.KUID;
import org.ardverk.dht.lang.Identifier;
import org.ardverk.lang.Precoditions;


/**
 * The {@link XorComparator} compares {@link Identifier}s by their XOR distance.
 */
public class XorComparator implements Comparator<Identifier> {

    private final Identifier identifier;
    
    public XorComparator(Identifier identifier) {
        this.identifier = Precoditions.notNull(identifier, "identifier");
    }
    
    /**
     * Returns the {@link Identifier}.
     */
    public Identifier getIdentifier() {
        return identifier;
    }
    
    private KUID xor(Identifier id) {
        return identifier.getId().xor(id.getId());
    }
    
    @Override
    public int compare(Identifier o1, Identifier o2) {
        return xor(o1).compareTo(xor(o2));
    }
}