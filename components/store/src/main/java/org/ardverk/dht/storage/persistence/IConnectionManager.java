package org.ardverk.dht.storage.persistence;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

interface IConnectionManager extends Closeable {

    public abstract Connection getConnection();

    public abstract Statement createStatement() throws SQLException;

    public abstract PreparedStatement prepareStatement(String sql)
            throws SQLException;

    public abstract void beginTxn() throws SQLException;

    public abstract void endTxn() throws SQLException;

    public abstract void commit() throws SQLException;

}