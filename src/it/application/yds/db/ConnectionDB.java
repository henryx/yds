/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author enrico
 */
public class ConnectionDB {
    private String hostname;
    private String port;
    private String username;
    private String password;
    private String dbname;

    private Connection conn;

    public ConnectionDB() {
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param dbname the dbname to set
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    public void openConnection() throws SQLException {
        String dbDriver;
        String dbUrl;

        dbDriver = "org.postgresql.Driver";
        dbUrl = "jdbc:postgresql://" +
                this.hostname +
                ":" +
                this.port +
                "/" +
                this.dbname;

        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException cnf) {}
        this.conn = DriverManager.getConnection(dbUrl, this.username, this.password);
    }
}
