package net.grigoriadi.sc.processing.parsing;

import java.io.InputStream;

/**
 * Reads a data from a an stream.
 */
public interface IStreamParser {

    String ITEM_NAME = "Item";
    String AMOUNT_NAME = "amount";
    String TIME_NAME = "time";

    void readStream(InputStream stream);
}
