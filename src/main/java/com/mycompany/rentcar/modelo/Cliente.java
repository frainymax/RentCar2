package com.mycompany.rentcar.modelo;

public class Cliente {

    private String cedula;
    private String nombre;
    private String apellido;
    private String direccion;
    private String email;
    private String telefono;

    public Cliente(String cedula, String nombre, String apellido,
                   String direccion, String email, String telefono) {

        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDireccion() { return direccion; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    @Override
    public String toString() {
        return cedula + "," + nombre + "," + apellido + "," +
               direccion + "," + email + "," + telefono;
    }
}