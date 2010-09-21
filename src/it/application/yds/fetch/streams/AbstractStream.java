/*
 * Copyright (C) 2010 Enrico Bianchi (enrico.bianchi@ymail.com)
 * Project       YDS
 * Description   The Yggdrasill Document Search - A java based file indexer
 * License       BSD (see LICENSE.BSD for details)
 */

package it.application.yds.fetch.streams;

import it.application.yds.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author enrico
 */
public abstract class AbstractStream {

    private String mime;
    protected File file;

    public void setFile(File file) {
        this.file = file;
    }

    private byte[] createChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException  {
        InputStream fis = new FileInputStream(this.file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public String getHash() throws IOException {
        BigInteger bigInt;
        String result;
        byte[] check;

        try {
            check = this.createChecksum();
            bigInt = new BigInteger(1, check);

            result = bigInt.toString(16).toUpperCase();
        } catch (FileNotFoundException ex) {
            Main.logger.error(null, ex);
            result = "";
        } catch (NoSuchAlgorithmException ex) {
            Main.logger.error(null, ex);
            result = "";
        }

        return result;
    }

    public String getFileName() {
        return this.file.getAbsolutePath();
    }

    public void setMime(String mime) {
        this.mime = mime;
    }
    
    public String getMime() {
        return this.mime;
    }
}
