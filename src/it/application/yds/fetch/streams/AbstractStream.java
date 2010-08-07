/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.fetch.streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public abstract class AbstractStream {

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

    public String getHash() {
        BigInteger bigInt;
        String result;
        byte[] check;

        try {
            check = this.createChecksum();
            bigInt = new BigInteger(1, check);

            result = bigInt.toString(16).toUpperCase();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextStream.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TextStream.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        } catch (IOException ex) {
            Logger.getLogger(TextStream.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        }

        return result;
    }

    public String getFileName() {
        return this.file.getAbsolutePath();
    }

}
