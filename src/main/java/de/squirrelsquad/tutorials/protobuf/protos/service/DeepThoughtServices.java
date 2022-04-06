package de.squirrelsquad.tutorials.protobuf.protos.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Durations;

import de.squirrelsquad.tutorials.protobuf.protos.CalculateIntegerTokens;
import de.squirrelsquad.tutorials.protobuf.protos.IntegerToken;
import de.squirrelsquad.tutorials.protobuf.protos.WhoAmI;
import io.grpc.stub.StreamObserver;

public class DeepThoughtServices extends DeepThoughtServicesGrpc.DeepThoughtServicesImplBase {

    public static final Instant startTime = Instant.now();
    public static final UUID serverId = java.util.UUID.randomUUID();

    private static AtomicInteger requestCounter = new AtomicInteger();

    private ExecutorService integerCalcThreads = Executors.newCachedThreadPool();

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void whoAreYou(Empty request, StreamObserver<WhoAmI> responseObserver) {

        logger.info("Who am I called");
        responseObserver.onNext(whoAmI());
        responseObserver.onCompleted();
        logger.info("Who am I request handled");
    }

    private static WhoAmI whoAmI() {
        Duration runTime = Durations.fromMillis(ChronoUnit.MILLIS.between(startTime, Instant.now()));
        Timestamp startTimeStamp = Timestamp.newBuilder().setSeconds(startTime.getEpochSecond())
                .setNanos(startTime.getNano()).build();
        int requestCounterInt = requestCounter.getAndAdd(1);
        return WhoAmI.newBuilder()
                .setLifeTime(runTime).setStartTime(startTimeStamp).setServerId(serverId.toString())
                .setPodRequestCounter(requestCounterInt).build();
    }

    @Override
    public void requestIntegerTokens(CalculateIntegerTokens request, StreamObserver<IntegerToken> responseObserver) {
        // TODO Auto-generated method stub
        logger.info("Request integer tokens called");
        CalcIntegerJob newJob = new CalcIntegerJob(request, responseObserver);
       // newJob.run();
        integerCalcThreads.submit(newJob);  
        logger.info("Request integer tokens method left");

    }

    private static class CalcIntegerJob implements Runnable {
        private Logger logger = Logger.getLogger(this.getClass().getName());

        private CalculateIntegerTokens request;

        private StreamObserver<IntegerToken> observer;

        public CalcIntegerJob(CalculateIntegerTokens request, StreamObserver<IntegerToken> responseObserver) {
            this.request = request;
            this.observer = responseObserver;
        }

        @Override
        public void run() {
            logger.info("Starting integer calculation :" + request);
            try {
                for (int val = request.getStartVal(); val < request.getEndVal(); val += request.getDelta()) {
                    logger.info("Thinking deep...");
                    try {
                        synchronized (this) {
                            this.wait(3000);
                        }
                        logger.info("Came up with a solution: " + val);
                        IntegerToken token = IntegerToken.newBuilder().setValue(val)
                                .setThreadName(Thread.currentThread().getName())
                                .setServerInfo(DeepThoughtServices.whoAmI())
                                .build();
                        observer.onNext(token);
                        logger.info("response sent");

                    } catch (InterruptedException e) {
                        logger.severe("That was a bad idea: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                logger.info("Finished my work. going back to sleep");
            } catch (Exception e) {
                logger.severe("Exception in handler thread: " + e.getMessage());
                e.printStackTrace();
            }
            observer.onCompleted();

        }

    }
}
