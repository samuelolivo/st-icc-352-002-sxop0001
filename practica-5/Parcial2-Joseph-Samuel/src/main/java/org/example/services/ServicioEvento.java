package org.example.services;

import org.example.models.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class ServicioEvento {
    private static ServicioEvento instancia;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenciaEventos");

    public static ServicioEvento getInstancia() {
        if (instancia == null) instancia = new ServicioEvento();
        return instancia;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.organizador", Evento.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Evento findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT e FROM Evento e " +
                                    "LEFT JOIN FETCH e.organizador " +
                                    "LEFT JOIN FETCH e.inscripciones i " +
                                    "LEFT JOIN FETCH i.usuario " +
                                    "WHERE e.id = :id", Evento.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            try {
                Evento evento = em.find(Evento.class, id);
                if (evento != null) {

                    if (evento.getOrganizador() != null) {
                        evento.getOrganizador().getId();
                    }
                    if (evento.getInscripciones() != null) {
                        evento.getInscripciones().size();
                    }
                }
                return evento;
            } catch (Exception ex) {
                return null;
            }
        } finally {
            em.close();
        }
    }

    public void crear(Evento evento) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(evento);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void actualizar(Evento evento) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(evento);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Evento e = em.find(Evento.class, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}