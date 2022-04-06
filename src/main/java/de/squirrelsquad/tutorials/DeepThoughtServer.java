package de.squirrelsquad.tutorials;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.squirrelsquad.tutorials.protobuf.protos.service.DeepThoughtServices;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DeepThoughtServer {
  private final Server server;
  private int port;
  private static final Logger logger = Logger.getLogger(DeepThoughtServer.class.getName());

  public DeepThoughtServer(int port) {
    this.port = port;
    server = ServerBuilder.forPort(port).addService(new DeepThoughtServices()).build();

  }

  public void start() throws IOException {
    server.start();
    logger.info("Server started, listening on " + this.port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown
        // hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          DeepThoughtServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });    
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon
   * threads.
   */
  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * 
   * /** Stop serving requests and shutdown resources.
   */
  public void stop() throws InterruptedException {
    if (server != null) {
      logger.info("Shutting down server...");
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
      logger.info("Server successfully shut down.");
    }
  }

}
