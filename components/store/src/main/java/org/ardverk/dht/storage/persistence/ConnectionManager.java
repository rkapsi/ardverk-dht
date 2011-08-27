package org.ardverk.dht.storage.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

class ConnectionManager implements IConnectionManager {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static ConnectionManager newInstance() 
            throws SQLException, ClassNotFoundException {
        
        Class.forName("org.hsqldb.jdbcDriver");

        String url = "jdbc:hsqldb:mem:index-" + COUNTER.incrementAndGet();

        // String path = dir.getAbsolutePath() + "/index-" +
        // COUNTER.incrementAndGet();
        // String url = "jdbc:hsqldb:file:" + path;

        String user = "sa";
        String password = "";
        Connection connection = DriverManager
                .getConnection(url, user, password);
        
        return new ConnectionManager(connection);
    }
    
    private final Connection connection;
    
    public ConnectionManager(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void close() {
        Utils.close(connection);
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#getConnection()
     */
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#createStatement()
     */
    @Override
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#prepareStatement(java.lang.String)
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#beginTxn()
     */
    @Override
    public void beginTxn() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#endTxn()
     */
    @Override
    public void endTxn() throws SQLException {
        connection.setAutoCommit(true);
    }
    
    /* (non-Javadoc)
     * @see org.ardverk.dht.storage.persistence.IConnectionManager#commit()
     */
    @Override
    public void commit() throws SQLException {
        connection.commit();
    }
}
