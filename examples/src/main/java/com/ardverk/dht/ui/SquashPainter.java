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

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.RequestMessage;

public class SquashPainter extends AbstractPainter {

    private static final long ATTACK = 250L;
    
    private static final long RELEASE = 2750L;
    
    private static final long DURATION = ATTACK + RELEASE;
    
    private static final float DOT_SIZE = 6f;
    
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
        
        g.setColor(Color.orange);
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
        
        g.setColor(Color.orange);
        dot.setFrame(x-DOT_SIZE/2d, y-DOT_SIZE/2d, DOT_SIZE, DOT_SIZE);
        g.setStroke(DEFAULT_STROKE);
        g.fill(dot);
    }
    
    @Override
    public void handleEvent(EventType type, KUID contactId, Message message) {
        if (contactId != null) {
            nodes.add(new Node(dot, type, contactId, message));
        }
    }
    
    @Override
    public void clear() {
        nodes.clear();
    }
    
    private class Node {
        
        private final Ellipse2D.Double dot;
        
        private final EventType type;
        
        private final KUID nodeId;
        
        private final boolean request;
        
        private final long timeStamp = System.currentTimeMillis();
        
        private final Arc2D.Double arc = new Arc2D.Double();
        
        private final Ellipse2D.Double circle = new Ellipse2D.Double();
        
        private final Ellipse2D.Double prxDot = new Ellipse2D.Double();
        
        private final Stroke stroke;
        
        public Node(Ellipse2D.Double dot, EventType type, KUID nodeId, Message message) {
            this.dot = dot;
            this.type = type;
            this.nodeId = nodeId;
            this.request = (message instanceof RequestMessage);
            
            this.stroke = PainterUtils.getStrokeForMessage(message);
            
            if (nodeId == null) {
                assert (request && type.equals(EventType.MESSAGE_SENT));
            }
        }
        
        private int alpha() {
            long delta = System.currentTimeMillis() - timeStamp;
            if (delta < DURATION) {
                return 255 - (int)(255f/DURATION * delta);
            }
            return 0;
        }
        
        private double extent() {
            long delta = System.currentTimeMillis() - timeStamp;
            if (delta < DURATION/3L) {
                return 3d * 180f/DURATION * delta;
            }
            return 180d;
        }
        
        private double radius() {
            final double r = 20d;
            long delta = System.currentTimeMillis() - timeStamp;
            if (delta < DURATION) {
                return r/DURATION * delta;
            }
            return r;
        }
        
        public boolean paint(Point2D.Double localhost, double width, double height, Graphics2D g) {
            
            if (nodeId != null) {
                paintArc(localhost, width, height, g);
            } else {
                paintLine(localhost, width, height, g);
            }
            
            return (System.currentTimeMillis() - timeStamp) >= DURATION;
        }
        
        private void paintArc(Point2D.Double localhost, double width, double height, Graphics2D g) {
            
            double nodeY = position(nodeId, height);
            double distance = Math.max(localhost.y, nodeY) - Math.min(localhost.y, nodeY);
            double bow = distance;
            double nodeX = (width-bow)/2d;
            
            double arcX = nodeX;
            double arcY = (localhost.y < nodeY) ? nodeY-distance : nodeY;
            
            double start = 0f;
            double extent = 0f;
            
            int red = 0;
            int green = 0;
            int blue = 0;
            
            if (type.equals(EventType.MESSAGE_SENT)) {
                red = 255;
                if (!request) {
                    blue = 255;
                }
                if (localhost.y < nodeY) {
                    start = 90f;
                    extent = -extent();
                } else {
                    start = -90f;
                    extent = extent();
                }
            } else {
                green = 255;
                if (request) {
                    blue = 255;
                }
                if (localhost.y < nodeY) {
                    start = -90f;
                    extent = -extent();
                } else {
                    start = 90f;
                    extent = extent();
                }
            }

            Point2D.Double corner = new Point2D.Double(
                    localhost.x + 2 * dot.width, localhost.y + 2 * dot.height);
            
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
                g.setColor(new Color(red, green, blue, alpha()));
                g.draw(shape);
            }
            
            //g.setStroke(ONE_PIXEL_STROKE);
            //g.setColor(Color.red);
            //g.draw(prxDot);
        }
        
        private void paintLine(Point2D.Double localhost, double width, double height, Graphics2D g) {
            g.setStroke(DEFAULT_STROKE);
            g.setColor(new Color(255, 0, 0, alpha()));
            
            double x1 = localhost.x;
            double y1 = localhost.y;
            double x2 = x1 + (width/(2d*180d)) * extent();
            double y2 = y1;
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }
}
