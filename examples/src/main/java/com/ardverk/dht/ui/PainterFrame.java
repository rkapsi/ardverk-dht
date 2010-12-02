package com.ardverk.dht.ui;

import javax.swing.JFrame;

import com.ardverk.dht.DHT;

public class PainterFrame extends JFrame {
    
    private static final long serialVersionUID = 5665016005270431817L;
    
    public PainterFrame(DHT dht) {
        super("Ardverk DHT");
        setContentPane(new PainterPanel(dht));
        setBounds(20, 30, 640, 480);
    }
    
    public PainterPanel getPainterPanel() {
        return (PainterPanel)getContentPane();
    }
    
    public void start() {
        getPainterPanel().start();
    }
    
    public void stop() {
        getPainterPanel().stop();
    }
    
    public boolean isRunning() {
        return getPainterPanel().isRunning();
    }
}
