//syncEncuestas.js
self.onmessage = async function (e) {
    const { encuestas, url } = e.data;
    try {
        await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json"},
            body: JSON.stringify(encuestas)
        });

        self.postMessage({ status: "OK", encuestas});

    } catch (err) {
        self.postMessage({status: "ERROR", encuestas});
    }
};