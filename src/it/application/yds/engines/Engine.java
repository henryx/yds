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

/**
 *
 * @author enrico
 */
public interface Engine {

    public void store(Stream stream);
    public void removeHash(String hash) throws SQLException;
    public ArrayList<String> getStoredHash() throws SQLException;
    public ArrayList<String> getStoredFiles(String hash) throws SQLException;
    public ArrayList<String> queryHash(String data) throws SQLException;

}
