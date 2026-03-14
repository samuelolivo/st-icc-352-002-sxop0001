package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Evento;
import org.example.models.Inscripcion;
import org.example.models.Usuario;
import org.example.models.RolesUsuario;
import org.example.services.ServicioEvento;
import org.example.services.ServicioInscripcion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorInscripcion {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void inscribirse(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Long eventoId = ctx.pathParamAsClass("id", Long.class).get();


        if (usuarioLogueado == null || usuarioLogueado.getRol() == RolesUsuario.NO_AUTENTICADO) {
            ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/evento/ver/" + eventoId);
            ctx.redirect("/login");
            return;
        }

        Evento evento = ServicioEvento.getInstancia().findById(eventoId);

        if (evento == null) {
            ctx.redirect("/evento/lista");
            return;
        }

        if (ServicioInscripcion.getInstancia().usuarioInscrito(eventoId, usuarioLogueado.getId())) {
            ctx.redirect("/evento/ver/" + eventoId + "?error=1");
            return;
        }


        if (!evento.tieneCupo()) {
            ctx.redirect("/evento/ver/" + eventoId + "?error=2");
            return;
        }

        try {
            Inscripcion inscripcion = ServicioInscripcion.getInstancia().crear(evento, usuarioLogueado);
            ctx.sessionAttribute("inscripcionId", inscripcion.getId());
            ctx.redirect("/evento/ver/" + eventoId + "?success=1&inscripcionId=" + inscripcion.getId());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.redirect("/evento/ver/" + eventoId + "?error=3");
        }
    }

    public static void desinscribirse(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Long eventoId = ctx.pathParamAsClass("id", Long.class).get();


        if (usuarioLogueado == null || usuarioLogueado.getRol() == RolesUsuario.NO_AUTENTICADO) {
            ctx.redirect("/login");
            return;
        }

        Inscripcion inscripcion = ServicioInscripcion.getInstancia().findByEventoAndUsuario(eventoId, usuarioLogueado.getId());
        LocalDate fechaEvento = inscripcion.getEvento().getFechaHora().toLocalDate();
        LocalDate hoy = LocalDate.now();

        if (inscripcion != null && hoy.isBefore(fechaEvento)) {
            try {
                ServicioInscripcion.getInstancia().eliminar(inscripcion.getId());
                ctx.redirect("/evento/ver/" + eventoId + "?success=2");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.redirect("/evento/ver/" + eventoId + "?error=3");
            }
        } else {
            ctx.redirect("/evento/ver/" + eventoId);
        }
    }

    public static void misinscripciones(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (usuarioLogueado == null || usuarioLogueado.getRol() == RolesUsuario.NO_AUTENTICADO) {
            ctx.redirect("/login");
            return;
        }

        List<Inscripcion> inscripciones = ServicioInscripcion.getInstancia().findByUsuario(usuarioLogueado.getId());

        Map<String, Object> model = new HashMap<>();
        model.put("inscripciones", inscripciones);
        model.put("usuario", usuarioLogueado);

        ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/mis-inscripciones");
        ctx.render("templates/misInscripciones.html", model);
    }

    public static void vistaEscanearQR(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (usuarioLogueado == null || usuarioLogueado.getRol() != RolesUsuario.ADMIN) {
            ctx.redirect("/evento/lista");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", usuarioLogueado);

        ctx.render("templates/admin/escanearQR.html", model);
    }

    public static void validarQR(Context ctx) {
        try {
            Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

            if (usuarioLogueado == null || usuarioLogueado.getRol() != RolesUsuario.ADMIN) {
                ctx.status(403);
                ctx.json(Map.of("success", false, "mensaje", "No tienes permisos"));
                return;
            }

            String qrData = ctx.formParam("qrData");

            if (qrData == null || qrData.isEmpty()) {
                ctx.status(400);
                ctx.json(Map.of("success", false, "mensaje", "QR vacío"));
                return;
            }


            Map<String, Object> datosQR = objectMapper.readValue(qrData, Map.class);


            long eventoId;
            Object eventIdObj = datosQR.get("eventId");
            switch (eventIdObj) {
                case Integer i -> eventoId = i.longValue();
                case Long l -> eventoId = l;
                case Double v -> eventoId = v.longValue();
                case String s -> eventoId = Long.parseLong(s);
                case null, default -> {
                    ctx.status(400);
                    ctx.json(Map.of("success", false, "mensaje", "Formato de eventId inválido"));
                    return;
                }
            }


            int usuarioId;
            Object userIdObj = datosQR.get("userId");
            switch (userIdObj) {
                case Integer i -> usuarioId = i;
                case Long l -> usuarioId = l.intValue();
                case Double v -> usuarioId = v.intValue();
                case String s -> usuarioId = Integer.parseInt(s);
                case null, default -> {
                    ctx.status(400);
                    ctx.json(Map.of("success", false, "mensaje", "Formato de userId inválido"));
                    return;
                }
            }


            String token = (String) datosQR.get("token");
            if (token == null || token.isEmpty()) {
                ctx.status(400);
                ctx.json(Map.of("success", false, "mensaje", "Token inválido"));
                return;
            }

            Inscripcion inscripcion = ServicioInscripcion.getInstancia().findByEventoAndUsuario(eventoId, usuarioId);

            if (inscripcion == null) {
                ctx.status(404);
                ctx.json(Map.of("success", false, "mensaje", "Inscripción no encontrada"));
                return;
            }

            if (!token.equals(inscripcion.getTokenQr())) {
                ctx.status(401);
                ctx.json(Map.of("success", false, "mensaje", "Token QR inválido"));
                return;
            }

            if (inscripcion.isAsistio()) {
                ctx.status(409);
                ctx.json(Map.of("success", false, "mensaje", "Asistencia ha sido registrada previamente"));
                return;
            }

            LocalDate fechaEvento = inscripcion.getEvento().getFechaHora().toLocalDate();
            LocalDate hoy = LocalDate.now();

            if (!fechaEvento.equals(hoy)) {
                ctx.status(409);
                ctx.json(Map.of("success", false, "mensaje", "Solo se puede registrar asistencia el día del evento"));
                return;
            }

            inscripcion.setAsistio(true);
            inscripcion.setFechaAsistencia(LocalDateTime.now());
            ServicioInscripcion.getInstancia().actualizar(inscripcion);

            ctx.json(Map.of(
                    "success", true,
                    "mensaje", "Asistencia registrada exitosamente",
                    "usuario", inscripcion.getUsuario().getUsuario(),
                    "evento", inscripcion.getEvento().getTitulo()
            ));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(Map.of("success", false, "mensaje", "Valores numéricos inválidos en el QR"));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of("success", false, "mensaje", "Error al procesar QR"));
        }
    }

    public static void obtenerQR(Context ctx) {
        Long inscripcionId = ctx.pathParamAsClass("id", Long.class).get();
        Inscripcion inscripcion = ServicioInscripcion.getInstancia().findById(inscripcionId);

        if (inscripcion == null) {
            ctx.status(404);
            ctx.json(Map.of("success", false));
            return;
        }

        Map<String, Object> qrData = new HashMap<>();
        qrData.put("eventId", inscripcion.getEvento().getId());
        qrData.put("userId", inscripcion.getUsuario().getId());
        qrData.put("token", inscripcion.getTokenQr());

        try {
            String jsonData = objectMapper.writeValueAsString(qrData);
            ctx.json(Map.of(
                    "success", true,
                    "data", jsonData,
                    "usuario", inscripcion.getUsuario().getUsuario(),
                    "evento", inscripcion.getEvento().getTitulo()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of("success", false));
        }
    }
}

