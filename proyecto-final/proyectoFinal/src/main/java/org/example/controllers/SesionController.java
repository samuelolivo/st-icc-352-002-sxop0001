package org.example.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import org.example.Main;
import org.example.services.UsuarioService;
import org.example.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class SesionController {

    private static final String COOKIE_JWT = "recordar";
    private final UsuarioService usuarioService;

    public SesionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void sesion(Context ctx) {
        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());
        System.out.println(ctx.path());

        if (jwt == null) {
            jwt = ctx.cookie(COOKIE_JWT);
        }

        if (jwt != null) {
            try {
                DecodedJWT djwt = JwtUtil.validarToken(jwt);
                String email = djwt.getClaim("email").asString();
                usuarioService.buscarActivoPorEmail(email);
                ctx.sessionAttribute(Main.KeySession.JWT.name(), jwt);
            } catch (Exception e) {
                ctx.removeCookie(COOKIE_JWT);
                ctx.sessionAttribute(Main.KeySession.JWT.name(), null);
            }
        }
    }

    public void adminValido(Context ctx) {
        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());

        if (jwt == null) {
            ctx.redirect("/login");
            return;
        }

        try {
            DecodedJWT djwt = JwtUtil.validarToken(jwt);
            String rol = djwt.getClaim("rol").asString();

            if (!"ADMIN".equals(rol)) {
                ctx.redirect("/encuesta");
            }

        } catch (Exception e) {
            ctx.redirect("/login");
        }
    }

    public void encuestadorValido(Context ctx) {
        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());

        if (jwt == null) {
            ctx.redirect("/login");
            return;
        }

        try {
            DecodedJWT djwt = JwtUtil.validarToken(jwt);
            String rol = djwt.getClaim("rol").asString();

            if (!"ADMIN".equals(rol) && !"ENCUESTADOR".equals(rol)) {
                ctx.redirect("/encuesta");
            }

        } catch (Exception e) {
            ctx.redirect("/login");
        }
    }

    public void vistaLogin(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        if (ctx.queryParam("error") != null) {
            model.put("error", "Credenciales inválidas.");
        }
        ctx.render("templates/login.html", model);
    }

    public void procesarLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        boolean recordar = Boolean.parseBoolean(ctx.formParam("recordar"));

        String JWT = usuarioService.autenticar(email, password);

        if (JWT != null) {
            ctx.sessionAttribute(Main.KeySession.JWT.name(), JWT);

            if (recordar) {
                Cookie cookie = new Cookie(COOKIE_JWT, JWT);
                cookie.setMaxAge(86400); // 1 dia
                cookie.setHttpOnly(true);
                ctx.cookie(cookie);
            }

            String anteriorPath = ctx.sessionAttribute(Main.KeySession.REFERER.name());
            ctx.redirect((anteriorPath != null) ? anteriorPath : "/encuesta");
        } else {
            ctx.redirect("/login?error=1");
        }
    }

    public void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.removeCookie(COOKIE_JWT);
        ctx.redirect("/encuesta");
    }

    public void defaultPath(Context ctx) { ctx.redirect("/encuesta"); }

    public void volver(Context ctx) {
        String path = ctx.sessionAttribute(Main.KeySession.REFERER.name());
        ctx.redirect(path != null ? path : "/encuesta");
    }
}