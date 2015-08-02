package net.grigoriadi.sc.processing;

import java.io.OutputStream;

/**
 * Stream generation interface.
 */
public interface IStreamGenerator {

    String NS = "http://grigoriadi.net/stream-combiner";

    void writeStream(OutputStream out);
}
