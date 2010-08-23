/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


/**
 *
 * @author enrico
 */
public class PdfStream extends AbstractStream implements InterfaceStream {

    public PdfStream() {
        super();
    }

    public String getStream() throws IOException {
        PDDocument document;
        PDFTextStripper stripper;
        String result;

        stripper = new PDFTextStripper();
        document = PDDocument.load(file);

        result = stripper.getText(document);
        document.close();

        return result;
    }
}
