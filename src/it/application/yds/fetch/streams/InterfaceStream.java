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