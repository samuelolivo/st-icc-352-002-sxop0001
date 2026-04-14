package org.example.grpc;

import io.grpc.stub.StreamObserver;

import org.example.grpc.EncuestaServiceGrpc.EncuestaServiceImplBase;

public class EncuestaServiceImpl extends EncuestaServiceImplBase {

    @Override
    public void sincronizar(EncuestaRequest request, StreamObserver<EncuestaResponse> responseObserver) {


        String nombre = request.getNombre();
        String sector = request.getSector();


        EncuestaResponse response = EncuestaResponse.newBuilder()
                .setMensaje("ENCUESTA DE " + nombre + " RECIBIDA")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}