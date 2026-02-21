package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.Carrito;
import org.example.models.Producto;
import org.example.models.Usuario;
import org.example.Main;

import java.util.ArrayList;
import java.util.List;

public class ServicioCarrito {

    public ServicioCarrito() {
    }

    public Carrito crear(Usuario usuario) {
        Carrito carrito = new Carrito(usuario);
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(carrito);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return carrito;
    }

    public Carrito buscarPorId(long id) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            return em.find(Carrito.class, (int)id);
        } finally {
            em.close();
        }
    }

    public boolean agregarProducto(long idCarrito, Producto producto) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrito carrito = em.find(Carrito.class, (int)idCarrito);
            if (carrito != null) {
                Producto productoDb = em.find(Producto.class, producto.getId());
                carrito.getListaProducto().add(productoDb);
                em.merge(carrito);
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

    public boolean eliminarProducto(long idCarrito, long idProducto) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrito carrito = em.find(Carrito.class, (int)idCarrito);
            if (carrito != null) {
                carrito.getListaProducto().removeIf(p -> p.getId() == idProducto);
                em.merge(carrito);
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

    public List<Producto> listarProductos(long idCarrito) {
        Carrito carrito = buscarPorId(idCarrito);
        return (carrito != null) ? carrito.getListaProducto() : new ArrayList<>();
    }

    public boolean vaciarCarrito(long idCarrito) {
        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrito carrito = em.find(Carrito.class, (int)idCarrito);
            if (carrito != null) {
                carrito.getListaProducto().clear();
                em.merge(carrito);
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

    /**
     * Pasa los productos del carrito temporal (sesión sin login) al carrito del usuario
     */
    public boolean mergeCarritoLogin(Usuario usuario) {
        Usuario anonimo = Main.servicioUsuario.buscarPorUsername("");
        if (anonimo == null) return false;

        Carrito carritoAnonimo = buscarPorId(anonimo.getId());
        if (carritoAnonimo == null || carritoAnonimo.getListaProducto().isEmpty()) return true;

        Carrito carritoUsuario = buscarPorId(usuario.getId());
        if (carritoUsuario == null) {
            carritoUsuario = crear(usuario);
        }

        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrito cUser = em.find(Carrito.class, carritoUsuario.getId());
            Carrito cAnon = em.find(Carrito.class, carritoAnonimo.getId());

            cUser.getListaProducto().addAll(cAnon.getListaProducto());
            cAnon.getListaProducto().clear();

            em.merge(cUser);
            em.merge(cAnon);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }
}