package com.ardverk.dht.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.ardverk.dht.KUID;
import com.ardverk.dht.message.Message;

public class JuicePainter extends AbstractPainter {

    private static final Random GENERATOR = new Random();
    
    private final List<Node> nodes 
        = Collections.synchronizedList(new LinkedList<Node>());
    
    private final Point2D.Double localhost = new Point2D.Double();
    
    private final Ellipse2D.Double ellipse = new Ellipse2D.Double();
    
    private final Ellipse2D.Double dot = new Ellipse2D.Double();
    
    public JuicePainter(KUID localhostId) {
        super(localhostId);
    }
    
    @Override
    protected void paint(Component c, Graphics2D g) {
        
        double width = c.getWidth();
        double height = c.getHeight();
        
        double gap = 50d;
        double radius = Math.max(Math.min(width/2d, height/2d) - gap, gap);
        
        double arc_x = width/2d-radius;
        double arc_y = height/2d-radius;
        double arc_width = 2d*radius;
        double arc_height = 2d*radius;
        
        g.setColor(Color.orange);
        g.setStroke(PainterUtils.TWO_PIXEL_STROKE);
        
        ellipse.setFrame(arc_x, arc_y, arc_width, arc_height);
        g.draw(ellipse);
        
        double fi = position(localhostId, 2d*Math.PI) - Math.PI/2d;
        double dx = width/2d + radius * Math.cos(fi);
        double dy = height/2d + radius * Math.sin(fi);
        
        localhost.setLocation(dx, dy);
        
        dot.setFrame(dx - PainterUtils.DOT_SIZE/2d, 
            dy - PainterUtils.DOT_SIZE/2d, 
            PainterUtils.DOT_SIZE, 
            PainterUtils.DOT_SIZE);
        
        synchronized (nodes) {
            for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
                if (it.next().paint(localhost, width, height, radius, g)) {
                    it.remove();
                }
            }
        }
        
        g.setColor(Color.ORANGE);
        g.setStroke(PainterUtils.DEFAULT_STROKE);
        g.fill(dot);
    }
    
    @Override
    public void handleEvent(EventType type, KUID contactId, Message message) {
        if (contactId != null) {
            nodes.add(new Node(contactId, message));
        }
    }
    
    private class Node {
        
        private final long creationTime = System.currentTimeMillis();

        private final Point2D.Double remote = new Point2D.Double();
        
        private final Point2D.Double point = new Point2D.Double();
        
        private final QuadCurve2D.Double curve = new QuadCurve2D.Double();
        
        private final Ellipse2D.Double circle = new Ellipse2D.Double();
        
        private final Ellipse2D.Double prxDot = new Ellipse2D.Double();
        
        private final KUID contactId;
        
        private final int noise;
        
        private final Stroke stroke;
        
        private final Color color;
        
        public Node(KUID contactId, Message message) {
            this.contactId = contactId;
            
            this.stroke = PainterUtils.getStrokeForMessage(message);
            this.color = PainterUtils.getColorForMessage(message);
            
            int noise = GENERATOR.nextInt(50);
            if (GENERATOR.nextBoolean()) {
                noise = -noise;
            }
            this.noise = noise;
        }
        
        private int alpha() {
            long delta = System.currentTimeMillis() - creationTime;
            
            if (delta < PainterUtils.ATTACK) {
                return (int)(255f/PainterUtils.ATTACK * delta);
            }
            
            return Math.max(255 - (int)(255f/PainterUtils.DURATION * delta), 0);
        }
        
        private double radius() {
            final double r = 20d;
            long delta = System.currentTimeMillis() - creationTime;
            if (delta < PainterUtils.DURATION) {
                return r/PainterUtils.DURATION * delta;
            }
            return r;
        }
        
        public boolean paint(Point2D.Double localhost, 
                double width, double height, double radius, Graphics2D g2) {
            
            double cx = width/2d;
            double cy = height/2d;
            
            double fi = position(contactId, 2d*Math.PI) - Math.PI/2d;
            
            double dx = cx + radius * Math.cos(fi);
            double dy = cy + radius * Math.sin(fi);
            
            remote.setLocation(dx, dy);
            
            Point2D.Double corner = new Point2D.Double(
                    localhost.x + 3 * dot.width, localhost.y + 3 * dot.height);
            
            this.prxDot.setFrameFromCenter(localhost, corner);
            
            Shape shape = null;
            if (!prxDot.contains(remote)) {
                point.setLocation(cx+noise, cy+noise);
                curve.setCurve(localhost, point, remote);
                shape = curve;
            } else {
                double r = radius();
                point.setLocation(localhost.x+r, localhost.y+r);
                circle.setFrameFromCenter(localhost, point);
                shape = circle;
            }
            
            if (shape != null) {
                g2.setStroke(stroke);
                g2.setColor(PainterUtils.alpha(color, alpha()));
                g2.draw(shape);
            }
            
            return System.currentTimeMillis() - creationTime >= PainterUtils.DURATION;
        }
    }
}
