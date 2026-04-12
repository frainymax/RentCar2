package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Recepcion;

import java.io.*;
import java.util.*;

public class RecepcionDAO {

    private final String RUTA = "archivos/recepciones.txt";

    private void asegurar() throws IOException {
        File f = new File(RUTA);

        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("id;matricula;fechaEntrada;fechaRecepcion;observacion");
            bw.newLine();
            bw.close();
        }
    }
    
    public void eliminar(String id) throws IOException {
    asegurar();

    List<String> lineas = new ArrayList<>();

    BufferedReader br = new BufferedReader(new FileReader(RUTA));
    String encabezado = br.readLine();

    String l;
    while ((l = br.readLine()) != null) {

        String[] d = l.split(";", -1);

        // 🔥 si NO es el que queremos eliminar, lo dejamos
        if (!d[0].equals(id)) {
            lineas.add(l);
        }
    }
    br.close();

    BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
    bw.write(encabezado);
    bw.newLine();

    for (String s : lineas) {
        bw.write(s);
        bw.newLine();
    }

    bw.close();
}

    public Recepcion buscar(String id) throws IOException {
        asegurar();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(";", -1);

            if (d[0].equals(id)) {
                br.close();
                return new Recepcion(d[0], d[1], d[2], d[3], d[4]);
            }
        }

        br.close();
        return null;
    }

    public void guardar(Recepcion r, String original) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();

        boolean actualizado = false;

        String l;
        while ((l = br.readLine()) != null) {
            if (l.split(";", -1)[0].equals(original)) {
                lineas.add(r.toString());
                actualizado = true;
            } else {
                lineas.add(l);
            }
        }
        br.close();

        if (!actualizado) {
            lineas.add(r.toString());
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

    public List<Recepcion> listar() throws IOException {
        asegurar();

        List<Recepcion> lista = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(";", -1);

            lista.add(new Recepcion(d[0], d[1], d[2], d[3], d[4]));
        }

        br.close();
        return lista;
    }
}