package org.example;

import io.javalin.Javalin;

public class Main {
    static void main() {
        var app = Javalin.create().start();
        var port = app.port();
    }
}
