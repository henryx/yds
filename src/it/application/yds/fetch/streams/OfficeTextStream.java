/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfDocument;

/**
 *
 * @author enrico
 */
public class OfficeTextStream extends AbstractStream implements InterfaceStream {
    public OfficeTextStream() {
        super();
    }

    // TODO: implement OpenOffice Writer extractor
    private String streamFromOOWriterText() throws IOException {
        OdfDocument doc;
        OdfFileDom dom;
        String result;

        try {
            doc = OdfDocument.loadDocument(this.file);
            result = doc.getOfficeBody().getTextContent();

        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }

        return result;
    }

    private String streamFromOfficeWordText() throws IOException {
        String result;
        WordExtractor extract;

        extract = new WordExtractor(new FileInputStream(this.file));
        result = extract.getText();

        return result;
    }

    public String getStream() throws IOException {
        if (this.getMime().equals("application/vnd.oasis.opendocument.text")) {
            return streamFromOOWriterText();
        } else {
             return streamFromOfficeWordText();
        }
    }
}
