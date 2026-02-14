package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
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
    }
}
