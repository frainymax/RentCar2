package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Gama;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GamaDAO {

    private final String RUTA = "archivos/gamas.txt";

    public Gama buscarPorId(int id) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine(); // encabezado

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(",", -1);
            if (Integer.parseInt(d[0]) == id) {
                br.close();
                return new Gama(
                        Integer.parseInt(d[0]),
                        d[1],
                        Double.parseDouble(d[2])
                );
            }
        }
        br.close();
        return null;
    }

    public void guardar(Gama nueva, int idOriginal) throws IOException {

        File archivo = new File(RUTA);
        List<String> lineas = new ArrayList<>();
        boolean actualizado = false;

        if (!archivo.exists()) {
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();
        }

        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String encabezado = br.readLine();
        if (encabezado == null) {
            encabezado = "id,descripcion,precio";
        }

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(",", -1);

            if (Integer.parseInt(d[0]) == idOriginal) {
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

        BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, false));
        bw.write(encabezado);
        bw.newLine();

        for (String l : lineas) {
            bw.write(l);
            bw.newLine();
        }
        bw.close();
    }

    public List<Gama> obtenerTodas() throws IOException {
        List<Gama> lista = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String linea;
        while ((linea = br.readLine()) != null) {
            String[] d = linea.split(",", -1);
            lista.add(new Gama(
                    Integer.parseInt(d[0]),
                    d[1],
                    Double.parseDouble(d[2])
            ));
        }
        br.close();
        return lista;
    }
}