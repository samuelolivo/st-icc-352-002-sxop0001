package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.RolesUsuario;
import org.example.models.EstadoObjeto;
import org.example.models.Usuario;
import org.example.services.ServicioUsuario;

import java.util.HashMap;
import java.util.Map;

public class ControladorRegistro {

    public static void vistaRegistro(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("templates/registro.html", model);
    }

    public static void registrar(Context ctx) {
        try {
            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");
            String passwordConfirmacion = ctx.formParam("passwordConfirmacion");


            if (usuario == null || usuario.trim().isEmpty()) {
                ctx.redirect("/registro?error=1");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                ctx.redirect("/registro?error=2");
                return;
            }

            if (!password.equals(passwordConfirmacion)) {
                ctx.redirect("/registro?error=3");
                return;
            }

            if (ServicioUsuario.getInstancia().findByUsername(usuario) != null) {
                ctx.redirect("/registro?error=4");
                return;
            }


            Usuario nuevoUsuario = new Usuario(usuario, password, RolesUsuario.PARTICIPANTE);
            nuevoUsuario.setEstado(EstadoObjeto.ACTIVO);

            ServicioUsuario.getInstancia().crearRegistro(nuevoUsuario);


            ctx.redirect("/login?success=1");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.redirect("/registro?error=5");
        }
    }
}


