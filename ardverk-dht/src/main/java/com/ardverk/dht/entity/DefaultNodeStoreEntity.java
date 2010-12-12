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

package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

public class DefaultNodeStoreEntity extends DefaultStoreEntity 
        implements NodeStoreEntity {

    private final NodeEntity nodeEntity;
    
    private final StoreEntity storeEntity;
    
    public DefaultNodeStoreEntity(NodeEntity nodeEntity, StoreEntity storeEntity) {
        super(storeEntity.getStoreResponses(), 
                EntityUtils.getTimeInMillis(nodeEntity, storeEntity), 
                TimeUnit.MILLISECONDS);
        
        this.nodeEntity = nodeEntity;
        this.storeEntity = storeEntity;
    }

    @Override
    public NodeEntity getNodeEntity() {
        return nodeEntity;
    }
    
    public StoreEntity getStoreEntity() {
        return storeEntity;
    }
}