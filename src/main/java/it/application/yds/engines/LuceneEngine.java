/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.engines;

import it.application.yds.fetch.streams.Stream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author enrico
 */
public class LuceneEngine implements Engine {
    private String location;
    
    public LuceneEngine(Properties cfg) {
        this.location = cfg.getProperty("lucene_location");
        
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
        Directory index = new RAMDirectory();
    }

    @Override
    public void store(Stream stream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getStoredHash() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeHash(String hash) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<String> getStoredFiles(String hash) throws SQLException{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<String> queryHash(String data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
