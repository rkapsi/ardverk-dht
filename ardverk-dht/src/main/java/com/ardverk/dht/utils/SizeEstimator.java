package com.ardverk.dht.utils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;

import com.ardverk.dht.KUID;
import com.ardverk.dht.routing.Contact;
import com.ardverk.dht.routing.RouteTable;
import com.ardverk.logging.LoggerUtils;

/**
 * An utility class to compute the approximate DHT size.
 * 
 * http://azureus.cvs.sourceforge.net/azureus/azureus2/com/aelitis/azureus/core/dht/control/impl/DHTControlImpl.java
 */
public class SizeEstimator {

    private static final Logger LOG 
        = LoggerUtils.getLogger(SizeEstimator.class);
    
    private static final int MIN_NODE_COUNT = 3;
    
    /** History of local estimations */
    private List<BigInteger> localSizeHistory = new LinkedList<BigInteger>();

    /** History of remote estimations (sizes we received with pongs) */
    private List<BigInteger> remoteSizeHistory = new LinkedList<BigInteger>();

    /** Current estimated size */
    private BigInteger estimatedSize = BigInteger.ZERO;

    /** The time when we made the last estimation */
    private long localEstimateTime = 0L;

    /** The time when we updated the estimated DHT size */
    private long updateEstimatedSizeTime = 0L;

    private final BigInteger maximum;
    
    public SizeEstimator(KUID maximum) {
        if (maximum == null) {
            throw new NullPointerException("maximum");
        }
        
        this.maximum = maximum.toBigInteger();
    }
    
    /**
     * Clears the history and sets everything to
     * its initial state
     */
    public synchronized void clear() {
        estimatedSize = BigInteger.ZERO;
        localEstimateTime = 0L;
        updateEstimatedSizeTime = 0L;
        
        localSizeHistory.clear();
        remoteSizeHistory.clear();
    }

    /**
     * Returns the approximate DHT size
     */
    public synchronized BigInteger getEstimatedSize(RouteTable routeTable) {
        if (routeTable != null && 
                (System.currentTimeMillis() - localEstimateTime) 
                    >= ContextSettings.ESTIMATE_NETWORK_SIZE_EVERY.getValue()) {
            
            SelectMode mode = SelectMode.ALL;
            if (ContextSettings.ESTIMATE_WITH_LIVE_NODES_ONLY.getValue()) {
                mode = SelectMode.ALIVE;
            }
            
            KUID localNodeId = routeTable.getLocalNode().getNodeID();
            Collection<Contact> nodes = routeTable.select(localNodeId, 
                    KademliaSettings.REPLICATION_PARAMETER.getValue(), mode);
            
            updateSize(nodes);
            localEstimateTime = System.currentTimeMillis();
        }
        
        return estimatedSize;
    }

    /**
     * Adds the approximate DHT size as returned by a remote Node.
     * The average of the remote DHT sizes is incorporated into into
     * our local computation.
     */
    public synchronized void addEstimatedRemoteSize(BigInteger remoteSize) {
        if (!ContextSettings.COUNT_REMOTE_SIZE.getValue()) {
            // Clear the list of remotely estimated DHT sizes as they're
            // no longer needed.
            remoteSizeHistory.clear();
            return;
        }
        
        if (remoteSize.compareTo(BigInteger.ZERO) == 0) {
            return;
        }
        
        if (remoteSize.compareTo(BigInteger.ZERO) < 0
                || remoteSize.compareTo(maximum) > 0) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(remoteSize + " is an illegal argument");
            }
            return;
        }
        
        remoteSizeHistory.add(remoteSize);
        
        // Adjust the size of the List. The Setting is SIMPP-able
        // and may change!
        int maxRemoteHistorySize = ContextSettings.MAX_REMOTE_HISTORY_SIZE.getValue();
        while (remoteSizeHistory.size() > maxRemoteHistorySize
                && !remoteSizeHistory.isEmpty()) {
            remoteSizeHistory.remove(0);
        }
    }

    /**
     * Updates the estimated DHT size with the given List of Contacts.
     * If <tt>nodes</tt> is null it will use the local RouteTable to
     * estimate the DHT size
     */
    public synchronized void updateSize(Collection<? extends Contact> nodes) {
        if ((System.currentTimeMillis() - updateEstimatedSizeTime) 
                >= ContextSettings.UPDATE_NETWORK_SIZE_EVERY.getValue()) {

            if (nodes.size() >= MIN_NODE_COUNT) {
                estimatedSize = computeSize(nodes);
                updateEstimatedSizeTime = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * Computes and returns the approximate DHT size based 
     * on the given List of Contacts
     */
    public synchronized BigInteger computeSize(Collection<? extends Contact> nodes) {
        
        // Works only with more than two Nodes
        if (nodes.size() < MIN_NODE_COUNT) {
            // There's always us!
            return BigInteger.ONE.max(BigInteger.valueOf(nodes.size()));
        }

        // Get the Iterator. We assume the Contacts are sorted by
        // their xor distance!
        Iterator<? extends Contact> contacts = nodes.iterator();
        
        // See Azureus DHTControlImpl.estimateDHTSize()
        // Di = nearestId xor NodeIDi
        // Dc = sum(i * Di) / sum(i * i)
        // Size = 2**160 / Dc

        BigInteger sum1 = BigInteger.ZERO;
        BigInteger sum2 = BigInteger.ZERO;
        
        // The algorithm works relative to the ID space.
        KUID nearestId = contacts.next().getContactId();
        
        // We start 1 because the nearest Node is the 0th item!
        for (int i = 1; contacts.hasNext(); i++) {
            Contact node = contacts.next();

            BigInteger distance = nearestId.xor(node.getContactId()).toBigInteger();
            BigInteger j = BigInteger.valueOf(i);

            sum1 = sum1.add(j.multiply(distance));
            sum2 = sum2.add(j.pow(2));
        }

        BigInteger estimatedSize = BigInteger.ZERO;
        if (!sum1.equals(BigInteger.ZERO)) {
            estimatedSize = maximum.multiply(sum2).divide(sum1);
        }

        // And there is always us!
        estimatedSize = BigInteger.ONE.max(estimatedSize);
        
        // Get the average of the local estimations
        BigInteger localSize = BigInteger.ZERO;
        localSizeHistory.add(estimatedSize);
        
        // Adjust the size of the List. The Setting is SIMPP-able
        // and may change!
        int maxLocalHistorySize = ContextSettings.MAX_LOCAL_HISTORY_SIZE.getValue();
        while(localSizeHistory.size() > maxLocalHistorySize
                && !localSizeHistory.isEmpty()) {
            localSizeHistory.remove(0);
        }
        
        if (!localSizeHistory.isEmpty()) {
            BigInteger localSizeSum = BigInteger.ZERO;
            for (BigInteger size : localSizeHistory) {
                localSizeSum = localSizeSum.add(size);
            }

            localSize = localSizeSum.divide(BigInteger.valueOf(localSizeHistory.size()));
        }
        
        // Get the combined average
        // S = (localEstimation + sum(remoteEstimation[i]))/count
        BigInteger combinedSize = localSize;
        if (ContextSettings.COUNT_REMOTE_SIZE.getValue()) {
            // Prune all duplicates and sort the values
            Set<BigInteger> remoteSizeSet = new TreeSet<BigInteger>(remoteSizeHistory);
            
            if (remoteSizeSet.size() >= 3) {
                BigInteger[] remote = remoteSizeSet.toArray(new BigInteger[0]);
                
                // Skip the smallest and largest values
                int count = 1;
                int skip = ContextSettings.SKIP_REMOTE_ESTIMATES.getValue();
                for (int i = skip; (skip >= 0) && (i < (remote.length-skip)); i++) {
                    combinedSize = combinedSize.add(remote[i]);
                    count++;
                }
                combinedSize = combinedSize.divide(BigInteger.valueOf(count));
                
                // Make sure we didn't exceed the MAXIMUM number as
                // we made an addition with the local estimation which
                // might be already 2**160 bit!
                combinedSize = combinedSize.min(maximum);
            }
        }

        // There is always us!
        return BigInteger.ONE.max(combinedSize);
    }
}