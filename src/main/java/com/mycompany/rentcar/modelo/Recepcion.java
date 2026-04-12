package com.mycompany.rentcar.modelo;

public class Recepcion {

    private String id;
    private String matricula;
    private String fechaEntrada;
    private String fechaRecepcion;
    private String observacion;

    public Recepcion(String id, String matricula,
                     String fechaEntrada, String fechaRecepcion,
                     String observacion) {

        this.id = id;
        this.matricula = matricula;
        this.fechaEntrada = fechaEntrada;
        this.fechaRecepcion = fechaRecepcion;
        this.observacion = observacion;
    }

    public String getId() { return id; }
    public String getMatricula() { return matricula; }
    public String getFechaEntrada() { return fechaEntrada; }
    public String getFechaRecepcion() { return fechaRecepcion; }
    public String getObservacion() { return observacion; }

    @Override
    public String toString() {
        return id + ";" + matricula + ";" +
               fechaEntrada + ";" + fechaRecepcion + ";" +
               observacion;
    }
}