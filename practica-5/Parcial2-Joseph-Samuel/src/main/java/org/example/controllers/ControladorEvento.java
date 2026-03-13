package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Evento;
import org.example.models.Usuario;
import org.example.services.ServicioEvento;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorEvento {

    public static void listar(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        List<Evento> eventos = ServicioEvento.getInstancia().findAll();

        Map<String, Object> model = new HashMap<>();
        model.put("eventos", eventos);
        model.put("usuario", usuarioLogueado);

        ctx.render("templates/eventos.html", model);
    }

    public static void formularioCrear(Context ctx) {
        ctx.render("templates/admin/crearEvento.html");
    }

    public static void guardar(Context ctx) {
        Usuario loginUser = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        try {
            Evento evento = new Evento();
            evento.setTitulo(ctx.formParam("titulo"));
            evento.setDescripcion(ctx.formParam("descripcion"));
            evento.setFechaHora(LocalDateTime.parse(ctx.formParam("fechaHora")));
            evento.setLugar(ctx.formParam("lugar"));
            evento.setCupoMaximo(ctx.formParamAsClass("cupoMaximo", Integer.class).get());
            evento.setOrganizador(loginUser);
            evento.setPublicado(false);

            ServicioEvento.getInstancia().crear(evento);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ctx.redirect("/eventos");
    }



    public static void vistaModificar(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(id);

        if (evento != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("evento", evento);
            ctx.render("templates/admin/editarEvento.html", model);
        } else {
            ctx.redirect("/eventos");
        }
    }

    public static void modificar(Context ctx) {
        Long id = ctx.formParamAsClass("id", Long.class).get();
        Evento e = ServicioEvento.getInstancia().findById(id);

        if (e != null) {
            try {
                e.setTitulo(ctx.formParam("titulo"));
                e.setDescripcion(ctx.formParam("descripcion"));
                e.setLugar(ctx.formParam("lugar"));
                e.setFechaHora(LocalDateTime.parse(ctx.formParam("fechaHora")));
                e.setCupoMaximo(ctx.formParamAsClass("cupoMaximo", Integer.class).get());

                ServicioEvento.getInstancia().actualizar(e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ctx.redirect("/eventos");
    }



    public static void alternarPublicacion(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento e = ServicioEvento.getInstancia().findById(id);

        if (e != null) {
            e.setPublicado(!e.isPublicado());
            ServicioEvento.getInstancia().actualizar(e);
        }
        ctx.redirect("/eventos");
    }

    public static void eliminar(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        ServicioEvento.getInstancia().eliminar(id);
        ctx.redirect("/eventos");
    }

    public static void vistaVer(Context ctx) {

        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(id);


        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (evento != null) {
            boolean estaInscrito = false;

            if (usuarioLogueado != null && evento.getInscripciones() != null) {
                estaInscrito = evento.getInscripciones().stream()
                        .anyMatch(ins -> {
                            return ins.getUsuario().getId() == usuarioLogueado.getId();
                        });
            }

            Map<String, Object> model = new HashMap<>();
            model.put("evento", evento);
            model.put("usuario", usuarioLogueado);
            model.put("estaInscrito", estaInscrito);

            ctx.render("templates/verEvento.html", model);
        } else {
            ctx.redirect("/eventos");
        }
    }
}