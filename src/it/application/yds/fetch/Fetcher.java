/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch;

import it.application.yds.fetch.engines.Engine;
import it.application.yds.fetch.engines.PgEngine;
import it.application.yds.fetch.streams.PdfStream;
import it.application.yds.fetch.streams.InterfaceStream;
import it.application.yds.fetch.streams.TextStream;
import it.application.yds.util.FileListing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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

    public Fetcher(Properties cfg) throws IllegalArgumentException, SQLException {
        HashMap<String, String> parameters;

        this.cfg = cfg;
        try {
            this.mimeParser = new MimetypesFileTypeMap("/etc/mime.types");
        } catch (IOException ex) {
            this.mimeParser = new MimetypesFileTypeMap();
        }

        parameters = new HashMap<String, String>();

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            this.engine = new PgEngine(this.cfg);
        } else {
            throw new IllegalArgumentException("Engine not supported");
        }
    }

    public void start() {
        try {
            File path;
            InterfaceStream stream;
            String mime;

            path = new File(this.cfg.getProperty("path"));
            List<File> files = FileListing.getFileListing(path);

            for (File file : files) {
                if (!file.isDirectory()) {
                    mime = this.mimeParser.getContentType(file);

                    if (mime.startsWith("text")||mime.endsWith("html")) {
                        stream = new TextStream();
                    } else if (mime.endsWith("pdf")||file.getName().toLowerCase().endsWith("pdf")) {
                        stream = new PdfStream();
                    } else {
                        stream = null;
                    }

                    if (stream != null) {
                        stream.setFile(file);
                        stream.setMime(mime);
                        this.engine.store(stream);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
