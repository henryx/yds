/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */
package it.application.yds.operations;

import it.application.yds.Main;
import it.application.yds.fetch.streams.OfficeTextStream;
import it.application.yds.fetch.streams.PdfStream;
import it.application.yds.fetch.streams.Stream;
import it.application.yds.fetch.streams.TextStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Stack;
import javax.activation.MimetypesFileTypeMap;

/**
 *
 * @author enrico
 */
public class Fetcher extends Operation {

    private MimetypesFileTypeMap mimeParser;

    public Fetcher(Properties cfg) throws IllegalArgumentException, SQLException {
        super(cfg);

        try {
            // NOTE: /etc/mime.types is used on UNIX systems.
            //       In Windows systems this file doesn't exist
            this.mimeParser = new MimetypesFileTypeMap("/etc/mime.types");
        } catch (IOException ex) {
            this.mimeParser = new MimetypesFileTypeMap();
        }

    }

    private void compute(File aFile) throws FileNotFoundException {
        Stream stream;
        String mime;

        mime = this.mimeParser.getContentType(aFile);
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
            stream.setFile(aFile);
            stream.setMime(mime);
            this.engine.store(stream);
        }
    }

    public void start() {
        try {
            File child;
            Stack<File> stack;

            stack = new Stack<>();
            stack.push(new File(this.cfg.getProperty("path")));

            while (!stack.isEmpty()) {
                child = stack.pop();

                if (child.isDirectory()) {
                    for (File f : child.listFiles()) {
                        stack.push(f);
                    }
                } else {
                    this.compute(child);
                }
            }
        } catch (FileNotFoundException ex) {
            Main.logger.error(null, ex);
        }
    }
}
