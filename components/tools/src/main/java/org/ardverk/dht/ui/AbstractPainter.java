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

package org.ardverk.dht.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;

import org.ardverk.dht.KUID;


abstract class AbstractPainter implements Painter {
    
    protected final KUID localhostId;
    
    private final BigDecimal maxId;
    
    public AbstractPainter(KUID localhostId) {
        this.localhostId = localhostId;
        this.maxId = new BigDecimal(
                localhostId.max().toBigInteger(), 
                PainterUtils.SCALE);
    }
    
    @Override
    public void paint(Component c, Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        paint(c, g2);
    }
    
    protected abstract void paint(Component c, Graphics2D g);
    
    protected double position(KUID nodeId, double scale) {
        return position(new BigDecimal(nodeId.toBigInteger(), PainterUtils.SCALE), scale);
    }
    
    protected double position(BigDecimal nodeId, double scale) {
        return nodeId.divide(maxId, 
                BigDecimal.ROUND_HALF_UP).multiply(
                    BigDecimal.valueOf(scale)).doubleValue();
    }
}