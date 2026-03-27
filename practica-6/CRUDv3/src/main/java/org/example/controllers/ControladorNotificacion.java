package org.example.controllers;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import org.example.Main;
import org.example.models.Producto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;
import org.example.models.Venta;

import java.util.*;
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
        long cantidadUsuariosUnicos = sesionesActivas.stream()
                .map(session -> (Usuario) session.sessionAttribute(Main.KeySession.USUARIO.name()))
                .filter(u -> u != null && u.getRol() != RolesUsuario.NO_AUTENTICADO)
                .map(u -> u.getId())
                .distinct()
                .count();

        broadcast(Map.of(
                "tipo", "CONTEO_USUARIOS",
                "cantidad", cantidadUsuariosUnicos
        ));
    }

    public static void notificarEliminacionComentario(Long idComentario) {
        broadcast(Map.of(
                "tipo", "ELIMINAR_COMENTARIO",
                "id", idComentario
        ));
    }

    public static void notificarNuevaVenta(Venta venta) {
        Map<String, Integer> conteoProductos = new HashMap<>();

        for (Producto p : venta.getListaProducto()) {
            String nombre = p.getNombre();
            conteoProductos.put(nombre, conteoProductos.getOrDefault(nombre, 0) + 1);
        }

        List<Map<String, Object>> productosResumen = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : conteoProductos.entrySet()) {
            productosResumen.add(Map.of(
                    "nombre", entrada.getKey(),
                    "cantidad", entrada.getValue()
            ));
        }

        broadcastAdmin(Map.of(
                "tipo", "NUEVA_VENTA",
                "productos", productosResumen
        ));
    }

    private static void broadcast(Object mensaje) {
        sesionesActivas.forEach(session -> {
            if (session.session.isOpen()) {
                session.send(mensaje);
            }
        });
    }

    private static void broadcastAdmin(Object mensaje) {
        sesionesActivas.forEach(session -> {
            if (session.session.isOpen()) {
                Usuario usuario = session.sessionAttribute(Main.KeySession.USUARIO.name());

                if (usuario != null && usuario.getRol().equals(RolesUsuario.ADMIN)) {
                    session.send(mensaje);
                }
            }
        });
    }
}