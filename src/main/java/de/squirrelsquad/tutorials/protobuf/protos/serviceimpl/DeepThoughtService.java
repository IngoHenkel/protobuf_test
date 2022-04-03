package de.squirrelsquad.tutorials.protobuf.protos.serviceimpl;
import com.google.protobuf.Empty;

import de.squirrelsquad.tutorials.protobuf.protos.CalculateIntegerTokens;
import de.squirrelsquad.tutorials.protobuf.protos.IntegerToken;
import de.squirrelsquad.tutorials.protobuf.protos.WhoAmI;
import de.squirrelsquad.tutorials.protobuf.protos.service.DeepThoughtServicesGrpc;
import io.grpc.stub.StreamObserver;

public class DeepThoughtService extends DeepThoughtServicesGrpc.DeepThoughtServicesImplBase{

    @Override
    public void whoAreYou(Empty request, StreamObserver<WhoAmI> responseObserver) {
        // TODO Auto-generated method stub
        super.whoAreYou(request, responseObserver);
    }

    @Override
    public void requestIntegerTokens(CalculateIntegerTokens request, StreamObserver<IntegerToken> responseObserver) {
        // TODO Auto-generated method stub
        super.requestIntegerTokens(request, responseObserver);
    }
    
}
