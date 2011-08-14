package org.ardverk.dht.storage.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ardverk.dht.KUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SQLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SQLUtils.class);
    
    private SQLUtils() {}
    
    public static void setBytes(PreparedStatement ps, int index, KUID value) throws SQLException {
        ps.setBytes(index, value.getBytes(false));
    }
    
    public static void beginTxn(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }
    
    public static void endTxn(Connection connection) throws SQLException {
        connection.setAutoCommit(true);
    }
    
    public static void close(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                LOG.trace("SQLException", e);
            }
        }
    }
    
    public static void close(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException e) {
                LOG.trace("SQLException", e);
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.trace("SQLException", e);
            }
        }
    }
}
