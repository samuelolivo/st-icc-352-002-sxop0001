package org.example.controllers;

import io.javalin.websocket.WsConfig; // Importe correcto para Javalin 7
import io.javalin.websocket.WsContext;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorNotificacion {

    private static final Set<WsContext> sesionesActivas = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void gestionarWebsocket(WsConfig ws) {

        ws.onConnect(ctx -> {
            sesionesActivas.add(ctx);
            enviarConteoUsuarios();
            System.out.println("Nuevo usuario conectado. Total: " + sesionesActivas.size());
        });

        ws.onClose(ctx -> {
            sesionesActivas.remove(ctx);
            enviarConteoUsuarios();
            System.out.println("Usuario desconectado. Total: " + sesionesActivas.size());
        });

        ws.onError(ctx -> {
            sesionesActivas.remove(ctx);
            enviarConteoUsuarios();
        });
    }

    private static void enviarConteoUsuarios() {
        broadcast(Map.of(
                "tipo", "CONTEO_USUARIOS",
                "cantidad", sesionesActivas.size()
        ));
    }

    public static void notificarEliminacionComentario(Long idComentario) {
        broadcast(Map.of(
                "tipo", "ELIMINAR_COMENTARIO",
                "id", idComentario
        ));
    }

    private static void broadcast(Object mensaje) {
        sesionesActivas.forEach(session -> {
            if (session.session.isOpen()) {
                session.send(mensaje);
            }
        });
    }
}