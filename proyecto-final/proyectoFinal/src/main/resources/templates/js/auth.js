//auth.js
import { guardarUsuario, guardarToken } from './storage.js';

class CredencialesError extends Error {}
class RespuestaError extends Error {}

export async function login(username, password) {
    try {
        const response = await fetch("/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (response.status === 401 || response.status === 403) {
            throw new CredencialesError("Credenciales incorrectas");
        }

        if (!response.ok) {
            throw new RespuestaError("Error del servidor");
        }

        const data = await response.json();

        if (!data.user || !data.token) {
            throw new RespuestaError("Respuesta invalida");
        }

        guardarUsuario(data.user);
        guardarToken(data.token);

        return { success: true, offline: false };

    } catch (err) {
        if (err instanceof CredencialesError) {
            console.error(err.message);
            return { success: false, offline: false, reason: "credenciales" };
        }

        if (err instanceof RespuestaError) {
            console.error(err.message);
            return { success: false, offline: false, reason: "servidor" };
        }

        console.log("Modo offline");
        return { success: false, offline: true, reason: "red" };
    }
}