package org.example.services;

import org.h2.tools.Server;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.models.Usuario;
import org.example.models.RolesUsuario;

import java.sql.SQLException;

public class BootStrapServices {

    private static Server server;
    private static EntityManagerFactory emf;

    public static void startDb() {
        try {

            server = Server.createTcpServer("-tcpPort", "9091", "-tcpAllowOthers", "-ifNotExists").start();
            System.out.println("Servidor H2 iniciado correctamente.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void stopDb() {
        if (server != null) {
            server.stop();
        }
        if (emf != null) {
            emf.close();
        }
    }

    public static void init() {
        emf = Persistence.createEntityManagerFactory("PersistenciaTienda");
        crearUsuarioAdmin();
    }

    private static void crearUsuarioAdmin() {
        EntityManager em = emf.createEntityManager();
        try {
            long count = (long) em.createQuery("SELECT count(u) FROM Usuario u WHERE u.usuario = 'admin'").getSingleResult();

            if (count == 0) {
                em.getTransaction().begin();
                Usuario admin = new Usuario("admin", "admin", RolesUsuario.ADMIN);
                em.persist(admin);
                em.getTransaction().commit();
                System.out.println("Usuario administrador inicial creado (admin/admin).");
            }
        } finally {
            em.close();
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}