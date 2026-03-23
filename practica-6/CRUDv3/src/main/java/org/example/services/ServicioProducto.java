package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.EstadoObjeto;
import org.example.models.Producto;

import java.math.BigDecimal;
import java.util.List;

public class ServicioProducto {

    private static ServicioProducto instancia;

    public static ServicioProducto getInstancia() {
        if (instancia == null) {
            instancia = new ServicioProducto();
        }
        return instancia;
    }
    private ServicioProducto() {
    }

    public Producto crear(String nombre, BigDecimal precio, int cantidad, String descripcion, List<String> imagenes) {
        Producto producto = new Producto(nombre, precio, cantidad, descripcion);
        if (imagenes != null) {
            producto.setImagenes(imagenes);
        }

        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(producto);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return producto;
    }

    public Producto buscarPorId(int id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.find(Producto.class, id);
        } finally {
            em.close();
        }
    }

    public Producto buscarActivoPorId(int id) {
        Producto producto = buscarPorId(id);
        if (producto != null && producto.getEstado() == EstadoObjeto.ACTIVO) {
            return producto;
        }
        return null;
    }

    public boolean modificarPorId(int id, String nuevoNombre, BigDecimal nuevoPrecio, int nuevaCantidad, String nuevaDescripcion, List<String> nuevasImagenes) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Producto producto = em.find(Producto.class, id);

            if (producto != null && producto.getEstado() == EstadoObjeto.ACTIVO) {
                producto.setNombre(nuevoNombre);
                producto.setPrecio(nuevoPrecio);
                producto.setCantidad(nuevaCantidad);
                producto.setDescripcion(nuevaDescripcion);

                if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
                    producto.getImagenes().clear();
                    producto.getImagenes().addAll(nuevasImagenes);
                }

                em.merge(producto);
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
            Producto producto = em.find(Producto.class, id);

            if (producto != null) {
                producto.setEstado(EstadoObjeto.INACTIVO);
                em.merge(producto);
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

    public List<Producto> listarTodos() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {

            return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Producto> listarActivos() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Producto p WHERE p.estado = :est", Producto.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    public List<Producto> listarPaginados(int pagina, int tamano) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {

            int offset = (pagina - 1) * tamano;

            return em.createQuery("SELECT p FROM Producto p WHERE p.estado = :est", Producto.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .setFirstResult(offset)
                    .setMaxResults(tamano)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long contarActivos() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Producto p WHERE p.estado = :est", Long.class)
                    .setParameter("est", EstadoObjeto.ACTIVO)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

}