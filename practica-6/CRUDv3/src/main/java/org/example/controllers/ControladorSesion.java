package org.example.controllers;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import org.example.Main;
import org.example.models.Carrito;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;
import org.example.services.ServicioLog; // Servicio JDBC
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.HashMap;
import java.util.Map;

import static org.example.Main.servicioCarrito;
import static org.example.Main.servicioUsuario;

public class ControladorSesion {

    private static final String COOKIE_USER = "recordar";
    private static final String CLAVE_ENCRIPCION = "gustavo-protege-mi-contra";

    public static void sesion(Context ctx) {
        Usuario usuarioSesion = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        System.out.println(ctx.path());

        if (usuarioSesion == null) {
            String cookieValor = ctx.cookie(COOKIE_USER);

            if (cookieValor != null) {
                try {
                    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                    textEncryptor.setPassword(CLAVE_ENCRIPCION);
                    String usuarioDesencriptado = textEncryptor.decrypt(cookieValor);

                    Usuario u = servicioUsuario.buscarPorUsername(usuarioDesencriptado);
                    if (u != null) {
                        ctx.sessionAttribute(Main.KeySession.USUARIO.name(), u);
                    }
                } catch (Exception e) {
                    ctx.removeCookie(COOKIE_USER);
                }
            }
            else {
                Usuario u = Usuario.usuarioInvitado();
                Carrito c = new Carrito(u);
                ctx.sessionAttribute(Main.KeySession.USUARIO_NA.name(), u);
                ctx.sessionAttribute(Main.KeySession.USUARIO.name(), u);

                ctx.sessionAttribute(Main.KeySession.CARRITO_NA.name(), c);
            }
        }
    }

    public static void adminValido(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        if (usuario == null || usuario.getRol() != RolesUsuario.ADMIN) {
            ctx.redirect("/producto/lista"); // Redirigir si no es admin
        }
    }

    public static void vistaLogin(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        if (ctx.queryParam("error") != null) {
            model.put("error", "Credenciales inválidas.");
        }
        ctx.render("templates/login.html", model);
    }

    public static void procesarLogin(Context ctx) {
        String nombre = ctx.formParam("usuario");
        String password = ctx.formParam("password");
        boolean recordar = Boolean.parseBoolean(ctx.formParam("recordar"));

        Usuario usuario = servicioUsuario.validarLogin(nombre, password);

        if (usuario != null) {
            ctx.sessionAttribute(Main.KeySession.USUARIO.name(), usuario);

            if (recordar) {
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(CLAVE_ENCRIPCION);
                String valorEncriptado = textEncryptor.encrypt(usuario.getUsuario());

                Cookie cookie = new Cookie(COOKIE_USER, valorEncriptado);
                cookie.setMaxAge(604800); // 1 semana
                cookie.setHttpOnly(true);
                ctx.cookie(cookie);
            }


            ServicioLog.registrarAcceso(usuario.getUsuario());

            servicioCarrito.mergeCarritoLogin(usuario, ctx.sessionAttribute(Main.KeySession.CARRITO_NA.name()));

            String anteriorPath = ctx.sessionAttribute(Main.KeySession.REFERER.name());
            ctx.redirect((anteriorPath != null) ? anteriorPath : "/producto/lista");
        } else {
            ctx.redirect("/login?error=1");
        }
    }

    public static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.removeCookie(COOKIE_USER);
        ctx.redirect("/producto/lista");
    }

    public static void defaultPath(Context ctx) { ctx.redirect("/producto/lista"); }

    public static void volver(Context ctx) {
        String path = ctx.sessionAttribute(Main.KeySession.REFERER.name());
        ctx.redirect(path != null ? path : "/producto/lista");
    }
}