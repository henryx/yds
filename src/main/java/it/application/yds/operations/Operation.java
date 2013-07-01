/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */
package it.application.yds.operations;

import it.application.yds.engines.Engine;
import it.application.yds.engines.LuceneEngine;
import it.application.yds.engines.PgEngine;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author enrico
 */
public abstract class Operation {

    protected Engine engine;
    protected Properties cfg;

    public Operation(Properties cfg) throws IllegalArgumentException, SQLException {
        this.cfg = cfg;

        switch (this.cfg.getProperty("engine")) {
            case "postgresql":
                this.engine = new PgEngine(this.cfg);
                break;
            case "lucene":
                this.engine = new LuceneEngine(this.cfg);
                break;
            default:
                throw new IllegalArgumentException("Engine not supported");
        }
    }
}
