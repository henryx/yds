/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.fetch;

import it.application.yds.fetch.engines.Engine;
import it.application.yds.fetch.engines.PgEngine;
import it.application.yds.fetch.streams.PdfStream;
import it.application.yds.fetch.streams.Stream;
import it.application.yds.fetch.streams.TextStream;
import it.application.yds.util.FileListing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;

/**
 *
 * @author enrico
 */
public class Fetcher {
    private Engine engine;
    private MimetypesFileTypeMap mimeParser;
    private Properties cfg;

    public Fetcher(Properties cfg) throws IllegalArgumentException {
        HashMap<String, String> parameters;

        this.cfg = cfg;
        try {
            this.mimeParser = new MimetypesFileTypeMap("/etc/mime.types");
        } catch (IOException ex) {
            this.mimeParser = new MimetypesFileTypeMap();
        }

        parameters = new HashMap<String, String>();

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            parameters.put("host", this.cfg.getProperty("pg_host"));
            parameters.put("port", this.cfg.getProperty("pg_port"));
            parameters.put("user", this.cfg.getProperty("pg_user"));
            parameters.put("password", this.cfg.getProperty("pg_password"));
            parameters.put("dbname", this.cfg.getProperty("pg_database"));

            this.engine = new PgEngine(parameters);
        } else {
            throw new IllegalArgumentException("Engine not supported");
        }
    }

    public void start() {
        try {
            File path;
            Stream stream;
            String mime;

            path = new File(this.cfg.getProperty("path"));
            List<File> files = FileListing.getFileListing(path);

            for (File file : files) {
                if (!file.isDirectory()) {
                    mime = this.mimeParser.getContentType(file);

                    if (mime.startsWith("text")) {
                        stream = new TextStream();
                    } else if (mime.endsWith("pdf")||file.getName().endsWith("pdf")) {
                        stream = new PdfStream();
                    } else {
                        stream = null;
                    }

                    if (stream != null) {
                        stream.setFile(file);
                    }

                    System.out.print(file);
                    System.out.println(": " + mime);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
