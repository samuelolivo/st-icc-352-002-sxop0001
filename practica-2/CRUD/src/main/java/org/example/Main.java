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
    static enum KeySession{
        USUARIO
    }

    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/templates";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress=false;
                staticFiles.aliasCheck=null;
            });
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7070);

        app.before("/**", ctx -> {
            System.out.println(ctx.path());

            if(ctx.path().startsWith("/login.html") ||
                    ctx.path().startsWith("/procesarLogin")
                    ){
                return;
            }
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            if(usuario == null){
                ctx.redirect("/login.html");
            }
        });

        app.post("/procesarLogin", ctx -> {
            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");

            if ("admin".equals(usuario) && "admin".equals(password)) {
                Usuario user = new Usuario();
                user.setUsuario(usuario);
                user.setRol(RolesUsuario.ADMIN);
                ctx.sessionAttribute(KeySession.USUARIO.name(), user);
                ctx.redirect("/admin/CRUD");
            } else {
                ctx.status(401).result("Usuario incorrecto, ingrese un usuario valido.");
            }
        });
    }


}
