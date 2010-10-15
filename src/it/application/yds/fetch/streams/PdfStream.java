/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import it.application.yds.Main;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


/**
 *
 * @author enrico
 */
public class PdfStream extends Stream {

    public PdfStream() {
        super();
    }

    @Override
    public String getStream() throws IOException {
        PDDocument document;
        PDFTextStripper stripper;
        String result;

        try {
            stripper = new PDFTextStripper();
            document = PDDocument.load(this.file);

            result = stripper.getText(document);
            document.close();

        } catch (NullPointerException ex) {
            Main.logger.error(null, ex);
            result = "";
        }
        return result;
    }
}
