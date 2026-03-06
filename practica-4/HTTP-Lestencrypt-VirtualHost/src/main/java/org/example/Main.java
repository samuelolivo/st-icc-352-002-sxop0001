package org.example;

import io.javalin.Javalin;

public class  Main {
    public static void main(String[] args) {
        Javalin app1 = Javalin.create().start(7070);
        app1.get("/", ctx -> ctx.html(
                "<h1>Aplicacion #1</h1><p>Gustavo, bienvenido a Emazon!</p>"
        ));

        Javalin app2 = Javalin.create().start(7071);
        app2.get("/", ctx -> ctx.html(
                "<h1>Aplicacion #2</h1><p>Gustavo, bienvenido a Ibay!</p>"
        ));
    }
}
