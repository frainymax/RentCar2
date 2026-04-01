package com.mycompany.rentcar.modelo;

public class Usuario {

    private String login;
    private String pass;
    private int nivel;
    private String nombre;
    private String apellido;
    private String email;

    public Usuario(String login, String pass, int nivel,
                   String nombre, String apellido, String email) {
        this.login = login;
        this.pass = pass;
        this.nivel = nivel;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        
        
    }

    public int getNivel() {
        return nivel;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return login + "," + pass + "," + nivel + "," +
               nombre + "," + apellido + "," + email;
    }
    
    public String getLogin() {
    return login;
}
    public String getPass() { return pass; }
public String getApellido() { return apellido; }
public String getEmail() { return email; }
}