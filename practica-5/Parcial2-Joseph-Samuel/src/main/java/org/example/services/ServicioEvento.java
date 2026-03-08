package org.example.services;

import org.example.models.EstadoObjeto;
import org.example.models.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.models.Usuario;

import java.util.List;

public class ServicioEvento {
    private static ServicioEvento instancia;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenciaEventos");

    public static ServicioEvento getInstancia() {
        if (instancia == null) instancia = new ServicioEvento();
        return instancia;
    }

    public List<Evento> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("from Evento", Evento.class).getResultList();
        }
    }

    public List<Evento> findAllActive() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("from Evento e where e.estado = :est", Evento.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getResultList();
        }
    }

    public List<Evento> findAllActiveAndPublic() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("from Evento e where e.estado = :est and e.publicado = :pub", Evento.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .setParameter("pub", true)
                    .getResultList();
        }
    }

    public Evento findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Evento.class, id);
        } catch (Exception ex) {
            return null;
        }
    }

    public void crear(Evento evento) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(evento);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void actualizar(Evento evento) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(evento);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public boolean eliminar(long id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Evento evento = em.find(Evento.class, id);
            if (evento != null) {
                evento.setEstado(EstadoObjeto.INACTIVO);
                em.merge(evento);
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
}