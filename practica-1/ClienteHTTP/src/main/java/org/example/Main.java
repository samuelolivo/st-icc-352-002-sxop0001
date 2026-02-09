package org.example;

import io.javalin.Javalin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    static void main() {
        var app = Javalin.create().start();
        var port = app.port();

        Scanner scanner = new Scanner(in);
        String url;

        do {
            out.print("Ingrese URL válida: ");
            url = scanner.nextLine();
        } while (!urlValida(url));
    }

    static boolean urlValida(String input){
        try {
            URL url = new URL(input);
            url.openConnection().connect();
            String protocol = url.getProtocol();

            if (protocol.equals("http") || protocol.equals("https")) {
                out.println("Error: El protocolo de la URL debe ser http o https.");
                return false;
            }

            return true;
        } catch (MalformedURLException e) {
            out.println("Error: El formato de la URL es incorrecto.");
        } catch (IOException e) {
            out.println("Error: No se pudo establecer una conexión con la URL.");
        }

        out.println();
        return false;
    }
}
