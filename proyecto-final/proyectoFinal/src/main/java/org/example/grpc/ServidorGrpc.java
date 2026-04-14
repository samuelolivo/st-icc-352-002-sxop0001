package org.example.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class ServidorGrpc {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new EncuestaServiceImpl())
                .build();

        System.out.println("Servidor gRPC iniciado en el puerto 50051...");
        server.start();
        server.awaitTermination();
    }
}