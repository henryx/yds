/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import java.io.File;

public interface InterfaceStream {
    public void setFile(File file);
    public void setMime(String mime);

    public String getFileName();
    public String getMime();
    public String getHash();
    public String getStream();
}