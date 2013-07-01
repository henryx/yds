/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */
package it.application.yds.operations;

import it.application.yds.Main;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author enrico
 */
public class Cleaner extends Operation {

    public Cleaner(Properties cfg) throws IllegalArgumentException, SQLException {
        super(cfg);
    }

    public void start() {
        ArrayList<String> files, hashStored;
        File openFile;

        try {
            hashStored = this.engine.getStoredHash();

        } catch (SQLException ex) {
            Main.logger.error("Problems about retrieve hash to check", ex);
            hashStored = new ArrayList<>();
        }

        for (String hash : hashStored) {

            try {
                files = this.engine.getStoredFiles(hash);

            } catch (SQLException ex) {
                Main.logger.error("Problems about retrieve hash to check", ex);
                files = new ArrayList<>();
            }

            for (String file : files) {
                openFile = new File(file);
                if (!openFile.exists()) {
                    try {
                        this.engine.removeHash(hash);
                    } catch (SQLException ex) {
                        Main.logger.error("Problems about to clean hash", ex);
                    }
                }
            }
        }

    }
}
