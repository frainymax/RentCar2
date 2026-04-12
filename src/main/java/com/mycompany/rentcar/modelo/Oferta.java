package com.mycompany.rentcar.modelo;

public class Oferta {

    private String id;
    private String matricula;
    private String descripcion;
    private double precio;

    public Oferta(String id, String matricula, String descripcion, double precio) {
        this.id = id;
        this.matricula = matricula;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }

    @Override
    public String toString() {
        return id + ";" + matricula + ";" + descripcion + ";" + precio;
    }
}