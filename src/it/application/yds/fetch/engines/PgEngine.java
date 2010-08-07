/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.application.yds.fetch.engines;

import it.application.yds.fetch.streams.InterfaceStream;
import java.util.HashMap;

/**
 *
 * @author enrico
 */
public class PgEngine implements Engine {
    public PgEngine(HashMap<String, String> parameters) {

    }

    public void store(InterfaceStream stream) {
        System.out.println(stream.getFileName() + ": " + stream.getHash());
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
