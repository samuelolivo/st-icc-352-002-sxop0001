package org.example;

import io.javalin.Javalin;

public class Server {
    static void server() {
        var app = Javalin.create(config -> {
            config.showJavalinBanner = false; // Oculta el banner de Javalin
        }).start(7070);

        app.post("/postEncontrado", ctx -> {
            String matricula = ctx.header("matricula-id");
            String asignatura = ctx.formParam("asignatura");

            System.out.println("POST recibido:");
            System.out.println("  Asignatura: " + asignatura);
            System.out.println("  Matrícula: " + matricula);

            ctx.status(200).result("Recibido correctamente");
        });

        System.out.println("Servidor Javalin corriendo en http://localhost:7070/");
    }
}
