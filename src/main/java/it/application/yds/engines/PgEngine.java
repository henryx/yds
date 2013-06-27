/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */
package it.application.yds.engines;

import it.application.yds.Main;
import it.application.yds.fetch.streams.Stream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

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
            // url=jdbc:postgresql://127.0.0.1:5432/yds
            url = "jdbc:postgresql://"
                    + this.cfg.getProperty("pg_host") + ":"
                    + this.cfg.getProperty("pg_port") + "/"
                    + this.cfg.getProperty("pg_database");

            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url, this.cfg.getProperty("pg_user"), this.cfg.getProperty("pg_password"));
        } catch (ClassNotFoundException ex) {
            Main.logger.error(null, ex);
        }
    }

    private void checkDB() throws SQLException {
        PreparedStatement pstmt;
        Statement stmt;
        String queryToVerify, query;
        ResultSet res;
        int count;

        pstmt = null;
        stmt = null;
        res = null;

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
                query = "CREATE TABLE public.doc_index(doc_hash VARCHAR(32), doc_idx TSVECTOR, "
                        + "CONSTRAINT pk_doc_index_1 PRIMARY KEY(doc_hash))";
                stmt.execute(query);
                query = "CREATE INDEX idx_doc_index_1 ON public.doc_index USING gist(doc_idx)";
                stmt.execute(query);
            }

            pstmt.setString(1, "documents");
            res = pstmt.executeQuery();
            res.next();
            count = res.getInt(1);
            if (count == 0) {
                query = "CREATE TABLE public.documents(doc_hash VARCHAR(32), "
                        + "doc_name VARCHAR(1024), doc_mime VARCHAR(72), "
                        + "CONSTRAINT pk_documents_1 PRIMARY KEY (doc_hash, doc_name))";
                stmt.execute(query);
            }

            this.conn.commit();

        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
            if (stmt instanceof Statement) {
                stmt.close();
            }
        }
    }

    private boolean isHashIndexed(String hash) throws SQLException {
        PreparedStatement pstmt;
        ResultSet res;
        String query;
        int values;

        pstmt = null;
        res = null;

        query = "SELECT count(doc_hash) FROM doc_index where doc_hash = ?";
        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            res = pstmt.executeQuery();

            res.next();
            values = res.getInt(1);

        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }

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

        pstmt = null;
        res = null;
        query = "SELECT COUNT(*) FROM documents WHERE doc_hash = ? AND doc_name = ?";

        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            pstmt.setString(2, file);
            res = pstmt.executeQuery();

            res.next();
            count = res.getInt(1);

        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void indexFile(String hash, String file, String mime) throws SQLException {
        PreparedStatement pstmt;
        String query;

        pstmt = null;
        query = "INSERT INTO documents (doc_hash, doc_name, doc_mime) VALUES (?, ?, ?)";
        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            pstmt.setString(2, file);
            pstmt.setString(3, mime);
            pstmt.executeUpdate();
        } finally {
            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }
    }

    private void indexHash(String hash, String text) throws SQLException {
        PreparedStatement pstmt;
        String query;

        pstmt = null;
        query = "INSERT INTO doc_index(doc_hash, doc_idx) VALUES(?, to_tsvector(?, ?))";

        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            pstmt.setString(2, this.cfg.getProperty("language"));
            pstmt.setString(3, text);
            pstmt.executeUpdate();
        } finally {
            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }
    }

    @Override
    public void store(Stream stream) {
        String file, hash, mime, text;
        boolean hashIndexed, fileIndexed;

        try {
            Main.logger.debug(stream.getFileName() + ": " + stream.getHash());
            file = stream.getFileName();
            hash = stream.getHash();
            mime = stream.getMime();
            text = stream.getStream();
        } catch (IOException ex) {
            Main.logger.error(null, ex);

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
                Main.logger.error("Problems about indexing file " + file, ex);
                try {
                    this.conn.rollback();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public ArrayList<String> getStoredHash() throws SQLException {
        ArrayList<String> result;
        Statement stmt;
        ResultSet res;
        String query;

        stmt = null;
        res = null;
        result = new ArrayList<>();

        query = "SELECT doc_hash FROM doc_index";

        try {
            stmt = this.conn.createStatement();
            res = stmt.executeQuery(query);

            while (res.next()) {
                result.add(res.getString(1));
            }
        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (stmt instanceof Statement) {
                stmt.close();
            }
        }
        return result;
    }

    @Override
    public void removeHash(String hash) throws SQLException {
        PreparedStatement pstmt;
        String query;

        pstmt = null;
        try {
            query = "DELETE FROM documents WHERE doc_hash = ?";
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        } finally {
            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }

        try {
            query = "DELETE FROM doc_index WHERE doc_hash = ?";
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        } finally {
            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }

        this.conn.commit();
    }

    @Override
    public ArrayList<String> getStoredFiles(String hash) throws SQLException {
        ArrayList<String> result;
        PreparedStatement pstmt;
        ResultSet res;
        String query;

        pstmt = null;
        res = null;

        result = new ArrayList<>();
        query = "SELECT doc_name FROM documents where doc_hash = ?";

        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, hash);
            res = pstmt.executeQuery();

            while (res.next()) {
                result.add(res.getString(1));
            }
        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }

        return result;
    }

    @Override
    public ArrayList<String> queryHash(String data) throws SQLException {
        ArrayList<String> result;
        PreparedStatement pstmt;
        ResultSet res;
        String query;

        pstmt = null;
        res = null;
        result = new ArrayList<>();

        query = "SELECT doc_hash FROM doc_index WHERE doc_idx @@ to_tsquery(?, ?)";

        try {
            pstmt = this.conn.prepareStatement(query);
            pstmt.setString(1, this.cfg.getProperty("language"));
            pstmt.setString(2, data);
            res = pstmt.executeQuery();

            while (res.next()) {
                result.add(res.getString(1));
            }
        } finally {
            if (res instanceof ResultSet) {
                res.close();
            }

            if (pstmt instanceof PreparedStatement) {
                pstmt.close();
            }
        }

        return result;
    }
}
