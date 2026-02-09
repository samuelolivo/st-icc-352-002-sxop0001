package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        cliente();
    }

    static void cliente() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(in);
        String input;
        HttpRequest request;

        //Para capturar URL
        do {
            out.print("Ingrese URL válida: ");
            input = scanner.nextLine();
            request = requestValido(input);
        } while (request == null);

        //Para determinar el tipo de recurso
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        String tipoRecurso = response.headers()
                .firstValue("Content-Type")
                .orElse("desconocido");

        out.println("Tipo de recurso seleccionado: " + tipoRecurso);

        //Informacion adicional en caso de ser html
        if (tipoRecurso.contains("html")){
            htmlInfo(response);
        }

        char tecla;
        do {
            out.println("\n\n\n¿Quieres probar con otra URL? [S]i  [N]o: ");
            tecla = scanner.next().charAt(0);
            if (tecla == 's' || tecla == 'S')
                cliente();
        } while(!(tecla == 's'|| tecla == 'n' || tecla == 'S'|| tecla == 'N'));
        System.exit(0);
    }

    static HttpRequest requestValido(String input){
        try {
            URI url = URI.create(input);
            String protocolo = url.getScheme();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
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

    static void htmlInfo(HttpResponse<String> response) throws IOException, InterruptedException {
        String html = response.body();
        URI uri = response.uri();
        Document document = Jsoup.parse(html, uri.toString());

        out.println("Cantidad de...");

        var lineas = html.lines().count();
        out.println("Lineas: " + lineas);

        var parrafos = document.select("p").size();
        out.println("Párrafos: " + parrafos);

        var imgEnParr = document.select("p img").size();
        out.println("Imágenes dentro de párrafos: " + imgEnParr);

        var forms = document.select("form");
        var cantForms = forms.size();
        var formPOST = document.select("form[method=post]").size();
        var formGET = cantForms - formPOST;
        out.println("Formularios con método GET: " + formGET);
        out.println("Formularios con método POST: " + formPOST);

        if (cantForms == 0){
            out.println("No hay formularios para mostrar.");
            return;
        }

        Elements inputs;
        int cont = 0;
        for (Element f : forms) {
            cont++;

            System.out.println("\n\n\nForm " + cont + ": ");
            f.attributes().forEach(attr -> System.out.print(attr.getKey() + "=" + attr.getValue() + "     "));

            inputs = f.select("input");
            System.out.println("\nInputs: ");
            for (Element i : inputs){
                i.attributes().forEach(attr -> System.out.println(attr.getKey() + "=" + attr.getValue()));
            }

            if (f.attr("method").equalsIgnoreCase("post")){

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(f.absUrl("action")))
                        .header("matricula-id", "10154465")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("asignatura=practica1"))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> responsePOST = client.send(request, HttpResponse.BodyHandlers.ofString());

                out.println("\nRespuesta: ");
                out.println(responsePOST.toString());
            }
        }
    }
}
