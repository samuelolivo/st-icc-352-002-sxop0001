//storage.js
const DB_NAME = "EncuestasDB";
const DB_VERSION = 1;
const STORE_ENCUESTAS = "encuestas_store";

function openDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION);

        request.onupgradeneeded = (event) => {
            const db = event.target.result;
            if (!db.objectStoreNames.contains(STORE_ENCUESTAS)) {
                db.createObjectStore(STORE_ENCUESTAS, { keyPath: "localId", autoIncrement: true });
            }
        };

        request.onsuccess = (event) => resolve(event.target.result);
        request.onerror = (event) => reject(event.target.error);
    });
}

export async function guardarEncuesta(encuesta) {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const transaction = db.transaction([STORE_ENCUESTAS], "readwrite");
        const store = transaction.objectStore(STORE_ENCUESTAS);

        encuesta.estado = "PENDIENTE";
        encuesta.fechaGuardadoLocal = new Date().toISOString();

        const request = store.add(encuesta);

        request.onsuccess = () => resolve();
        request.onerror = (e) => reject(e.target.error);
    });
}

export async function getEncuestasPendientes() {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const transaction = db.transaction([STORE_ENCUESTAS], "readonly");
        const store = transaction.objectStore(STORE_ENCUESTAS);
        const request = store.getAll();

        request.onsuccess = () => resolve(request.result);
        request.onerror = (e) => reject(e.target.error);
    });
}

export async function clearEncuestasSincronizadas(localIds) {
    const db = await openDB();
    return new Promise((resolve, reject) => {
        const transaction = db.transaction([STORE_ENCUESTAS], "readwrite");
        const store = transaction.objectStore(STORE_ENCUESTAS);

        localIds.forEach(id => store.delete(id));

        transaction.oncomplete = () => resolve();
        transaction.onerror = (e) => reject(e.target.error);
    });
}


export function guardarUsuario(usuario) {
    localStorage.setItem("auth_user", JSON.stringify(usuario));
}

export function getUsuario() {
    return JSON.parse(localStorage.getItem("auth_user"));
}

export function guardarToken(token) {
    localStorage.setItem("auth_token", token);
}

export function getToken() {
    return localStorage.getItem("auth_token");
}