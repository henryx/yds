/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch;

import it.application.yds.Main;
import it.application.yds.engines.Engine;
import it.application.yds.engines.PgEngine;
import it.application.yds.fetch.streams.OfficeTextStream;
import it.application.yds.fetch.streams.PdfStream;
import it.application.yds.fetch.streams.Stream;
import it.application.yds.fetch.streams.TextStream;
import it.application.yds.util.FileListing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
        this.cfg = cfg;
        try {
            // NOTE: /etc/mime.types is used on UNIX systems.
            //       In Windows systems this file doesn't exist
            this.mimeParser = new MimetypesFileTypeMap("/etc/mime.types");
        } catch (IOException ex) {
            this.mimeParser = new MimetypesFileTypeMap();
        }

        if (this.cfg.getProperty("engine").equals("postgresql")) {
            this.engine = new PgEngine(this.cfg);
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
                    switch (mime) {
                        case "text/plain":
                        case "text/html":
                            stream = new TextStream();
                            break;
                        case "application/pdf":
                        case "application/x-pdf":
                        case "application/x-bzpdf":
                        case "application/x-gzpdf":
                            stream = new PdfStream();
                            break;
                        case "application/vnd.oasis.opendocument.text":
                        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                        case "application/msword":
                            stream = new OfficeTextStream();
                            break;
                        default:
                            stream = null;
                            break;
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
