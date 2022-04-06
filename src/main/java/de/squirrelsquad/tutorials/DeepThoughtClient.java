package de.squirrelsquad.tutorials;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import de.squirrelsquad.tutorials.protobuf.protos.CalculateIntegerTokens;
import de.squirrelsquad.tutorials.protobuf.protos.IntegerToken;
import de.squirrelsquad.tutorials.protobuf.protos.WhoAmI;
import de.squirrelsquad.tutorials.protobuf.protos.service.DeepThoughtServicesGrpc;
import de.squirrelsquad.tutorials.protobuf.protos.service.DeepThoughtServicesGrpc.DeepThoughtServicesBlockingStub;
import de.squirrelsquad.tutorials.protobuf.protos.service.DeepThoughtServicesGrpc.DeepThoughtServicesStub;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

public class DeepThoughtClient {

    private final DeepThoughtServicesBlockingStub blockingStub;
    private final DeepThoughtServicesStub asyncStub;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public DeepThoughtClient(Channel channel) {
        this.blockingStub = DeepThoughtServicesGrpc.newBlockingStub(channel);
        this.asyncStub = DeepThoughtServicesGrpc.newStub(channel);
    }

    public void requestWhoAmI() {
        logger.info("Requesting whoAreYou, blocking");
        WhoAmI response = this.blockingStub.whoAreYou(Empty.getDefaultInstance());
        logger.info("Server is: " + response);
    }

    public void requestIntegers(int start, int end, int delta) {
        logger.info("requesting integer tokens, blocking");
        Iterator<IntegerToken> result = this.blockingStub.requestIntegerTokens(
                CalculateIntegerTokens.newBuilder().setStartVal(start).setEndVal(end).setDelta(delta).build());
        logger.info("Request returned");
        while (result.hasNext()) {
            IntegerToken token = result.next();
            logger.info("Token received: " + token);
        }
    }

    public void requestIntegersAsync(int start, int end, int delta) {
        logger.info("requesting integer tokens, async");
        TokenObserver obs = new TokenObserver();
        this.asyncStub.requestIntegerTokens(
                CalculateIntegerTokens.newBuilder().setStartVal(start).setEndVal(end).setDelta(delta).build(), obs);
        logger.info("Request sent. waiting for response");
        try {
            List<IntegerToken> response = obs.get();
            logger.info("Request returned");
            for(IntegerToken token: response){
                logger.info(("Token: " + token));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TokenObserver implements StreamObserver<IntegerToken>, Future<List<IntegerToken>> {

        private Logger logger = Logger.getLogger(this.getClass().getName());

        private Object syncObj = new Object();

        private ArrayList<IntegerToken> results = new ArrayList<>();

        private boolean canceled = false;
        private boolean finished = false;

        @Override
        public void onNext(IntegerToken value) {
            logger.info("Received token: " + value);
            synchronized (syncObj) {
                this.results.add(value);
            }

        }

        @Override
        public void onError(Throwable t) {
            logger.info("Error on token stream: " + t.getMessage());
            logger.info("Token stream finished");
            synchronized (this.syncObj) {
                this.canceled = true;
                this.finished = true;
                this.syncObj.notifyAll();
            }

        }

        @Override
        public void onCompleted() {
            logger.info("Token stream finished");
            synchronized (this.syncObj) {
                this.finished = true;
                this.syncObj.notifyAll();
            }

        }

        @Override
        public boolean cancel(boolean arg0) {
            logger.info("Cancelling");
            synchronized (this.syncObj) {
                this.canceled = true;
                syncObj.notifyAll();

            }
            return true;
        }

        @Override
        public List<IntegerToken> get() throws InterruptedException, ExecutionException {
            return get(0, TimeUnit.MILLISECONDS);
        }

        @Override
        public List<IntegerToken> get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException {
            synchronized (this.syncObj) {
                if (!finished && !canceled) {
                    logger.info("Waiting for tokens to come in...");
                    this.syncObj.wait(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
                }
                if (!finished) {
                    throw new ExecutionException("Tokens were not received in time", new TimeoutException());
                }
                return results;
            }
        }

        @Override
        public boolean isCancelled() {
            synchronized (this.syncObj) {
                return isCancelled();
            }
        }

        @Override
        public boolean isDone() {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
