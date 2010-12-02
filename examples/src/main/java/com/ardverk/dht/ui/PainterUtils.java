package com.ardverk.dht.ui;

import java.awt.BasicStroke;
import java.awt.Stroke;

import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;

class PainterUtils {

    private PainterUtils() {}

    public static Stroke getStrokeForMessage(Message message) {
        float dash_phase = (float)Math.random() * 10f;
        
        if (message instanceof PingRequest
                || message instanceof PingResponse) {
            return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 10.0f, 
                    new float[]{ 2f, 2f }, dash_phase);
        } else if (message instanceof NodeRequest
                || message instanceof NodeResponse) {
            return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 10.0f, 
                    new float[]{ 1f, 5f, 5f }, dash_phase);
        } else if (message instanceof ValueRequest
                || message instanceof ValueResponse) {
            return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 10.0f, 
                    new float[]{ 5f, 5f }, dash_phase);
        } else if (message instanceof StoreRequest
                || message instanceof StoreResponse) {
            return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 10.0f, 
                    new float[]{ 5f, 3f }, dash_phase);
        }
        
        return JuicePainter.DEFAULT_STROKE;
    }
}
