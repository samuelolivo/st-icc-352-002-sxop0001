package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.Producto;
import org.example.models.Venta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServicioVenta {

    private static ServicioVenta instancia;

    public static ServicioVenta getInstancia() {
        if (instancia == null) {
            instancia = new ServicioVenta();
        }
        return instancia;
    }

    private ServicioVenta() {
    }

    public Venta registrar(String userCliente, List<Producto> productos) {
        EntityManager em = BootStrapServices.getEntityManager();
        Venta venta = new Venta(new Date(), userCliente);

        try {
            em.getTransaction().begin();

            List<Producto> productosManaged = new ArrayList<>();
            for (Producto p : productos) {
                Producto pDb = em.find(Producto.class, p.getId());
                if (pDb != null) {
                    if (!productosManaged.contains(pDb)) {
                        Producto pNuevo = new Producto(pDb.getNombre(), pDb.getPrecio(), 1, pDb.getDescripcion());
                        pNuevo.setId(pDb.getId());
                        productosManaged.add(pNuevo);
                    }
                    else {
                        for (Producto pm : productosManaged)
                        {
                            if (pm.getId() == pDb.getId())
                            {
                                pm.setCantidad(pm.getCantidad() + 1);
                                break;
                            }
                        }
                    }
                }
            }

            venta.setListaProducto(productosManaged);

            em.persist(venta);
            em.getTransaction().commit();
            return venta;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public Venta buscarPorId(long id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.find(Venta.class, id);
        } finally {
            em.close();
        }
    }

    public List<Venta> listarTodas() {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Venta v ORDER BY v.fecha DESC", Venta.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Venta> listarPorCliente(String userCliente) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Venta v WHERE v.userCliente = :user ORDER BY v.fecha DESC", Venta.class)
                    .setParameter("user", userCliente)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}