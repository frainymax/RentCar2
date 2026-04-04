package com.mycompany.rentcar.modelo;

public class Vehiculo {

    private String matricula;
    private String marca;
    private String modelo;
    private int tipoVehiculo;
    private int tipoMotor;
    private String gama;
    private String descripcionGama;
    private boolean techo;
    private boolean aire;
    private boolean cuero;
    private String color;
    private boolean automatico;
    private boolean status;

    public Vehiculo(String matricula, String marca, String modelo,
                    int tipoVehiculo, int tipoMotor, String gama,
                    String descripcionGama,
                    boolean techo, boolean aire, boolean cuero,
                    String color, boolean automatico, boolean status) {

        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.tipoVehiculo = tipoVehiculo;
        this.tipoMotor = tipoMotor;
        this.gama = gama;
        this.descripcionGama = descripcionGama;
        this.techo = techo;
        this.aire = aire;
        this.cuero = cuero;
        this.color = color;
        this.automatico = automatico;
        this.status = status;
    }

    // ===== GETTERS NECESARIOS =====
    public String getMatricula() { return matricula; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getTipoVehiculo() { return tipoVehiculo; }
    public int getTipoMotor() { return tipoMotor; }
    public String getGama() { return gama; }
    public String getDescripcionGama() { return descripcionGama; }
    public boolean isTecho() { return techo; }
    public boolean isAire() { return aire; }
    public boolean isCuero() { return cuero; }
    public String getColor() { return color; }
    public boolean isAutomatico() { return automatico; }
    public boolean isStatus() { return status; }

    @Override
    public String toString() {
        return matricula + "," + marca + "," + modelo + "," +
                tipoVehiculo + "," + tipoMotor + "," + gama + "," +
                descripcionGama + "," +
                techo + "," + aire + "," + cuero + "," +
                color + "," + automatico + "," + status;
    }
}