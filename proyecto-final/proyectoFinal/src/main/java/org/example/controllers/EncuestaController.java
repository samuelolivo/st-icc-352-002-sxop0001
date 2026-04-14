package org.example.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.javalin.http.Context;
import org.example.Main;
import org.example.grpc.EncuestaRequest;
import org.example.grpc.EncuestaResponse;
import org.example.grpc.EncuestaServiceGrpc;
import org.example.models.Encuesta;
import org.example.models.EstadoSincronizacion;
import org.example.models.NivelEscolar;
import org.example.models.Ubicacion;
import org.example.services.EncuestaService;
import org.example.utils.JwtUtil;

import java.time.LocalDateTime;
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

    public void procesarEncuestaGrpc(Context ctx) {

        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        String nombre = (String) body.get("nombre");
        String sector = (String) body.get("sector");
        String nivelString = (String) body.get("nivelEscolar");
        String fotoBase64 = (String) body.get("fotoBase64");
        String usuarioEmail = (String) body.get("usuario");

        Map<String, Object> ubiMap = (Map<String, Object>) body.get("ubicacion");
        double lat = Double.parseDouble(ubiMap.get("latitud").toString());
        double lon = Double.parseDouble(ubiMap.get("longitud").toString());


        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        try {
            EncuestaServiceGrpc.EncuestaServiceBlockingStub stub = EncuestaServiceGrpc.newBlockingStub(channel);

            EncuestaRequest request = EncuestaRequest.newBuilder()
                    .setNombre(nombre)
                    .setSector(sector)
                    .setNivelEscolar(nivelString)
                    .build();

            EncuestaResponse response = stub.sincronizar(request);

            Encuesta nuevaEncuesta = new Encuesta();
            nuevaEncuesta.setNombre(nombre);
            nuevaEncuesta.setSector(sector);


            nuevaEncuesta.setNivelEscolar(NivelEscolar.valueOf(nivelString.toUpperCase()));


            nuevaEncuesta.setUbicacion(new Ubicacion(lat, lon));

            nuevaEncuesta.setFotoBase64(fotoBase64);
            nuevaEncuesta.setUsuarioId(usuarioEmail);
            nuevaEncuesta.setEstadoSync(EstadoSincronizacion.SINCRONIZADO);
            nuevaEncuesta.setEstadoObjeto(true);
            nuevaEncuesta.setFechaCreacion(LocalDateTime.now());
            nuevaEncuesta.setFechaSincronizacion(LocalDateTime.now());

            encuestaService.guardar(nuevaEncuesta);

            ctx.status(200).json(Map.of(
                    "status", "success",
                    "mensaje", "Respuesta gRPC: " + response.getMensaje()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("status", "error", "mensaje", e.getMessage()));
        } finally {
            channel.shutdown();
        }
    }
}
