package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.*;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioInscripcion {

    private static ServicioInscripcion instancia;

    public static ServicioInscripcion getInstancia() {
        if (instancia == null) instancia = new ServicioInscripcion();
        return instancia;
    }

    public enum ResultadoInscripcion {
        EXITO, SIN_CUPO, DUPLICADA, ERROR
    }

    public ResultadoInscripcion inscribir(Evento evento, Usuario usuario) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM Inscripcion i WHERE i.evento.id = :eid AND i.usuario.id = :uid AND i.estado = :est",
                            Long.class)
                    .setParameter("eid", evento.getId())
                    .setParameter("uid", usuario.getId())
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getSingleResult();

            if (count > 0) return ResultadoInscripcion.DUPLICADA;

            Long inscritos = em.createQuery(
                            "SELECT COUNT(i) FROM Inscripcion i WHERE i.evento.id = :eid AND i.estado = :est",
                            Long.class)
                    .setParameter("eid", evento.getId())
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getSingleResult();

            if (inscritos >= evento.getCupoMaximo()) return ResultadoInscripcion.SIN_CUPO;

            em.getTransaction().begin();
            Inscripcion inscripcion = new Inscripcion(evento, usuario);
            inscripcion.setEstado(EstadoObjeto.ACTIVO);
            em.persist(inscripcion);
            em.getTransaction().commit();

            return ResultadoInscripcion.EXITO;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return ResultadoInscripcion.ERROR;
        } finally {
            em.close();
        }
    }

    public boolean cancelar(Long inscripcionId, Usuario usuario) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Inscripcion i = em.find(Inscripcion.class, inscripcionId);

            if (i == null || i.getUsuario().getId() != usuario.getId()) return false;
            if (i.getEvento().getFechaHora().isBefore(LocalDateTime.now())) return false;

            i.setEstado(EstadoObjeto.INACTIVO);
            em.merge(i);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean marcarAsistencia(String tokenQr) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            Inscripcion i = em.createQuery(
                            "SELECT i FROM Inscripcion i WHERE i.tokenQr = :token AND i.estado = :est",
                            Inscripcion.class)
                    .setParameter("token", tokenQr)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getSingleResult();

            if (i.isAsistio()) return false;

            em.getTransaction().begin();
            i.setAsistio(true);
            i.setFechaAsistencia(LocalDateTime.now());
            em.merge(i);
            em.getTransaction().commit();
            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Inscripcion> listarPorUsuario(int usuarioId) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i JOIN FETCH i.evento WHERE i.usuario.id = :uid AND i.estado = :est",
                            Inscripcion.class)
                    .setParameter("uid", usuarioId)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Inscripcion> listarPorEvento(Long eventoId) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i JOIN FETCH i.usuario WHERE i.evento.id = :eid AND i.estado = :est",
                            Inscripcion.class)
                    .setParameter("eid", eventoId)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Inscripcion buscarPorToken(String token) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Inscripcion i JOIN FETCH i.evento JOIN FETCH i.usuario WHERE i.tokenQr = :token",
                            Inscripcion.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}