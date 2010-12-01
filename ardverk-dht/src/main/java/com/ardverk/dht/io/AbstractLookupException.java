package com.ardverk.dht.io;

import java.util.concurrent.TimeUnit;

import com.ardverk.dht.io.LookupResponseHandler.Outcome;
import com.ardverk.dht.lang.ArdverkException;

abstract class AbstractLookupException extends ArdverkException {
    
    private static final long serialVersionUID = -2767832375265292182L;

    private final Outcome outcome;
    
    public AbstractLookupException(Outcome outcome) {
        super(outcome.getTimeInMillis(), TimeUnit.MILLISECONDS);
        this.outcome = outcome;
    }

    public Outcome getOutcome() {
        return outcome;
    }
}
