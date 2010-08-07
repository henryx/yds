/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.fetch.engines;

import it.application.yds.fetch.streams.InterfaceStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class PgEngine implements Engine {
    private Connection conn;
    private Properties cfg;

    public PgEngine(Properties cfg) throws SQLException {
        this.cfg = cfg;

        this.openConnection();
        this.checkDB();
    }

    private void openConnection() throws SQLException {
        String url;

        try {
            // url=jdbc:postgresql://192.168.1.2:5432/conto
            url = "jdbc:postgresql://" +
                    this.cfg.getProperty("pg_host") + ":" +
                    this.cfg.getProperty("pg_port") + "/" +
                    this.cfg.getProperty("pg_database");

            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url, this.cfg.getProperty("pg_user"), this.cfg.getProperty("pg_password"));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkDB() {
        Statement stmt;
        String query;
        ResultSet res;
        int count;

        try {
            query = "SELECT COUNT(tablename) FROM pg_tables WHERE " +
                 "schemaname = 'public' AND tablename = 'doc_index'";
            stmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            res = stmt.executeQuery(query);

            res.next();
            count = res.getInt(1);

            if (count == 0) {
                query = "CREATE TABLE doc_index(doc_hash VARCHAR(255), doc_idx TSVECTOR)";
                stmt.execute(query);
                query = "CREATE INDEX idx_doc_index_1 ON doc_index USING gist(doc_idx)";
                stmt.execute(query);
                this.conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void store(InterfaceStream stream) {
        String file, hash, query, text;

        Logger.getLogger(PgEngine.class.getName()).log(Level.FINEST, "{0}: {1}", new Object[]{stream.getFileName(), stream.getHash()});

        file = stream.getFileName();
        hash = stream.getHash();
        text = stream.getStream();

        query = "SELECT count() ";
    }
}
