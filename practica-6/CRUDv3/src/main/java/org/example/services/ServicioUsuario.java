package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.EstadoObjeto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;

import java.util.List;

public class ServicioUsuario {

    private static ServicioUsuario instancia;

    public static ServicioUsuario getInstancia() {
        if (instancia == null) {
            instancia = new ServicioUsuario();
        }
        return instancia;
    }

    private ServicioUsuario() {
    }


    public Usuario crear(String username, String password, RolesUsuario rol) {
        Usuario usuario = new Usuario(username, password, rol);
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return usuario;
    }

    public Usuario crearRegistro(Usuario usuario) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return usuario;
    }

    public Usuario buscarPorId(int id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public boolean bloquear(int id) {
        EntityManager em = BootStrapServices.getEntityManager();

        try {
            em.getTransaction().begin();

            Usuario usuario = em.find(Usuario.class, id);

            if (usuario != null) {
                usuario.setBloqueado(!usuario.isBloqueado());
                em.merge(usuario);
                em.getTransaction().commit();
                return true;
            }

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        return false;
    }

    public Usuario buscarPorUsername(String username) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.usuario = :user", Usuario.class)
                    .setParameter("user", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Usuario findByUsername(String username) {
        return buscarPorUsername(username);
    }

    public Usuario validarLogin(String username, String password) {
        Usuario usuario = buscarPorUsername(username);
        if (usuario != null &&
                usuario.getPassword().equals(password) &&
                usuario.getEstado() == EstadoObjeto.ACTIVO) {
            return usuario;
        }
        return null;
    }

    public boolean modificarPorId(int id, String nuevoUsername, String nuevoPassword, RolesUsuario nuevoRol) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null && usuario.getEstado() == EstadoObjeto.ACTIVO) {
                usuario.setUsuario(nuevoUsername);
                usuario.setPassword(nuevoPassword);
                usuario.setRol(nuevoRol);
                em.merge(usuario);
                em.getTransaction().commit();
                return true;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return false;
    }

    public boolean borrarPorId(int id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null) {
                usuario.setEstado(EstadoObjeto.INACTIVO);
                em.merge(usuario);
                em.getTransaction().commit();
                return true;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return false;
    }

    public boolean borrarPorUsername(String username) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = buscarPorUsername(username);
            if (usuario != null) {
                Usuario userDb = em.find(Usuario.class, usuario.getId());
                userDb.setEstado(EstadoObjeto.INACTIVO);
                em.merge(userDb);
                em.getTransaction().commit();
                return true;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return false;
    }

    public boolean usurioNoAutenticado(Usuario u) {
        if (u != null && u.getRol().equals(RolesUsuario.NO_AUTENTICADO)) {
            return true;
        }
        return false;
    }

    public List<Usuario> listarTodos() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.rol <> :rol", Usuario.class)
                    .setParameter("rol", RolesUsuario.NO_AUTENTICADO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarActivos() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.estado = :est AND u.rol <> :rol", Usuario.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .setParameter("rol", RolesUsuario.NO_AUTENTICADO)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}