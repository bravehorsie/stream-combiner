package net.grigoriadi.sc.processing;

import java.io.OutputStream;

/**
 * Created by rgrigoriadi on 7/30/15.
 */
public interface IStreamGenerator {

    String NS = "http://grigoriadi.net/stream-combiner";

    void writeStream(OutputStream out) throws InterruptedException;
}
