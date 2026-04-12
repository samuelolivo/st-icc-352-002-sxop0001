let socket;

export function connectWebSocket() {
    const host = window.location.host;
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    socket = new WebSocket(`${protocol}://${host}/ws`);

    socket.onopen = () => {
        console.log("WebSocket conectado");
    };

    socket.onmessage = (event) => {
        console.log("Mensaje del servidor:", event.data);
    };

    socket.onclose = () => {
        console.log("WebSocket desconectado");
        console.warn("OFFLINE FUNCIONA");
        socket = null;
    };

    socket.onerror = (err) => {
        console.error("WebSocket error:", err);
    };
}

export function enviarMensaje(data) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(data));
    } else {
        console.warn("WebSocket no conectado");
    }
}