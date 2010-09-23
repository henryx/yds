/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.engines;

import it.application.yds.fetch.streams.InterfaceStream;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author enrico
 */
public class LuceneEngine implements Engine {

    public void store(InterfaceStream stream) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<String> getHashStored() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeHash(String hash) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<String> getFileStored(String hash) throws SQLException{
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
