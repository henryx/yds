/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.engines;

import it.application.yds.fetch.streams.InterfaceStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
            url = "jdbc:postgresql://"
                    + this.cfg.getProperty("pg_host") + ":"
                    + this.cfg.getProperty("pg_port") + "/"
                    + this.cfg.getProperty("pg_database");

            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url, this.cfg.getProperty("pg_user"), this.cfg.getProperty("pg_password"));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkDB() {
        PreparedStatement pstmt;
        Statement stmt;
        String queryToVerify, query;
        ResultSet res;
        int count;

        try {
            queryToVerify = "SELECT COUNT(tablename) FROM pg_tables WHERE "
                    + "schemaname = 'public' AND tablename = ?";
            stmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt = this.conn.prepareStatement(queryToVerify);

            pstmt.setString(1, "doc_index");
            res = pstmt.executeQuery();
            res.next();
            count = res.getInt(1);
            if (count == 0) {
                query = "CREATE TABLE doc_index(doc_hash VARCHAR(32), doc_idx TSVECTOR, "
                        + "CONSTRAINT pk_doc_index_1 PRIMARY KEY(doc_hash))";
                stmt.execute(query);
                query = "CREATE INDEX idx_doc_index_1 ON doc_index USING gist(doc_idx)";
                stmt.execute(query);
            }

            pstmt.setString(1, "documents");
            res = pstmt.executeQuery();
            res.next();
            count = res.getInt(1);
            if (count == 0) {
                query = "CREATE TABLE documents(doc_hash VARCHAR(32), "
                        + "doc_name VARCHAR(1024), doc_mime VARCHAR(30), "
                        + "CONSTRAINT pk_documents_1 PRIMARY KEY (doc_hash, doc_name))";
                stmt.execute(query);
            }

            this.conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isHashIndexed(String hash) throws SQLException {
        PreparedStatement pstmt;
        ResultSet res;
        String query;
        int values;

        query = "SELECT count(doc_hash) FROM doc_index where doc_hash = ?";
        pstmt = this.conn.prepareStatement(query);
        pstmt.setString(1, hash);
        res = pstmt.executeQuery();

        res.next();

        values = res.getInt(1);

        if (values > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFileIndexed(String hash, String file) throws SQLException {
        PreparedStatement pstmt;
        ResultSet res;
        String query;
        int count;

        query = "SELECT COUNT(*) FROM documents WHERE doc_hash = ? AND doc_name = ?";

        pstmt = this.conn.prepareStatement(query);
        pstmt.setString(1, hash);
        pstmt.setString(2, file);
        res = pstmt.executeQuery();

        res.next();
        count = res.getInt(1);

        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void indexFile(String hash, String file, String mime) throws SQLException {
        PreparedStatement pstmt;
        String query;

        query = "INSERT INTO documents (doc_hash, doc_name, doc_mime) VALUES (?, ?, ?)";

        pstmt = this.conn.prepareStatement(query);
        pstmt.setString(1, hash);
        pstmt.setString(2, file);
        pstmt.setString(3, mime);
        pstmt.executeUpdate();
    }

    private void indexHash(String hash, String text) throws SQLException {
        PreparedStatement pstmt;
        ResultSet res;
        String query;

        query = "INSERT INTO doc_index(doc_hash, doc_idx) VALUES(?, to_tsvector(?, ?))";

        pstmt = this.conn.prepareStatement(query);
        pstmt.setString(1, hash);
        pstmt.setString(2, this.cfg.getProperty("language"));
        pstmt.setString(3, text);
        pstmt.executeUpdate();
    }

    public void store(InterfaceStream stream) {
        String file, hash, mime, text;
        boolean hashIndexed, fileIndexed;

        try {
            Logger.getLogger(PgEngine.class.getName()).log(Level.FINEST, "{0}: {1}", new Object[]{stream.getFileName(), stream.getHash()});
            file = stream.getFileName();
            hash = stream.getHash();
            mime = stream.getMime();
            text = stream.getStream();
        } catch (IOException ex) {
            Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);

            file = "";
            hash = "";
            mime = "";
            text = "";
        }

        if (!text.equals("")) {
            try {
                hashIndexed = this.isHashIndexed(hash);

                if (hashIndexed) {
                    fileIndexed = this.isFileIndexed(hash, file);

                    if (!fileIndexed) {
                        this.indexFile(hash, file, mime);
                    }
                } else {
                    this.indexHash(hash, text);
                    this.indexFile(hash, file, mime);
                }
                this.conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, "Problems about indexing file {0}", file);
                Logger.getLogger(PgEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
