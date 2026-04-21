package com.mycompany.rentcar.modelo;

public class Recepcion {

    private String id;
    private String idReserva;      // ← NUEVO
    private String matricula;
    private String fechaEntrada;
    private String fechaRecepcion;
    private String observacion;

    // Constructor completo con idReserva
    public Recepcion(String id, String idReserva, String matricula,
                     String fechaEntrada, String fechaRecepcion,
                     String observacion) {
        this.id             = id;
        this.idReserva      = idReserva;
        this.matricula      = matricula;
        this.fechaEntrada   = fechaEntrada;
        this.fechaRecepcion = fechaRecepcion;
        this.observacion    = observacion;
    }

    public String getId()             { return id; }
    public String getIdReserva()      { return idReserva; }   // ← NUEVO getter
    public String getMatricula()      { return matricula; }
    public String getFechaEntrada()   { return fechaEntrada; }
    public String getFechaRecepcion() { return fechaRecepcion; }
    public String getObservacion()    { return observacion; }

    @Override
    public String toString() {
        return id             + ";" +
               idReserva      + ";" +
               matricula      + ";" +
               fechaEntrada   + ";" +
               fechaRecepcion + ";" +
               observacion;
    }
}
