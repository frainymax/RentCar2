package com.mycompany.rentcar.modelo;

public class Gama {

    private String id;
    private String descripcion;
    private double precio;

    public Gama(String id, String descripcion, double precio) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }

    @Override
    public String toString() {
        return id + ";" + descripcion + ";" + precio;
    }
}