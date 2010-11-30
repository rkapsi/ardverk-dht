package com.ardverk.dht.routing;

public class RouteTableAdapter implements RouteTableListener {

    @Override
    public void handleBucketSplit(Bucket bucket, Bucket left, Bucket right) {
    }

    @Override
    public void handleContactAdded(Bucket bucket, Contact contact) {
    }

    @Override
    public void handleContactReplaced(Bucket bucket, Contact existing,
            Contact contact) {
    }

    @Override
    public void handleContactChanged(Bucket bucket, Contact existing,
            Contact contact) {
    }

    @Override
    public void handleContactRemoved(Bucket bucket, Contact contact) {
    }

    @Override
    public void handleContactCollision(Contact existing, Contact contact) {
    }
}
