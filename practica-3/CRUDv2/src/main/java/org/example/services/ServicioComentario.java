package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.Comentario;
import org.example.models.EstadoObjeto;
import org.example.models.Producto;
import java.util.List;

public class ServicioComentario {

    public void crear(String contenido, String usuario, int productoId) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Producto producto = em.find(Producto.class, productoId);
            if (producto != null) {
                Comentario comentario = new Comentario(contenido, usuario, producto);
                em.persist(comentario);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public List<Comentario> listarPorProducto(int productoId) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Comentario c WHERE c.producto.id = :id AND c.estado = :est ORDER BY c.fechaPublicacion DESC", Comentario.class)
                    .setParameter("id", productoId)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Comentario comentario = em.find(Comentario.class, id);
            if (comentario != null) {
                comentario.setEstado(EstadoObjeto.INACTIVO);
                em.merge(comentario);
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