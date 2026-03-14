package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Evento;
import org.example.models.Inscripcion;
import org.example.models.Usuario;
import org.example.services.ServicioEvento;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class ControladorEvento {

    public static void listar(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        List<Evento> eventos = ServicioEvento.getInstancia().findAll();

        Map<String, Object> model = new HashMap<>();
        model.put("eventos", eventos);
        model.put("usuario", usuarioLogueado);

        ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/evento/lista");
        ctx.render("templates/eventos.html", model);
    }

    public static void vistaCrear(Context ctx) {
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

        ctx.redirect("/evento/lista");
    }



    public static void vistaModificar(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(id);

        if (evento != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("evento", evento);
            ctx.render("templates/admin/editarEvento.html", model);
        } else {
            ctx.redirect("/evento/lista");
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
        ctx.redirect("/evento/lista");
    }

    public static void alternarPublicacion(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento e = ServicioEvento.getInstancia().findById(id);

        if (e != null) {
            e.setPublicado(!e.isPublicado());
            ServicioEvento.getInstancia().actualizar(e);
        }
        ctx.redirect("/evento/lista");
    }

    public static void eliminar(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        ServicioEvento.getInstancia().eliminar(id);
        ctx.redirect("/evento/lista");
    }

    public static void vistaVer(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(id);

        List<Inscripcion> inscripciones = evento.getInscripciones();

        Map<String, Long> inscripcionesPorDia = inscripciones.stream()
                .filter(i -> i.getFechaInscripcion() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getFechaInscripcion().format(DateTimeFormatter.ofPattern("dd/MM")),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> asistenciaPorHora = inscripciones.stream()
                .filter(i -> i.isAsistio() && i.getFechaAsistencia() != null)
                .collect(Collectors.groupingBy(
                        i -> String.format("%02d", i.getFechaAsistencia().getHour()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        long totalInscritos  = inscripciones.size();
        long totalAsistentes = inscripciones.stream().filter(Inscripcion::isAsistio).count();
        double porcentaje    = totalInscritos > 0
                ? Math.round((totalAsistentes * 100.0 / totalInscritos) * 10.0) / 10.0
                : 0.0;

        if (evento != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("evento", evento);
            model.put("totalInscritos",   totalInscritos);
            model.put("totalAsistentes",  totalAsistentes);
            model.put("porcentajeAsistencia", porcentaje);
            model.put("inscripcionesPorDia", inscripcionesPorDia);
            model.put("asistenciaPorHora",   asistenciaPorHora);
            model.put("usuario", ctx.sessionAttribute(Main.KeySession.USUARIO.name()));
            ctx.render("templates/verEvento.html", model);
        } else {
            ctx.redirect("/evento/lista");
        }
    }
}