/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */
package it.application.yds;

import it.application.yds.operations.Cleaner;
import it.application.yds.operations.Fetcher;
import it.application.yds.operations.Query;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
    public static final String VERSION = "0.3";
    public static final Logger logger = Logger.getLogger("YDS");
    private Properties cfgFile;
    private Options opts;

    public Main() {
        this.opts = new Options();

        this.opts.addOption("h", "help", false, "Print this help");
        this.opts.addOption(Option.builder("c")
                .longOpt("cfg")
                .desc("Set the configuration file")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        this.opts.addOption(Option.builder("s")
                .longOpt("scan")
                .desc("Scan data")
                .build());
        this.opts.addOption(Option.builder("C")
                .longOpt("clean")
                .desc("Remove old indexed data")
                .build());
        this.opts.addOption(Option.builder("q")
                .longOpt("query")
                .hasArg()
                .argName("ARG")
                .desc("Query data")
                .build());
        this.opts.addOption(Option.builder()
                .longOpt("cmd")
                .desc("Use command line for querying data")
                .build());
    }

    /**
     * Print various information about the application
     */
    private void printHelp(Integer code) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("YDS", this.opts);
        System.exit(code);
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
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.FATAL, null, ex);
            System.exit(2);
        }
    }

    /**
     * Open the configuration file
     *
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

    public void go(String[] args) throws ParseException {
        Cleaner aCleaner;
        CommandLine cmd;
        CommandLineParser parser;
        Fetcher fetch;
        Query query;

        parser = new DefaultParser();
        cmd = parser.parse(this.opts, args);

        if (cmd.hasOption("help")) {
            this.printHelp(0);
        }

        this.setCfg(cmd.getOptionValue("cfg"));
        this.setLog();

        if (cmd.hasOption("clean")) {
            try {
                aCleaner = new Cleaner(cfgFile);
                aCleaner.start();
                System.exit(0);
            } catch (IllegalArgumentException | SQLException ex) {
                Main.logger.error(null, ex);
                System.exit(3);
            }
        }

        if (cmd.hasOption("scan")) {
            try {
                fetch = new Fetcher(this.cfgFile);
                fetch.start();
                System.exit(0);
            } catch (IllegalArgumentException | SQLException ex) {
                Main.logger.error(null, ex);
                System.exit(3);
            }
        }

        if (cmd.hasOption("query")) {
            try {
                query = new Query(cfgFile);
                query.search(cmd.getOptionValue("query"));

                System.exit(0);
            } catch (IllegalArgumentException | SQLException ex) {
                Main.logger.error(null, ex);
                System.exit(3);
            }
        }

        if (cmd.hasOption("cmd")) {
            try {
                query = new Query(cfgFile);
                query.cmd();
            } catch (IllegalArgumentException | SQLException ex) {
                Main.logger.error(null, ex);
                System.exit(3);
            }
        }

        if (cmd.getOptions().length <= 0) {
            System.out.println("None operation is selected");
            this.printHelp(1);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Main m;

            m = new Main();
            m.go(args);
        } catch (ParseException ex) {
            Main.logger.error(null, ex);
        }
    }
}
