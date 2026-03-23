package org.example.services;

import jakarta.persistence.EntityManager;
import org.example.models.Carrito;
import org.example.models.Producto;
import org.example.models.Usuario;
import org.example.Main;

import java.util.ArrayList;
import java.util.List;

public class ServicioCarrito {

    private static ServicioCarrito instancia;

    public static ServicioCarrito getInstancia() {
        if (instancia == null) {
            instancia = new ServicioCarrito();
        }
        return instancia;
    }
    private ServicioCarrito() {
    }

    public Carrito crear(Usuario usuario) {
        EntityManager em = BootStrapServices.getEntityManager();
        Carrito carrito = null;
        try {
            em.getTransaction().begin();
            Usuario usuarioManaged = em.find(Usuario.class, usuario.getId());

            if (usuarioManaged != null) {
                carrito = new Carrito();
                carrito.setUsuario(usuarioManaged);

                em.persist(carrito);
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
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

                if (carrito.getListaProducto() == null) {
                    carrito.setListaProducto(new ArrayList<>());
                }

                carrito.getListaProducto().add(productoDb);
                em.merge(carrito);
                em.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
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


    public boolean mergeCarritoLogin(Usuario usuario, Carrito carritoMerge) {
        if (carritoMerge == null) {
            return true;
        }

        if (carritoMerge.getListaProducto() == null || carritoMerge.getListaProducto().isEmpty()) {
            return true;
        }

        EntityManager em = BootStrapServices.getEntityManager();
        try {
            em.getTransaction().begin();

            Carrito carritoUsuario = em.find(Carrito.class, usuario.getId());
            if (carritoUsuario == null) {
                carritoUsuario = new Carrito(usuario);
                em.persist(carritoUsuario);
            }

            carritoUsuario.getListaProducto().addAll(carritoMerge.getListaProducto());
            carritoMerge.getListaProducto().clear();

            em.merge(carritoUsuario);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}