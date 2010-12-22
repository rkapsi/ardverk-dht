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