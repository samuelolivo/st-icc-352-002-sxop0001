package org.example.services;

import org.example.models.Producto;
import org.example.models.Venta;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServicioVenta {

    private ArrayList<Venta> listaVentas;

    public ServicioVenta() {
        listaVentas = new ArrayList<>();
    }

    public Venta registrar(String userCliente, ArrayList<Producto> productos) {
        Venta venta = new Venta(new Date(), userCliente); //

        Map<Integer, Integer> conteo = new HashMap<>();
        Map<Integer, Producto> prototipos = new HashMap<>();

        for (Producto p : productos) {
            int id = p.getId();
            conteo.put(id, conteo.getOrDefault(id, 0) + 1);
            prototipos.put(id, p);
        }

        ArrayList<Producto> listaFinal = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : conteo.entrySet()) {
            Producto original = prototipos.get(entry.getKey());

            Producto pVenta = new Producto(original.getNombre(), original.getPrecio(), entry.getValue());
            pVenta.setId(original.getId());

            listaFinal.add(pVenta);
        }

        venta.setListaProducto(listaFinal);
        listaVentas.add(venta);

        return venta;
    }


    public Venta buscarPorId(long id) {
        for (Venta venta : listaVentas) {
            if (venta.getId() == id) {
                return venta;
            }
        }

        return null;
    }

    public ArrayList<Venta> listarTodas() {
        return new ArrayList<>(listaVentas);
    }

    public ArrayList<Venta> listarPorCliente(String userCliente) {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta venta : listaVentas) {
            if (venta.getUserCliente().equals(userCliente)) {
                resultado.add(venta);
            }
        }

        return resultado;
    }
}