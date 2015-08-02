package net.grigoriadi.sc.processing.parsing;

import java.io.*;

/**
 * Created by rgrigoriadi on 8/1/15.
 */
public class ConsolePrinter implements IStreamParser {
    @Override
    public void readStream(InputStream stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        PrintWriter writer = new PrintWriter(System.out);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
