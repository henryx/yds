/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds;

import it.application.yds.clean.Cleaner;
import it.application.yds.fetch.Fetcher;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author enrico
 */
public class Main {
    public static final String PROGNAME = "YDS";
    public static final String VERSION = "0.2";
    public static final Logger logger = Logger.getLogger("YDS");
    private Properties cfgFile;
    private Boolean scan;
    private Boolean query;

    public Main() {
        this.scan = Boolean.FALSE;
        this.query = Boolean.FALSE;
    }

    /**
     * Create log via log4j
     */
    private void setLog() {
        Appender appender;
        
        try {
            appender = new FileAppender(new PatternLayout("%d %-5p %c - %m%n"), this.cfgFile.getProperty("log_file"));
            Main.logger.addAppender(appender);
            Main.logger.setLevel(Level.toLevel(this.cfgFile.getProperty("log_level")));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, null, ex);
            System.exit(2);
        } catch (SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, null, ex);
            System.exit(2);
        }
    }

    /**
     * Open the configuration file
     * @param cfgFile a configuration file
     */
    private void setCfg(String cfgFile) {
        try {
            this.cfgFile = new Properties();
            this.cfgFile.load(new FileInputStream(cfgFile));
        } catch (IOException ex) {
            Main.logger.error(null, ex);
            System.exit(1);
        }
    }

    /**
     * Print various information about che application
     */
    public void printHelp() {
        System.out.println(Main.PROGNAME + " " + Main.VERSION);
        System.out.println("Usage:");
        System.out.println("    -c<file> or --cfg=<file> Set the configuration file");
        System.out.println("    -s or --scan             Scan data");
        System.out.println("    -q or --query            Query data");
        System.out.println("    -h or --help             Print this help");
    }

    /**
     * Parser for parameters passed via command line
     * @param option a parameter
     */
    public void parseOption(String option) throws IllegalArgumentException {
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
        } else if (option.equals("-h") ||
                   option.equals("--help")) {
            this.printHelp();
            System.exit(0);
        } else {
            System.out.println("Invalid parameter");
            this.printHelp();
            System.exit(3);
        }
    }

    public void execute() {
        Fetcher fetch;
        Thread t;

        if (this.scan.booleanValue() && this.query.booleanValue()) {
            System.out.println("Scan and Query aren't available at the same instance");
            System.exit(1);
        } else if (!this.scan.booleanValue() && !this.scan.booleanValue()) {
            System.out.println("No operation selected");
            System.exit(1);
        }

        this.setLog();

        try {
            t = new Thread(new Cleaner(this.cfgFile));
            t.setName("Cleaner");
            t.start();
        } catch (IllegalArgumentException ex) {
            Main.logger.error(null, ex);
        } catch (SQLException ex) {
            Main.logger.error(null, ex);
        }

        if (this.scan.booleanValue()) {
            try {
                fetch = new Fetcher(this.cfgFile);
                fetch.start();
            } catch (IllegalArgumentException ex) {
                Main.logger.error(null, ex);
            } catch (SQLException ex) {
                Main.logger.error(null, ex);
            }
        }

        if (this.query.booleanValue()) {
            // TODO: implement interface for queries
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
