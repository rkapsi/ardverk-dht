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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ardverk.dht.KUID;
import org.ardverk.dht.message.Message;


public class SquashPainter extends AbstractPainter {
    
    private final List<Node> nodes 
        = Collections.synchronizedList(new LinkedList<Node>());
    
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    
    private final Point2D.Double localhost = new Point2D.Double();
    
    public SquashPainter(KUID localhostId) {
        super(localhostId);
    }
    
    @Override
    protected void paint(Component c, Graphics2D g) {
        int width = c.getWidth();
        int height = c.getHeight();
        
        g.setColor(Color.ORANGE);
        g.setStroke(new BasicStroke(2.0f));
        g.draw(new Line2D.Float(width/2f, 0f, width/2f, height));
        
        double x = width/2f;
        double y = position(localhostId, height);
        localhost.setLocation(x, y);
        
        synchronized (nodes) {
            for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
                if (it.next().paint(localhost, width, height, g)) {
                    it.remove();
                }
            }
        }
        
        g.setColor(Color.ORANGE);
        dot.setFrame(x-PainterUtils.DOT_SIZE/2d, 
                y-PainterUtils.DOT_SIZE/2d, 
                PainterUtils.DOT_SIZE, 
                PainterUtils.DOT_SIZE);
        g.setStroke(PainterUtils.DEFAULT_STROKE);
        g.fill(dot);
    }
    
    @Override
    public void handleEvent(EventType type, KUID contactId, Message message) {
        if (contactId != null) {
            nodes.add(new Node(type, contactId, message));
            PainterUtils.adjustSize(nodes);
        }
    }
    
    private class Node {
        
        private final long creationTime = System.currentTimeMillis();

        private final EventType type;
        
        private final KUID contactId;
        
        private final Arc2D.Double arc = new Arc2D.Double();
        
        private final Ellipse2D.Double circle = new Ellipse2D.Double();
        
        private final Ellipse2D.Double prxDot = new Ellipse2D.Double();
        
        private final Stroke stroke;
        
        private final Color color;
        
        public Node(EventType type, KUID contactId, Message message) {
            this.type = type;
            this.contactId = contactId;
            
            this.stroke = PainterUtils.getStrokeForMessage(message);
            this.color = PainterUtils.getColorForMessage(message);
        }
        
        private int alpha() {
            long delta = System.currentTimeMillis() - creationTime;
            if (delta < PainterUtils.DURATION) {
                return 255 - (int)(255f/PainterUtils.DURATION * delta);
            }
            return 0;
        }
        
        private double extent() {
            long delta = System.currentTimeMillis() - creationTime;
            if (delta < PainterUtils.DURATION/3L) {
                return 3d * 180f/PainterUtils.DURATION * delta;
            }
            return 180d;
        }
        
        private double radius() {
            final double r = 20d;
            long delta = System.currentTimeMillis() - creationTime;
            if (delta < PainterUtils.DURATION) {
                return r/PainterUtils.DURATION * delta;
            }
            return r;
        }
        
        public boolean paint(Point2D.Double localhost, double width, double height, Graphics2D g) {
            
            if (contactId != null) {
                paintArc(localhost, width, height, g);
            } else {
                paintLine(localhost, width, height, g);
            }
            
            return (System.currentTimeMillis() - creationTime) >= PainterUtils.DURATION;
        }
        
        private void paintArc(Point2D.Double localhost, double width, double height, Graphics2D g) {
            
            double nodeY = position(contactId, height);
            double distance = Math.max(localhost.y, nodeY) - Math.min(localhost.y, nodeY);
            double bow = distance;
            double nodeX = (width-bow)/2d;
            
            double arcX = nodeX;
            double arcY = (localhost.y < nodeY) ? nodeY-distance : nodeY;
            
            double start = 0f;
            double extent = 0f;
            
            if (type == EventType.MESSAGE_SENT) {
                if (localhost.y < nodeY) {
                    start = 90f;
                    extent = -extent();
                } else {
                    start = -90f;
                    extent = extent();
                }
            } else {
                if (localhost.y < nodeY) {
                    start = -90f;
                    extent = -extent();
                } else {
                    start = 90f;
                    extent = extent();
                }
            }

            Point2D.Double corner = new Point2D.Double(
                    localhost.x + 2 * dot.width, 
                    localhost.y + 2 * dot.height);
            
            this.prxDot.setFrameFromCenter(localhost, corner);
            
            Shape shape = null;
            if (!prxDot.contains(width/2d, nodeY)) {
                arc.setArc(arcX, arcY, bow, distance, start, extent, Arc2D.OPEN);
                shape = arc;
            } else {
                double r = radius();
                circle.setFrameFromCenter(localhost.x, localhost.y, 
                        localhost.x+r, localhost.y+r);
                shape = circle;
            }
            
            if (shape != null) {
                g.setStroke(stroke);
                g.setColor(PainterUtils.alpha(color, alpha()));
                g.draw(shape);
            }
        }
        
        private void paintLine(Point2D.Double localhost, double width, double height, Graphics2D g) {
            g.setStroke(PainterUtils.DEFAULT_STROKE);
            g.setColor(new Color(255, 0, 0, alpha()));
            
            double x1 = localhost.x;
            double y1 = localhost.y;
            double x2 = x1 + (width/(2d*180d)) * extent();
            double y2 = y1;
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }
}