package it.application.yds.fetch.streams;

import java.io.File;

public interface InterfaceStream {
    public void setFile(File file);

    public String getFileName();
    public String getHash();
    public String getStream();
}