package org.example;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.Map;

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
        }).start();

        app.before(ctx -> {
            Boolean logged = ctx.sessionAttribute("logged");
            if (ctx.path().equals("/login") || ctx.path().equals("/login.html") || ctx.path().endsWith(".css")) {
                return;
            }

            if (logged == null || !logged) {
                ctx.redirect("/login.html");
            }
        });

        app.get("/login", ctx -> {
            ctx.render("login.html");
        });

        app.post("/login", ctx -> {
            String user = ctx.formParam("user");
            String pass = ctx.formParam("password");

            if ("admin".equals(user) && "12345678".equals(pass)) {
                ctx.sessionAttribute("logged", true);
                ctx.sessionAttribute("user", user);
                ctx.redirect("/inicio.html");
            } else {
                ctx.redirect("/login.html?error=true");
            }
        });

        app.get("/", ctx -> {
            ctx.redirect("/inicio.html");
        });

        app.post("/inicio", ctx -> {
            ctx.sessionAttribute("logged", false);
            ctx.redirect("/login.html");
        });
    }
}
