package org.ardverk.dht.storage;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Index implements Closeable {

    private static final String CREATE_KEYS
        = "CREATE TABLE keys ("
        + "id BINARY PRIMARY KEY,"
        + "key VARCHAR(16384) UNIQUE"
        + ")";
    
    private static final String CREATE_VALUES
        = "CREATE TABLE entries (" // Can't use values because it's a reserved keyword
        + "id BIGINT PRIMARY KEY,"
        + "kid BINARY FOREIGN KEY REFERENCES keys(id),"
        + "key VARCHAR(16384)"
        + ")";
    
    private static final String CREATE_PROPERTIES 
        = "CREATE TABLE properties ("
        + "id BIGINT PRIMARY KEY,"
        + "vid BIGINT FOREIGN KEY REFERENCES entries(id),"
        + "name VARCHAR(256),"
        + "value VARCHAR(16384),"
        + ")";
    
    public static Index create(File dir) {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            
            String url = "jdbc:hsqldb:mem:index";
            String user = "sa";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);
            
            Statement statement = connection.createStatement();
            statement.execute(CREATE_KEYS);
            statement.execute(CREATE_VALUES);
            statement.execute(CREATE_PROPERTIES);
            statement.close();
            
            return new Index(connection);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFoundException", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException", e);
        }
    }
    
    private final Connection connection;
    
    private Index(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException err) {
            throw new IOException("SQLException", err);
        }
    }
    
    public static void main(String[] args) {
        Index index = Index.create(null);
    }
}
