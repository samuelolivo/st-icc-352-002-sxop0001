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
            ctx.redirect("/eventos");
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

        if (inscripcion != null) {
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
            ctx.redirect("/eventos");
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


            Long eventoId;
            Object eventIdObj = datosQR.get("eventId");
            if (eventIdObj instanceof Integer) {
                eventoId = ((Integer) eventIdObj).longValue();
            } else if (eventIdObj instanceof Long) {
                eventoId = (Long) eventIdObj;
            } else if (eventIdObj instanceof Double) {
                eventoId = ((Double) eventIdObj).longValue();
            } else if (eventIdObj instanceof String) {
                eventoId = Long.parseLong((String) eventIdObj);
            } else {
                ctx.status(400);
                ctx.json(Map.of("success", false, "mensaje", "Formato de eventId inválido"));
                return;
            }


            Integer usuarioId;
            Object userIdObj = datosQR.get("userId");
            if (userIdObj instanceof Integer) {
                usuarioId = (Integer) userIdObj;
            } else if (userIdObj instanceof Long) {
                usuarioId = ((Long) userIdObj).intValue();
            } else if (userIdObj instanceof Double) {
                usuarioId = ((Double) userIdObj).intValue();
            } else if (userIdObj instanceof String) {
                usuarioId = Integer.parseInt((String) userIdObj);
            } else {
                ctx.status(400);
                ctx.json(Map.of("success", false, "mensaje", "Formato de userId inválido"));
                return;
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
                ctx.json(Map.of("success", false, "mensaje", "Asistencia ya registrada"));
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

