package org.example.services;

import org.example.models.Evento;
import org.example.models.Inscripcion;
import org.example.models.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

import static org.example.services.BootStrapServices.getEntityManager;

public class ServicioInscripcion {
    private static ServicioInscripcion instancia;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenciaEventos");

    public static ServicioInscripcion getInstancia() {
        if (instancia == null) instancia = new ServicioInscripcion();
        return instancia;
    }

    public Inscripcion crear(Evento evento, Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Inscripcion inscripcion = new Inscripcion(evento, usuario);
            em.persist(inscripcion);
            em.getTransaction().commit();
            return inscripcion;
        } finally {
            em.close();
        }
    }

    public Inscripcion findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i " +
                                    "JOIN FETCH i.usuario " +
                                    "JOIN FETCH i.evento " +
                                    "WHERE i.id = :id", Inscripcion.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Inscripcion> findByEvento(Long eventoId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("from Inscripcion where evento.id = :eventoId", Inscripcion.class)
                    .setParameter("eventoId", eventoId)
                    .getResultList();
        }
    }

    public List<Inscripcion> findByUsuario(int usuarioId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i JOIN FETCH i.evento WHERE i.usuario.id = :usuarioId",
                            Inscripcion.class)
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Inscripcion findByEventoAndUsuario(Long eventoId, int usuarioId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i " +
                                    "JOIN FETCH i.usuario " +
                                    "JOIN FETCH i.evento " +
                                    "WHERE i.evento.id = :eventoId AND i.usuario.id = :usuarioId", Inscripcion.class)
                    .setParameter("eventoId", eventoId)
                    .setParameter("usuarioId", usuarioId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean usuarioInscrito(Long eventoId, Integer usuarioId) {
        return findByEventoAndUsuario(eventoId, usuarioId) != null;
    }

    public void actualizar(Inscripcion inscripcion) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(inscripcion);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Inscripcion i = em.find(Inscripcion.class, id);
            if (i != null) em.remove(i);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}

