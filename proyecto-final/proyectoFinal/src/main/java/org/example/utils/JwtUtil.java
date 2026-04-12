package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.example.models.Usuario;
import java.util.Date;

public class JwtUtil {

    private static final String CLAVE_SECRETA = "camacho_y_gustavo_protejan_mi_contra_123";
    private static final String EMISOR = "SistemaEncuestasApp";

    public static String generarToken(Usuario usuario) {
        Algorithm algoritmo = Algorithm.HMAC256(CLAVE_SECRETA);
        long tiempoExpiracion = 24L * 60 * 60 * 1000; //1 dia
        Date fechaExpiracion = new Date(System.currentTimeMillis() + tiempoExpiracion);

        return JWT.create()
                .withIssuer(EMISOR)
                .withSubject(usuario.getId())
                .withClaim("email", usuario.getEmail())
                .withClaim("rol", usuario.getRol().name())
                .withClaim("nombre", usuario.getNombre())
                .withExpiresAt(fechaExpiracion)
                .sign(algoritmo);
    }

    public static DecodedJWT validarToken(String token) {
        Algorithm algoritmo = Algorithm.HMAC256(CLAVE_SECRETA);
        JWTVerifier verificador = JWT.require(algoritmo)
                .withIssuer(EMISOR)
                .build();

        return verificador.verify(token);
    }
}