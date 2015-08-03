package net.grigoriadi.sc.processing.parsing;

import java.io.InputStream;

/**
 * Reads domain specific data from a xml stream.
 */
public interface IStreamParser {

    //xml stream tag items
    String ITEM_NAME = "Item";
    String AMOUNT_NAME = "amount";
    String TIME_NAME = "time";

    void readStream(InputStream stream);
}
