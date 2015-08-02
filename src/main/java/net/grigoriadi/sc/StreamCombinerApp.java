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

    private Thread serverThread = new StreamServerTask();

    public static void main(String[] args) {
        StreamCombinerApp app = new StreamCombinerApp();
        LOG.info("Press enter to continue..");
        app.waitForKeyPress(() -> LOG.debug("Starting server and clients..."));
        app.runServer();
        //in the description of the app it is actually mentioned to run clients based on N hosts:ports
        //since dummy server generating data is listen on one single localhost port it doesn't make sense
        //can be effortlessly changed to
        app.runClients(args.length);
        app.runQueueWorker();
        app.waitForKeyPress(app::terminate);

        LOG.debug("Exiting main thread");
    }

    private void terminate() {
        serverThread.interrupt();
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runQueueWorker() {
        QueueWorker task = new QueueWorker();
        Future<?> workerFuture = executorService.submit(task);
        AppConetxt.getInstance().getClientRegistry().setAllClientsShutDownListener(()-> workerFuture.cancel(true));
    }

    private void runClients(int count) {
        for (int i = 0; i < count; i++) {
            LOG.debug(MessageFormat.format("Running client on [{0}:{1}], id:[{2}]", "localhost", 9123, i));
            executorService.execute(new StreamClientTask("localhost", 9123, i));
        }
    }

    private void runServer() {
        serverThread.start();
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
