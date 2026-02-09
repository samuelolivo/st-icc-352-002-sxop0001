package org.example;

import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    static void main() {
       //var app = Javalin.create().start(7070);

        Scanner scanner = new Scanner(in);
        String input;
        URI url;

        do {
            out.print("Ingrese URL válida: ");
            input = scanner.nextLine();
            url = urlValida(input);
        } while (url == null);
    }

    static URI urlValida(String input){
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

            return url;
        } catch (Exception e) {
            out.println("Error: No se pudo accesar a la URL proporcionada.");
            return null;
        }
    }
}
