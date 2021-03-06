/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import it.application.yds.Main;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TextStream extends Stream {

    public TextStream() {
        super();
    }

    private String streamFromText() throws IOException {
        ArrayList<String> text;
        BufferedReader buffer;
        String line, result;

        text = new ArrayList<>();
        try {
            buffer = new BufferedReader(new FileReader(this.file));
            while ((line = buffer.readLine()) != null) {
                text.add(line);
            }

            result = text.toString();

        } catch (FileNotFoundException ex) {
            Main.logger.error(null, ex);
            result = "";
        }
        return result;
    }

    private String streamFromHTML() {
        // TODO: write method to extract text from HTML files
        return "";
    }

    @Override
    public String getStream() throws IOException {
        // TODO: write methods to extract text from formatted text files (e.g. HTML)

        if (this.getMime().equals("text/html")) {
            return streamFromHTML();
        } else {
            return streamFromText();
        }
    }
}
