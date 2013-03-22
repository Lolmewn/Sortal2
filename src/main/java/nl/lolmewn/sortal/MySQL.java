/*
 * MySQL.java
 * 
 * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
 * 
 * Sortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sortal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
 */

package nl.lolmewn.sortal;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lolmewn <info@lolmewn.nl>
 */

public class MySQL {

    private String host, username, password, database, prefix;
    private int port;
    private boolean fault;
    private Main plugin;
    
    private JDCConnection con;

    public MySQL(Main main, String host, int port, String username, String password, String database, String prefix) {
        this.plugin = main;
        this.prefix = prefix;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.setupDatabase();
        this.validateTables();
    }

    private void setupDatabase() {
        if (this.isFault()) {
            return;
        }
        this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "warps"
                + "(name varchar(255) PRIMARY KEY NOT NULL, "
                + "world varchar(255) NOT NULL, "
                + "x DOUBLE NOT NULL, "
                + "y DOUBLE NOT NULL, "
                + "z DOUBLE NOT NULL, "
                + "yaw float, "
                + "pitch float, "
                + "price int, "
                + "uses int,"
                + "used int,"
                + "usedTotalBased boolean,"
                + "owner varchar(255))");
        this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "signs"
                + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "world varchar(255) NOT NULL, "
                + "x int NOT NULL, "
                + "y int NOT NULL, "
                + "z int NOT NULL, "
                + "warp varchar(255) NOT NULL, "
                + "price int,"
                + "uses int,"
                + "used int,"
                + "usedTotalBased boolean,"
                + "owner varchar(255))");
        this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.prefix + "users"
                + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "player varchar(255) NOT NULL,"
                + "used int NOT NULL,"
                + "warp varchar(255),"
                + "x int, y int, z int, world varchar(255))");
                //Example query: [Lolmewn, 2, test, 0,0,0,null], [Lolmewn, 3, null, 50,80,50,world]
    }
    
    public JDCConnection getConnection(){
        if (this.con != null) {
            if (this.con.lease()) {
                if (con.isValid()) {
                    return con;
                }
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            this.plugin.debug("Creating connection to MySQL database...");
            try {
                return (this.con = new JDCConnection(DriverManager.getConnection(url, username, password)));
            } catch (SQLException ex) {
                Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.setFault(true);
        } finally {
            if (this.fault) {
                System.out.println("MySQL connection failed!");
            }
        }
        return null;
    }

    public boolean isFault() {
        return fault;
    }

    private void setFault(boolean fault) {
        this.fault = fault;
    }

    public int executeStatement(String statement) {
        if (isFault()) {
            System.out.println("[Sortal] Can't execute statement, something wrong with connection");
            return 0;
        }
        if(this.plugin.getSettings().isDebug()){
            this.plugin.debug("Executing query: " + statement);
        }
        try {
            Connection con = this.getConnection();
            Statement state = con.createStatement();
            int re = state.executeUpdate(statement);
            state.close();
            return re;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResultSet executeQuery(String statement) {
        if (isFault()) {
            System.out.println("[Sortal] Can't execute query, something wrong with connection");
            return null;
        }
        if (statement.toLowerCase().startsWith("update") || statement.toLowerCase().startsWith("insert") || statement.toLowerCase().startsWith("delete")) {
            this.executeStatement(statement);
            return null;
        }
        if(this.plugin.getSettings().isDebug()){
            this.plugin.debug("Executing query: " + statement);
        }
        try {
            Connection con = this.getConnection();
            Statement state = con.createStatement();
            ResultSet set = state.executeQuery(statement);
            con.close();
            return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (isFault()) { 
           System.out.println("[Sortal] Can't close connection, something wrong with it");
            return;
        }
        this.con.close();
    }

    private void validateTables() {
        if (this.isFault()) {
            return;
        }
        this.checkColumn(this.prefix + "warps", "uses", "int");
        this.checkColumn(this.prefix + "warps", "used", "int");
        this.checkColumn(this.prefix + "warps", "owner", "varchar(255)");
        this.checkColumn(this.prefix + "warps", "usedTotalBased", "boolean");
        this.checkColumn(this.prefix + "signs", "uses", "int");
        this.checkColumn(this.prefix + "signs", "used", "int");
        this.checkColumn(this.prefix + "signs", "owner", "varchar(255)");
        this.checkColumn(this.prefix + "signs", "usedTotalBased", "boolean");
        this.checkColumn(this.prefix + "signs", "isPrivate", "boolean");
        this.checkColumn(this.prefix + "signs", "privateUsers", "text");
        this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN x DOUBLE NOT NULL");
        this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN y DOUBLE NOT NULL");
        this.executeStatement("ALTER TABLE " + this.prefix + "warps MODIFY COLUMN z DOUBLE NOT NULL");
    }
    
    private void checkColumn(String table, String column, String type){
        ResultSet set = this.executeQuery("SELECT * FROM " + table + " LIMIT 1");
        if(set == null){
            return;
        }
        try {
            while(set.next()){
                set.getObject(column);
            }
        } catch (SQLException ex) {
            System.out.println("Adding column " + column + ",type " + type + " to table " + table + "..");
            this.executeStatement("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            System.out.println("Added column " + column + ",type " + type + " to table " + table + " succesfully");
        }
        /*
         * try
    {
      DatabaseMetaData d = this.connection.getMetaData();
      rs = d.getColumns(null, null, table, null);

      while (rs.next()) {
        col.add(rs.getString("COLUMN_NAME"));
      }
      return col;
    }
         */
    }
    
    private class JDCConnection implements Connection {

        private final Connection conn;
        private boolean inuse;
        private long timestamp;
        private int networkTimeout;
        private String schema;

        JDCConnection(Connection conn) {
            this.conn = conn;
            inuse = false;
            timestamp = 0;
            networkTimeout = 30;
            schema = "default";
        }

        @Override
        public void clearWarnings() throws SQLException {
            conn.clearWarnings();
        }

        @Override
        public void close() {
            inuse = false;
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
            } catch (final SQLException ex) {
                terminate();
            }
        }

        @Override
        public void commit() throws SQLException {
            conn.commit();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return conn.createArrayOf(typeName, elements);
        }

        @Override
        public Blob createBlob() throws SQLException {
            return conn.createBlob();
        }

        @Override
        public Clob createClob() throws SQLException {
            return conn.createClob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return conn.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return conn.createSQLXML();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return conn.createStatement();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return conn.createStruct(typeName, attributes);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return conn.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException {
            return conn.getCatalog();
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return conn.getClientInfo();
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return conn.getClientInfo(name);
        }

        @Override
        public int getHoldability() throws SQLException {
            return conn.getHoldability();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return conn.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return conn.getTransactionIsolation();
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return conn.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return conn.getWarnings();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return conn.isClosed();
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return conn.isReadOnly();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return conn.isValid(timeout);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return conn.isWrapperFor(iface);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return conn.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return conn.prepareCall(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return conn.prepareStatement(sql);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return conn.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return conn.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return conn.prepareStatement(sql, columnNames);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            conn.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback() throws SQLException {
            conn.rollback();
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            conn.rollback(savepoint);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            conn.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            conn.setCatalog(catalog);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            conn.setClientInfo(properties);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            conn.setClientInfo(name, value);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            conn.setHoldability(holdability);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            conn.setReadOnly(readOnly);
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return conn.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return conn.setSavepoint(name);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            conn.setTransactionIsolation(level);
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            conn.setTypeMap(map);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return conn.unwrap(iface);
        }

        @SuppressWarnings("unused")
        public int getNetworkTimeout() throws SQLException {
            return networkTimeout;
        }

        @SuppressWarnings("unused")
        public void setNetworkTimeout(Executor exec, int timeout) throws SQLException {
            networkTimeout = timeout;
        }

        @SuppressWarnings("unused")
        public void abort(Executor exec) throws SQLException {
            // Not implemented really...
        }

        @SuppressWarnings("unused")
        public String getSchema() throws SQLException {
            return schema;
        }

        @SuppressWarnings("unused")
        public void setSchema(String str) throws SQLException {
            schema = str;
        }

        long getLastUse() {
            return timestamp;
        }

        boolean inUse() {
            return inuse;
        }

        boolean isValid() {
            try {
                return conn.isValid(1);
            } catch (final SQLException ex) {
                return false;
            }
        }

        synchronized boolean lease() {
            if (inuse) {
                return false;
            }
            inuse = true;
            timestamp = System.currentTimeMillis();
            return true;
        }

        void terminate() {
            try {
                conn.close();
            } catch (final SQLException ex) {
            }
        }
    }
}