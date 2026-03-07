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

    public List<Evento> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("from Evento", Evento.class).getResultList();
        }
    }

    public Evento findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Evento.class, id);
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

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Evento e = em.find(Evento.class, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}