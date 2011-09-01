package org.ardverk.dht.storage.sql;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

class ConnectionManager implements Closeable {
    
    public static ConnectionManager newInstance(File dir) 
            throws SQLException, ClassNotFoundException {
        
        Class.forName("org.hsqldb.jdbcDriver");
        
        String path = dir.getAbsolutePath() + "/index";
        String url = "jdbc:hsqldb:file:" + path;

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
    
    public Connection getConnection() {
        return connection;
    }
    
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
    
    public void beginTxn() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    public void endTxn() throws SQLException {
        connection.setAutoCommit(true);
    }
    
    public void commit() throws SQLException {
        connection.commit();
    }
}
