/*
 * Copyright 2010 Roger Kapsi
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

package com.ardverk.dht.lang;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ArdverkException extends IOException {
    
    private static final long serialVersionUID = 5855991566361343341L;

    private final long timeInMillis;

    public ArdverkException(long time, TimeUnit unit) {
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(String message, Throwable cause, 
            long time, TimeUnit unit) {
        super(message, cause);
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(String message, long time, TimeUnit unit) {
        super(message);
        this.timeInMillis = unit.toMillis(time);
    }

    public ArdverkException(Throwable cause, long time, TimeUnit unit) {
        super(cause);
        this.timeInMillis = unit.toMillis(time);
    }
    
    public long getTime(TimeUnit unit) {
        return unit.convert(timeInMillis, TimeUnit.MILLISECONDS);
    }
    
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
}