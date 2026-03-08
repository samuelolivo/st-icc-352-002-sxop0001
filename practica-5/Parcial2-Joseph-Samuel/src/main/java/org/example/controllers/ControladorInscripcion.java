package org.example.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Evento;
import org.example.models.Inscripcion;
import org.example.models.Usuario;
import org.example.services.ServicioEvento;
import org.example.services.ServicioInscripcion;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ControladorInscripcion {

    public static void inscribirse(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Long eventoId = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(eventoId);

        if (evento == null || !evento.isPublicado()) {
            ctx.redirect("/evento/lista");
            return;
        }

        ServicioInscripcion.ResultadoInscripcion resultado =
                ServicioInscripcion.getInstancia().inscribir(evento, usuario);

        switch (resultado) {
            case EXITO      -> ctx.redirect("/inscripcion/mis-inscripciones?exito=1");
            case DUPLICADA  -> ctx.redirect("/evento/ver/" + eventoId + "?error=duplicada");
            case SIN_CUPO   -> ctx.redirect("/evento/ver/" + eventoId + "?error=cupo");
            default         -> ctx.redirect("/evento/ver/" + eventoId + "?error=general");
        }
    }

    public static void misInscripciones(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        Map<String, Object> model = new HashMap<>();
        model.put("inscripciones", ServicioInscripcion.getInstancia().listarPorUsuario(usuario.getId()));
        model.put("usuario", usuario);

        ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/inscripcion/mis-inscripciones");
        ctx.render("templates/misInscripciones.html", model);
    }

    public static void cancelar(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Long id = ctx.pathParamAsClass("id", Long.class).get();

        ServicioInscripcion.getInstancia().cancelar(id, usuario);
        ctx.redirect("/inscripcion/mis-inscripciones");
    }

    public static void verQr(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Long id = ctx.pathParamAsClass("id", Long.class).get();

        var inscripciones = ServicioInscripcion.getInstancia().listarPorUsuario(usuario.getId());
        Inscripcion inscripcion = inscripciones.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst().orElse(null);

        if (inscripcion == null) {
            ctx.status(404);
            return;
        }

        try {
            String contenidoQr = "evento=" + inscripcion.getEvento().getId()
                    + "&usuario=" + inscripcion.getUsuario().getId()
                    + "&token=" + inscripcion.getTokenQr();

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenidoQr, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

            ctx.contentType("image/png");
            ctx.result(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    public static void vistaEscanear(Context ctx) {
        Long eventoId = ctx.pathParamAsClass("id", Long.class).get();
        Evento evento = ServicioEvento.getInstancia().findById(eventoId);

        Map<String, Object> model = new HashMap<>();
        model.put("evento", evento);
        model.put("usuario", ctx.sessionAttribute(Main.KeySession.USUARIO.name()));
        ctx.render("templates/admin/escanearQr.html", model);
    }

    public static void validarQr(Context ctx) {
        String token = ctx.formParam("token");
        boolean exito = ServicioInscripcion.getInstancia().marcarAsistencia(token);

        Map<String, Object> model = new HashMap<>();
        if (exito) {
            Inscripcion inscripcion = ServicioInscripcion.getInstancia().buscarPorToken(token);
            model.put("inscripcion", inscripcion);
            model.put("exito", true);
        } else {
            model.put("error", "QR inválido, ya registrado o no encontrado.");
        }
        model.put("usuario", ctx.sessionAttribute(Main.KeySession.USUARIO.name()));
        ctx.render("templates/admin/escanearQr.html", model);
    }
}