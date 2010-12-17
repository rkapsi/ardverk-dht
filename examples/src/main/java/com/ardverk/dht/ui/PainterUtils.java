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
import java.awt.Stroke;
import java.util.List;

import com.ardverk.dht.message.Message;
import com.ardverk.dht.message.NodeRequest;
import com.ardverk.dht.message.NodeResponse;
import com.ardverk.dht.message.PingRequest;
import com.ardverk.dht.message.PingResponse;
import com.ardverk.dht.message.RequestMessage;
import com.ardverk.dht.message.StoreRequest;
import com.ardverk.dht.message.StoreResponse;
import com.ardverk.dht.message.ValueRequest;
import com.ardverk.dht.message.ValueResponse;

class PainterUtils {

    private static int MAX_SIZE = 2048;
    
    public static final int SCALE = 10;

    public static final Stroke DEFAULT_STROKE = new BasicStroke(1.0f);

    public static final Stroke TWO_PIXEL_STROKE = new BasicStroke(2.0f);
    
    public static final long ATTACK = 250L;
    
    public static final long RELEASE = 2750L;
    
    public static final long DURATION = ATTACK + RELEASE;
    
    public static final float DOT_SIZE = 6f;
    
    private static final Color PING_REQUEST = Color.RED;
    
    private static final Color PING_RESPONSE = PING_REQUEST.darker();
    
    private static final Color NODE_REQUEST = Color.GREEN;
    
    private static final Color NODE_RESPONSE = NODE_REQUEST.darker();
    
    private static final Color VALUE_REQUEST = Color.BLUE;
    
    private static final Color VALUE_RESPONSE = VALUE_REQUEST.darker();
    
    private static final Color STORE_REQUEST = Color.CYAN;
    
    private static final Color STORE_RESPONSE = STORE_REQUEST.darker();

    private PainterUtils() {}

    public static Color alpha(Color color, int alpha) {
        int rgb = (color.getRGB() & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        return new Color(rgb, true);
    }
    
    public static Color getColorForMessage(Message message) {
        if (message instanceof PingRequest) {
            return PING_REQUEST;
        } else if (message instanceof PingResponse) {
            return PING_RESPONSE;
        } else if (message instanceof NodeRequest) {
            return NODE_REQUEST;
        } else if (message instanceof NodeResponse) {
            return NODE_RESPONSE;
        } else if (message instanceof ValueRequest) {
            return VALUE_REQUEST;
        } else if (message instanceof ValueResponse) {
            return VALUE_RESPONSE;
        } else if (message instanceof StoreRequest) {
            return STORE_REQUEST;
        } else if (message instanceof StoreResponse) {
            return STORE_RESPONSE;
        }
        
        return Color.WHITE;
    }
    
    public static Stroke getStrokeForMessage(Message message) {
        float dash_phase = (float)Math.random() * 10f;
        
        if (message instanceof RequestMessage) {
            return PainterUtils.DEFAULT_STROKE;
        } else {
            return new BasicStroke(1.0f, BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_ROUND, 10.0f, 
                new float[]{ 2f, 4f }, dash_phase);
        }
    }
    
    public static void adjustSize(List<?> nodes) {
        while (!nodes.isEmpty() && nodes.size() >= MAX_SIZE) {
            nodes.remove(0);
        }
    }
}