/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch;

import it.application.yds.Main;
import it.application.yds.fetch.engines.Engine;
import it.application.yds.fetch.engines.PgEngine;
import it.application.yds.fetch.streams.PdfStream;
import it.application.yds.fetch.streams.InterfaceStream;
import it.application.yds.fetch.streams.OfficeTextStream;
import it.application.yds.fetch.streams.TextStream;
import it.application.yds.util.FileListing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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

                    if (mime.equals("text/plain") ||
                        mime.equals("text/html")) {
                        stream = new TextStream();
                    } else if (mime.equals("application/pdf") ||
                               mime.equals("application/x-pdf") ||
                               mime.equals("application/x-bzpdf") ||
                               mime.equals("application/x-gzpdf")){
                        stream = new PdfStream();
                    } else if (mime.equals("application/vnd.oasis.opendocument.text") ||
                               mime.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                               mime.equals("application/msword")) {
                        stream = new OfficeTextStream();
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
            Main.logger.error(null, ex);
        }
    }
}
