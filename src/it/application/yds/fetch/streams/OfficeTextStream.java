/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import java.io.IOException;

/**
 *
 * @author enrico
 */
public class OfficeTextStream extends AbstractStream implements InterfaceStream {
    public OfficeTextStream() {
        super();
    }

    // TODO: implement OpenOffice Writer extractor
    private String streamFromOOWriterText() {
        return "";
    }

    // TODO: implement Office Word (pre 2007) extractor
    private String streamFromOfficeWordText() {
        return "";
    }

    // TODO: implement Office Word (2007+) extractor
    private String streamFromOOXMLText() {
        return "";
    }

    public String getStream() throws IOException {
        if (this.getMime().equals("application/vnd.oasis.opendocument.text")) {
            return streamFromOOWriterText();
        } else if (this.getMime().equals("application/msword")) {
            return streamFromOfficeWordText();
        } else {
            return streamFromOOXMLText();
        }
    }
}
