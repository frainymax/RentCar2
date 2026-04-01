/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Usuario;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {
    
     private final String RUTA = "archivos/usuarios.txt";

    public void guardarUsuario(Usuario nuevo) throws IOException {

    File archivo = new File(RUTA);
    List<String> lineas = new ArrayList<>();
    boolean actualizado = false;

    // SI el archivo no existe, crearlo con encabezado
    if (!archivo.exists()) {
        archivo.getParentFile().mkdirs();
        archivo.createNewFile();
    }

    BufferedReader br = new BufferedReader(new FileReader(archivo));
    String linea;

    // LEER encabezado
    String encabezado = br.readLine();
    if (encabezado == null) {
        encabezado = "login,pass,nivel,nombre,apellido,email";
    }

    while ((linea = br.readLine()) != null) {

        String[] datos = linea.split(",", -1);

        if (datos[0].equals(nuevo.getLogin())) {
            lineas.add(nuevo.toString());
            actualizado = true;
        } else {
            lineas.add(linea);
        }
    }
    br.close();

    if (!actualizado) {
        lineas.add(nuevo.toString());
    }

    BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, false));

    //  ESCRIBIR ENCABEZADO SIEMPRE
    bw.write(encabezado);
    bw.newLine();

    for (String l : lineas) {
        bw.write(l);
        bw.newLine();
    }

    bw.close();
}

   public Usuario buscarUsuario(String login, String password) throws IOException {

    BufferedReader br = new BufferedReader(new FileReader(RUTA));
    String linea;

    // Saltar encabezado
    br.readLine();

    while ((linea = br.readLine()) != null) {

        String[] datos = linea.split(",", -1);

        String log = datos[0].trim();
        String pass = datos[1].trim();

        if (log.equals(login) && pass.equals(password)) {

            int nivel = Integer.parseInt(datos[2].trim());
            String nombre = datos[3].trim();
            String apellido = datos[4].trim();
            String email = datos[5].trim();

            br.close();

            return new Usuario(log, pass, nivel, nombre, apellido, email);
        }
    }

    br.close();
    return null;
}
   public Usuario buscarPorLogin(String login) throws IOException {

    BufferedReader br = new BufferedReader(new FileReader(RUTA));
    String linea;

    while ((linea = br.readLine()) != null) {
        String[] d = linea.split(",", -1);

        if (d[0].equals(login)) {
            br.close();
            return new Usuario(
                    d[0], d[1],
                    Integer.parseInt(d[2]),
                    d[3], d[4], d[5]
            );
        }
    }

    br.close();
    return null;
}

    public List<Usuario> obtenerUsuarios() throws IOException {

    List<Usuario> lista = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader(RUTA));
    String linea;

    br.readLine(); // saltar encabezado

    while ((linea = br.readLine()) != null) {

    String[] datos = linea.split(",", -1);

    if (datos.length < 6) {
        continue; // ignora líneas dañadas
    }

    lista.add(new Usuario(
            datos[0].trim(),
            datos[1].trim(),
            Integer.parseInt(datos[2].trim()),
            datos[3].trim(),
            datos[4].trim(),
            datos[5].trim()
    ));
}
    
    

    br.close();
    return lista;
    
}
    public void eliminarUsuario(String login) throws IOException {
    List<Usuario> lista = obtenerUsuarios();
    lista.removeIf(u -> u.getLogin().equals(login));
    reescribirArchivo(lista);
}
    
   public void reescribirArchivo(List<Usuario> lista) throws IOException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));

    // 🔥 escribir encabezado SIEMPRE
    bw.write("login,pass,nivel,nombre,apellido,email");
    bw.newLine();

    for (Usuario u : lista) {
        bw.write(u.toString());
        bw.newLine();
    }

    bw.close();
}
   
   
    
}
