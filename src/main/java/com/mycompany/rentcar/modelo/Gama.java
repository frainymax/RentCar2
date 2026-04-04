package com.mycompany.rentcar.modelo;

public class Gama {

    private int idGama;
    private String descripcion;
    private double precio;

    public Gama(int idGama, String descripcion, double precio) {
        this.idGama = idGama;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public int getIdGama() { return idGama; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }

    @Override
    public String toString() {
        return idGama + "," + descripcion + "," + precio;
    }
}