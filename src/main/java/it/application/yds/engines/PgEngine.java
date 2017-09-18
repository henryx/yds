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
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
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
        String queryToVerify, query;
        int count;

        queryToVerify = "SELECT COUNT(tablename) FROM pg_tables WHERE "
                + "schemaname = 'public' AND tablename = ?";
        try (Statement stmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             PreparedStatement pstmt = this.conn.prepareStatement(queryToVerify)
        ) {

            pstmt.setString(1, "doc_index");
            try (ResultSet res = pstmt.executeQuery()) {
                res.next();
                count = res.getInt(1);

                if (count == 0) {
                    query = "CREATE TABLE public.doc_index(doc_hash VARCHAR(32), doc_idx TSVECTOR, "
                            + "CONSTRAINT pk_doc_index_1 PRIMARY KEY(doc_hash))";
                    stmt.execute(query);
                    query = "CREATE INDEX idx_doc_index_1 ON public.doc_index USING gist(doc_idx)";
                    stmt.execute(query);
                }
            }

            pstmt.setString(1, "documents");
            try (ResultSet res = pstmt.executeQuery()) {
                res.next();
                count = res.getInt(1);
                if (count == 0) {
                    query = "CREATE TABLE public.documents(doc_hash VARCHAR(32), "
                            + "doc_name VARCHAR(1024), doc_mime VARCHAR(72), "
                            + "CONSTRAINT pk_documents_1 PRIMARY KEY (doc_hash, doc_name))";
                    stmt.execute(query);
                }

                this.conn.commit();
            }
        }
    }

    private boolean isHashIndexed(String hash) throws SQLException {
        String query;
        int values;

        query = "SELECT count(doc_hash) FROM doc_index where doc_hash = ?";
        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {

            pstmt.setString(1, hash);
            try (ResultSet res = pstmt.executeQuery()) {

                res.next();
                values = res.getInt(1);
            }
        }

        if (values > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFileIndexed(String hash, String file) throws SQLException {
        String query;
        int count;

        query = "SELECT COUNT(*) FROM documents WHERE doc_hash = ? AND doc_name = ?";

        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.setString(2, file);

            try (ResultSet res = pstmt.executeQuery()) {
                res.next();
                count = res.getInt(1);
            }

        }

        if (count > 0) return true;
        else {
            return false;
        }
    }

    private void indexFile(String hash, String file, String mime) throws SQLException {
        String query;

        query = "INSERT INTO documents (doc_hash, doc_name, doc_mime) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.setString(2, file);
            pstmt.setString(3, mime);
            pstmt.executeUpdate();
        }
    }

    private void indexHash(String hash, String text) throws SQLException {
        String query;

        query = "INSERT INTO doc_index(doc_hash, doc_idx) VALUES(?, to_tsvector(?, ?))";

        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.setString(2, this.cfg.getProperty("language"));
            pstmt.setString(3, text);
            pstmt.executeUpdate();
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
        String query;

        result = new ArrayList<>();
        query = "SELECT doc_hash FROM doc_index";

        try (Statement stmt = this.conn.createStatement()) {
            try (ResultSet res = stmt.executeQuery(query)) {
                while (res.next()) {
                    result.add(res.getString(1));
                }
            }
        }

        return result;
    }

    @Override
    public void removeHash(String hash) throws SQLException {
        String query;

        query = "DELETE FROM documents WHERE doc_hash = ?";
        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        }

        query = "DELETE FROM doc_index WHERE doc_hash = ?";
        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        }

        this.conn.commit();
    }

    @Override
    public ArrayList<String> getStoredFiles(String hash) throws SQLException {
        ArrayList<String> result;
        String query;

        result = new ArrayList<>();
        query = "SELECT doc_name FROM documents where doc_hash = ?";

        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    result.add(res.getString(1));
                }
            }
        }

        return result;
    }

    @Override
    public ArrayList<String> queryHash(String data) throws SQLException {
        ArrayList<String> result;
        String query;

        result = new ArrayList<>();
        query = "SELECT doc_hash FROM doc_index WHERE doc_idx @@ to_tsquery(?, ?)";

        try (PreparedStatement pstmt = this.conn.prepareStatement(query)) {
            pstmt.setString(1, this.cfg.getProperty("language"));
            pstmt.setString(2, data);
            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    result.add(res.getString(1));
                }
            }
        }

        return result;
    }
}
