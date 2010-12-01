package com.ardverk.dht;

import java.util.concurrent.ExecutionException;

import org.ardverk.utils.DeadlockScanner;

import bsh.Interpreter;

public class Main {
    
    public static void main(String[] args) throws Exception, 
            InterruptedException, ExecutionException {
        
        DeadlockScanner.start();
        Interpreter.main(new String[0]);
    }
}
