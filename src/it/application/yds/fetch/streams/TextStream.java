package it.application.yds.fetch.streams;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextStream extends AbstractStream implements InterfaceStream {

    public TextStream() {
        super();
    }

    private String streamFromText() {
        ArrayList<String> text;
        BufferedReader buffer;
        String line, result;

        text = new ArrayList<String>();
        try {
            buffer = new BufferedReader(new FileReader(this.file));
            while ((line = buffer.readLine()) != null) {
                text.add(line);
            }

            result = text.toString();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextStream.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        } catch (IOException ex) {
            Logger.getLogger(TextStream.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
        }
        return result;
    }

    public String getStream() {
        if (this.getMime().contains("plain")) {
            return streamFromText();
        } else {
            return "";
        }
    }
}
