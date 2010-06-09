/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.fetch;

import it.application.yds.fetch.engines.Engine;
import it.application.yds.fetch.engines.PgEngine;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author enrico
 */
public class Fetcher {
    private Engine engine;
    private Properties cfg;

    public Fetcher(Properties cfg) throws Exception {
        HashMap<String, String> parameters;

        this.cfg = cfg;
        parameters = new HashMap<String, String>();

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            parameters.put("host", this.cfg.getProperty("pg_host"));
            parameters.put("port", this.cfg.getProperty("pg_port"));
            parameters.put("user", this.cfg.getProperty("pg_user"));
            parameters.put("password", this.cfg.getProperty("pg_password"));
            parameters.put("dbname", this.cfg.getProperty("pg_database"));

            this.engine = new PgEngine(parameters);
        } else {
            throw new Exception("Engine not supported");
        }
    }

    // FIXME: rewrite for support multiple paths
    // FIXME: this method is not recursive
    private Vector<String[]> parseDirectories() {
        Vector<String[]> result;
        File dir, oneDirItem;
        String[] list, content;
        String item, path;
        int i;

        result = new Vector<String[]>();
        path = this.cfg.getProperty("path");
        dir = new File(path);

        if (dir.isDirectory()) {
            list = dir.list();
            for (i = 0; i < list.length; i++) {
                content = new String[2];

                item = list[i];
                oneDirItem = new File(path, item);
                if (!oneDirItem.isDirectory()) {
                    content[0] = path + "/" + item;
                    content[1] = "FILE";
                } else {
                    content[0] =  path + "/" + item;
                    content[1] = "DIRECTORY";
                }
                result.add(content);
            }
        }

        return result;
    }

    public void start() {
        parseDirectories();
    }
}
