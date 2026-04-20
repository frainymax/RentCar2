package com.mycompany.rentcar.modelo;

public class Reserva {

    private int    idReserva;   // ← NUEVO campo autogenerado
    private String matricula;
    private String cedula;
    private String oferta;
    private String fechaReserva;
    private String fechaSalida;
    private String fechaEntrada;
    private String observacion;
    private int    dias;
    private double total;

    // Constructor COMPLETO con idReserva (usado al guardar y al leer del archivo)
    public Reserva(int idReserva, String matricula, String cedula, String oferta,
                   String fechaReserva, String fechaSalida, String fechaEntrada,
                   String observacion, int dias, double total) {
        this.idReserva   = idReserva;
        this.matricula   = matricula;
        this.cedula      = cedula;
        this.oferta      = oferta;
        this.fechaReserva  = fechaReserva;
        this.fechaSalida   = fechaSalida;
        this.fechaEntrada  = fechaEntrada;
        this.observacion   = observacion;
        this.dias  = dias;
        this.total = total;
    }

    // Getters
    public int    getIdReserva()    { return idReserva; }
    public String getMatricula()    { return matricula; }
    public String getCedula()       { return cedula; }
    public String getOferta()       { return oferta; }
    public String getFechaReserva() { return fechaReserva; }
    public String getFechaSalida()  { return fechaSalida; }
    public String getFechaEntrada() { return fechaEntrada; }
    public String getObservacion()  { return observacion; }
    public int    getDias()         { return dias; }
    public double getTotal()        { return total; }

    // toString guarda idReserva como primera columna
    @Override
    public String toString() {
        return idReserva    + ";" +
               matricula    + ";" +
               cedula       + ";" +
               oferta       + ";" +
               fechaReserva + ";" +
               fechaSalida  + ";" +
               fechaEntrada + ";" +
               observacion  + ";" +
               dias         + ";" +
               total;
    }
}
