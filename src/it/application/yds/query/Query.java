/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.query;

import it.application.yds.engines.Engine;
import it.application.yds.engines.PgEngine;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author ebianchi
 */
public class Query {
    private Engine engine;
    private Properties cfg;

    public Query(Properties cfg) throws IllegalArgumentException, SQLException {
        super();

        this.cfg = cfg;

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            this.engine = new PgEngine(this.cfg);
        } else {
            throw new IllegalArgumentException("Engine not supported");
        }
    }

    public void search(String data) throws SQLException {
        ArrayList<String> files, hash;

        hash = this.engine.queryHash(data);

        for (String item: hash) {
            files = this.engine.getStoredFiles(item);
            for (String filename: files) {
                System.out.println(filename);
            }
        }

    }
    
    public void search() {
        // TODO: create a console system for query data
    }

}
