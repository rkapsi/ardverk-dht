package com.ardverk.dht.routing;

public interface RouteTableListener {

    public void handleBucketSplit(Bucket bucket, Bucket left, Bucket right);
    
    public void handleContactAdded(Bucket bucket, Contact contact);
    
    public void handleContactReplaced(Bucket bucket, Contact existing, Contact contact);
    
    public void handleContactChanged(Bucket bucket, Contact existing, Contact contact);
    
    public void handleContactRemoved(Bucket bucket, Contact contact);
    
    public void handleContactCollision(Contact existing, Contact contact);
}
