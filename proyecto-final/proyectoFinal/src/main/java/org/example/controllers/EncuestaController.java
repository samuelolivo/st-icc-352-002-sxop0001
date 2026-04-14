package org.example.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Encuesta;
import org.example.services.EncuestaService;
import org.example.utils.JwtUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncuestaController {
    private final EncuestaService encuestaService;

    public EncuestaController(EncuestaService encuestaService) {
        this.encuestaService = encuestaService;
    };

    public void mostrarFormulario(Context ctx) {
        ctx.render("templates/formularioEncuesta.html");
    }


    public void crearEncuesta(Context ctx) {
        Encuesta nueva = ctx.bodyAsClass(Encuesta.class);

        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());
        String idUsuario = null;

        if (jwt != null) {
            DecodedJWT djwt = JWT.decode(jwt);
            idUsuario = djwt.getSubject();
        }

        nueva.setUsuarioId(idUsuario);

        encuestaService.guardar(nueva);
        ctx.status(201).json(nueva);
    }

    public void vistaMapa(Context ctx) {
        ctx.render("templates/mapaEncuestas.html");
    }

    public void vistaListar(Context ctx) {
        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());
        String nombre = null;
        String rol = null;

        if (jwt != null) {
            DecodedJWT djwt = JWT.decode(jwt);
            nombre = djwt.getClaim("nombre").asString();
            rol = djwt.getClaim("rol").asString();
        }

        List<Encuesta> lista = encuestaService.listarTodasActivas();

        Map<String, Object> model = new HashMap<>();
        model.put("encuestas", lista);
        model.put("nombre", nombre);
        model.put("rol", rol);

        ctx.render("templates/encuestas.html", model);
    }

    public void mostrarEditar(Context ctx) {
        String id = ctx.pathParam("id");
        Encuesta encuesta = encuestaService.buscarPorId(id);

        if (encuesta == null) {
            ctx.redirect("/encuesta");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("encuesta", encuesta);
        model.put("editando", true);

        ctx.render("templates/formularioEncuesta.html", model);


    }

    public void eliminar(Context ctx) {
        String id = ctx.pathParam("id");
        encuestaService.desactivar(id);
        ctx.redirect("/encuesta");
    }

    public void listarEncuestasJson(Context ctx) {
        List<Encuesta> lista = encuestaService.listarTodasActivas();
        ctx.json(lista);
    }

    public void sincronizarEncuesta(Context ctx) {
        Encuesta encuestaPendiente = ctx.bodyAsClass(Encuesta.class);

        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());
        String idUsuario = null;

        if (jwt != null) {
            DecodedJWT djwt = JWT.decode(jwt);
            idUsuario = djwt.getSubject();
        }

        encuestaPendiente.setUsuarioId(idUsuario);
        encuestaService.guardar(encuestaPendiente);
        ctx.status(200).result("Sincronización exitosa");
    }


}
