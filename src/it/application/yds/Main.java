/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds;

import it.application.yds.fetch.Fetcher;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Main {
    public static final String PROGNAME = "YDS";
    public static final String VERSION = "0.1";
    private Properties cfgFile;
    private Boolean scan;
    private Boolean query;

    public Main() {
    }

    public void printHelp() {
        System.out.println("Usage:");
        System.out.println("    -c<file> or --cfg=<file> Set the configuration file");
        System.out.println("    -s or --scan             Scan data");
        System.out.println("    -q or --query            Query data");
    }

    public void setCfg(String cfgFile) {
        try {
            this.cfgFile = new Properties();
            this.cfgFile.load(new FileInputStream(cfgFile));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public void parseOption(String option) {
        if (option.startsWith("-c") ||
            option.startsWith("--cfg=")) {
            if (option.startsWith("--")) {
                this.setCfg(option.substring(6));
            } else {
                this.setCfg(option.substring(2));
            }
        } else if (option.equals("-s") ||
                   option.equals("--scan")) {
            this.scan = Boolean.TRUE;
        } else if (option.equals("-q") ||
                   option.equals("--query")) {
            this.query = Boolean.TRUE;
        }
    }

    public void execute() {
        Fetcher fetch;

        if (this.scan.booleanValue()) {
            try {
                fetch = new Fetcher(this.cfgFile);
                fetch.start();
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (this.query.booleanValue()) {
            // TODO: implement interface for queries
        } else {
            System.out.println("Scan and Query aren't available at the same instance");
            System.exit(1);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m;

        m = new Main();
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                m.parseOption(args[i]);
            }
            m.execute();
        } else {
            m.printHelp();
            System.exit(0);
        }
    }
}
