package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Oferta;

import java.io.*;
import java.util.*;

public class OfertaDAO {

    private final String RUTA = "archivos/ofertas.txt";

    // ===== ASEGURAR ARCHIVO =====
    private void asegurar() throws IOException {
        File f = new File(RUTA);

        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("id,matricula,descripcion,precio");
            bw.newLine();
            bw.close();
        }
    }

    // ===== BUSCAR =====
    public Oferta buscar(String id) throws IOException {
        asegurar();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine(); // encabezado

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(",", -1);

            if (d[0].equals(id)) {
                br.close();
                return new Oferta(
                        d[0],
                        d[1],
                        d[2],
                        Double.parseDouble(d[3])
                );
            }
        }

        br.close();
        return null;
    }

    // ===== GUARDAR / MODIFICAR =====
    public void guardar(Oferta o, String original) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();

        boolean actualizado = false;

        String l;
        while ((l = br.readLine()) != null) {
            if (l.split(",")[0].equals(original)) {
                lineas.add(o.toString());
                actualizado = true;
            } else {
                lineas.add(l);
            }
        }
        br.close();

        if (!actualizado) {
            lineas.add(o.toString());
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
    public void eliminar(String id) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            if (!l.split(",")[0].equals(id)) {
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
    public List<Oferta> listar() throws IOException {
        asegurar();

        List<Oferta> lista = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(",", -1);

            lista.add(new Oferta(
                    d[0],
                    d[1],
                    d[2],
                    Double.parseDouble(d[3])
            ));
        }

        br.close();
        return lista;
    }
}