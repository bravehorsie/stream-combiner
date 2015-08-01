package net.grigoriadi.sc;

import net.grigoriadi.sc.processing.QueueWorker;
import net.grigoriadi.sc.transport.StreamClientTask;
import net.grigoriadi.sc.transport.StreamServerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.concurrent.*;

/**
 * Application developed as homework for Oracle on combining sorted streams.
 */
public class StreamCombinerApp {

    private static Logger LOG = LoggerFactory.getLogger(StreamCombinerApp.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private Future<?> serverTaskFuture;

    public static void main(String[] args) {
        StreamCombinerApp app = new StreamCombinerApp();
        AppConetxt.getInstance().setClientCount(args.length);
//        app.waitForKeyPress(() -> LOG.debug("Starting server and clients..."));
        app.runServer();
        app.runClients();
        app.runQueueWorker();
       /* app.waitForKeyPress(() -> {
            try {
                app.serverTaskFuture.get(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            } finally {
                app.serverTaskFuture.cancel(true);
                LOG.debug("Server task cancelled.");

            }
        });*/
    }

    private void runQueueWorker() {
        executorService.execute(new QueueWorker());
    }

    private void runClients() {
        for (int i = 0; i < AppConetxt.getInstance().getClientCount(); i++) {
            LOG.debug(MessageFormat.format("Running client on [{0}:{1}], id:[{2}]", "localhost", 9123, i));
            executorService.execute(new StreamClientTask("localhost", 9123, i));
        }
    }

    private void runServer() {
        executorService.execute(new StreamServerTask());
    }


    private void waitForKeyPress(Runnable afterKeyPress) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter something key to continue:");
        try {
            br.readLine();
            afterKeyPress.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
