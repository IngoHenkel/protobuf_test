package de.squirrelsquad.tutorials;

import java.io.IOException;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DeepThoughtMain {
    private static Logger logger = Logger.getLogger(DeepThoughtMain.class.getName());

    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
        }
        try {
            if (args[0].equals("-c")) {
                String serverUri = args[1];
                startClient(serverUri);
                System.exit(0);
            }
            if (args[0].equals("-s")){
                String serverPort = args[1];
                startServer(Integer.parseInt(serverPort));
            
            }
        } catch (Exception e) {
            logger.severe("Error: " + e);
            e.printStackTrace();
            ;
        }
    }

    private static void startServer(int port) throws InterruptedException, IOException {
        DeepThoughtServer server = new DeepThoughtServer(port);
        server.start();
        server.blockUntilShutdown();
    }

    private static void startClient(String serverUri) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverUri).usePlaintext().build();
        try {
            DeepThoughtClient client = new DeepThoughtClient(channel);
            client.requestWhoAmI();
           // client.requestIntegers(0, 20, 1);
            client.requestIntegersAsync(5, 100, 5);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void usage() {
        System.out.println(
                "Usage: java -jar [jar] (-c targetUri: start client connecting to targetUri, e.G.: -c localhost:8980 |  -s serverport: start server on port serverPort, e.G. -s 8980)");
        System.exit(-1);
    }
}
