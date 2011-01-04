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

package com.ardverk.dht.config;

/**
 * The {@link GetConfig} is providing configuration date for the
 * FIND_VALUE process.
 */
public interface GetConfig extends LookupConfig {
    
    /**
     * Returns the retrieval count. The default value is 1.
     */
    public int getR();

    /**
     * Sets the retrieval count. The default value is 1 and the
     * maximum is K.
     */
    public void setR(int r);
}