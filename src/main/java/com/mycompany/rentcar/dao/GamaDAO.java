package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Gama;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GamaDAO {

    private final String RUTA = "archivos/gamas.txt";

    // ===== ASEGURA QUE EL ARCHIVO EXISTA =====
    private void asegurarArchivo() throws IOException {
        File archivo = new File(RUTA);

        if (!archivo.exists()) {
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            bw.write("id;descripcion;precio"); // 🔴 CAMBIADO
            bw.newLine();
            bw.close();
        }
    }

    // ===== BUSCAR =====
    public Gama buscarPorId(String id) throws IOException {
        asegurarArchivo();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine(); // encabezado

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(";", -1); // 🔴 CAMBIADO

            if (d[0].equals(id)) {
                br.close();
                return new Gama(
                        d[0],
                        d[1],
                        Double.parseDouble(d[2])
                );
            }
        }

        br.close();
        return null;
    }

    // ===== GUARDAR / MODIFICAR =====
    public void guardarGama(Gama nueva, String idOriginal) throws IOException {

        asegurarArchivo();

        List<String> lineas = new ArrayList<>();
        boolean actualizado = false;

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String encabezado = br.readLine();

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(";", -1); // 🔴 CAMBIADO

            if (d[0].equals(idOriginal)) {
                lineas.add(nueva.toString());
                actualizado = true;
            } else {
                lineas.add(linea);
            }
        }
        br.close();

        if (!actualizado) {
            lineas.add(nueva.toString());
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write(encabezado);
        bw.newLine();

        for (String l : lineas) {
            bw.write(l);
            bw.newLine();
        }

        bw.close();
    }

    // ===== ELIMINAR =====
    public void eliminarGama(String id) throws IOException {

        asegurarArchivo();

        List<String> lineas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String encabezado = br.readLine();

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(";", -1); // 🔴 CAMBIADO

            if (!d[0].equals(id)) {
                lineas.add(linea);
            }
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write(encabezado);
        bw.newLine();

        for (String l : lineas) {
            bw.write(l);
            bw.newLine();
        }

        bw.close();
    }

    // ===== LISTAR =====
    public List<Gama> obtenerGamas() throws IOException {
        asegurarArchivo();

        List<Gama> lista = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(";", -1); // 🔴 CAMBIADO

            lista.add(new Gama(
                    d[0],
                    d[1],
                    Double.parseDouble(d[2])
            ));
        }

        br.close();
        return lista;
    }
}