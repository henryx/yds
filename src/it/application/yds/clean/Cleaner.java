/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.clean;

import it.application.yds.Main;
import it.application.yds.fetch.engines.Engine;
import it.application.yds.fetch.engines.PgEngine;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author enrico
 */
public class Cleaner implements Runnable {
    private Engine engine;
    private Properties cfg;

    public Cleaner(Properties cfg) throws IllegalArgumentException, SQLException {
        super();

        this.cfg = cfg;

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            this.engine = new PgEngine(this.cfg);
        } else {
            throw new IllegalArgumentException("Engine not supported");
        }
    }

    public void run() {
        ArrayList<String> files, hashStored;
        File openFile;

        try {
            hashStored = this.engine.getHashStored();

        } catch (SQLException ex) {
            Main.logger.error("Problems about retrieve hash to check", ex);
            hashStored = new ArrayList<String>();
        }

        for (String hash : hashStored) {

            try {
                files = this.engine.getFileStored(hash);

            } catch (SQLException ex) {
                Main.logger.error("Problems about retrieve hash to check", ex);
                files = new ArrayList<String>();
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
