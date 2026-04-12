package org.example.controllers;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketController {

    private static final Map<WsContext, String> clientesConectados = new ConcurrentHashMap<>();

    public void configurarRutas(WsConfig ws) {

        ws.onConnect(ctx -> {
            System.out.println("Nueva conexión WebSocket establecida: " + ctx.sessionId());
            clientesConectados.put(ctx, ctx.sessionId());
        });

        ws.onMessage(ctx -> {
            String mensaje = ctx.message();
            System.out.println("Mensaje recibido por WS: " + mensaje);

            clientesConectados.keySet().stream()
                    .filter(c -> c.session.isOpen())
                    .forEach(c -> c.send(mensaje));
        });

        ws.onClose(ctx -> {
            System.out.println("Conexión WebSocket cerrada: " + ctx.sessionId());
            clientesConectados.remove(ctx);
        });

        ws.onError(ctx -> {
            System.out.println("Error en WebSocket: " + ctx.sessionId());
            clientesConectados.remove(ctx);
        });
    }
}