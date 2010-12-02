package com.ardverk.dht;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.ardverk.io.IoUtils;
import org.ardverk.utils.DeadlockScanner;

import bsh.Interpreter;

public class Main {
    
    public static void main(String[] args) throws Exception {
        
        DeadlockScanner.start();
        
        Interpreter bsh 
            = InterpreterFactory.create();
        
        Reader example = loadExample();
        if (example != null) {
            try {
                bsh.eval(example);
            } finally {
                IoUtils.close(example);
            }
        }
        
        bsh.run();
    }
    
    private static Reader loadExample() {
        InputStream in = Main.class.getResourceAsStream("example.bsh");
        if (in != null) {
            return new BufferedReader(new InputStreamReader(in));
        }
        return null;
    }
}
