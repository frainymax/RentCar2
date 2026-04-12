package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Cliente;

import java.io.*;
import java.util.*;

public class ClienteDAO {

    private final String RUTA = "archivos/clientes.txt";

    // ===== CREAR ARCHIVO SI NO EXISTE =====
    private void asegurar() throws IOException {
        File f = new File(RUTA);

        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("cedula;nombre;apellido;direccion;email;telefono"); // 👈 CAMBIADO
            bw.newLine();
            bw.close();
        }
    }

    // ===== BUSCAR =====
    public Cliente buscar(String cedula) throws IOException {
        asegurar();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(";", -1); // 👈 CAMBIADO

            if (d[0].equals(cedula)) {
                br.close();
                return new Cliente(
                        d[0], d[1], d[2],
                        d[3], d[4], d[5]
                );
            }
        }

        br.close();
        return null;
    }

    // ===== GUARDAR / MODIFICAR =====
    public void guardar(Cliente c, String original) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();
        boolean actualizado = false;

        String l;
        while ((l = br.readLine()) != null) {
            if (l.split(";")[0].equals(original)) { // 👈 CAMBIADO
                lineas.add(c.toString());
                actualizado = true;
            } else {
                lineas.add(l);
            }
        }
        br.close();

        if (!actualizado) {
            lineas.add(c.toString());
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write(enc);
        bw.newLine();

        for (String s : lineas) {
            bw.write(s);
            bw.newLine();
        }

        bw.close();
    }

    // ===== ELIMINAR =====
    public void eliminar(String cedula) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            if (!l.split(";")[0].equals(cedula)) {  
                lineas.add(l);
            }
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write(enc);
        bw.newLine();

        for (String s : lineas) {
            bw.write(s);
            bw.newLine();
        }

        bw.close();
    }

    // ===== LISTAR =====
    public List<Cliente> listar() throws IOException {
        asegurar();

        List<Cliente> lista = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(";", -1);  

            lista.add(new Cliente(
                    d[0], d[1], d[2],
                    d[3], d[4], d[5]
            ));
        }

        br.close();
        return lista;
    }
}
