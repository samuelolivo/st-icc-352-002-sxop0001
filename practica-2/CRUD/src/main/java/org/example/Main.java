package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress=false;
                staticFiles.aliasCheck=null;
            });
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7070);

        app.before("/admin/*", ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");

            if (usuario == null || usuario.getRol() != RolesUsuario.ADMIN) {
                ctx.status(401).result("Acceso denegado: Se requiere rol de administrador para acceder.");
            }
        });
    }

}
