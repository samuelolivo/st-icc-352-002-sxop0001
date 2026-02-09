package org.example;

import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    static void main() throws IOException, InterruptedException {
       //var app = Javalin.create().start(7070);

        Scanner scanner = new Scanner(in);
        String input;
        HttpRequest request;

        //Para capturar URL
        do {
            out.print("Ingrese URL válida: ");
            input = scanner.nextLine();
            request = requestValido(input);
        } while (request == null);


    }

    static HttpRequest requestValido(String input){
        try {
            URI url = URI.create(input);
            String protocolo = url.getScheme();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.discarding());

            if (!(protocolo.equals("http") || protocolo.equals("https"))) {
                out.println("Error: El protocolo de la URL debe ser 'http' o 'https'.");
                return null;
            }

            return request;
        } catch (Exception e) {
            out.println("Error: No se pudo accesar a la URL proporcionada.");
            return null;
        }
    }
}
