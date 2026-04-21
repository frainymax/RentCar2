package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Recepcion;

import java.io.*;
import java.util.*;

public class RecepcionDAO {

    private final String RUTA = "archivos/recepciones.txt";

    // Formato del archivo:
    // id;idReserva;matricula;fechaEntrada;fechaRecepcion;observacion

    private void asegurar() throws IOException {
        File f = new File(RUTA);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("id;idReserva;matricula;fechaEntrada;fechaRecepcion;observacion");
            bw.newLine();
            bw.close();
        }
    }

    public List<Recepcion> listar() throws IOException {
        asegurar();
        List<Recepcion> lista = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine(); // saltar encabezado
        String l;
        while ((l = br.readLine()) != null) {
            if (l.trim().isEmpty()) continue;
            String[] d = l.split(";", -1);
            if (d.length < 6) continue;
            lista.add(new Recepcion(
                d[0].trim(), // id
                d[1].trim(), // idReserva
                d[2].trim(), // matricula
                d[3].trim(), // fechaEntrada
                d[4].trim(), // fechaRecepcion
                d[5].trim()  // observacion
            ));
        }
        br.close();
        return lista;
    }

    public Recepcion buscar(String id) throws IOException {
        for (Recepcion r : listar()) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    public Recepcion buscarPorMatricula(String matricula) throws IOException {
        for (Recepcion r : listar()) {
            if (r.getMatricula().equalsIgnoreCase(matricula)) return r;
        }
        return null;
    }

    public void guardar(Recepcion r, String original) throws IOException {
        asegurar();
        List<Recepcion> lista = listar();

        boolean actualizado = false;
        List<Recepcion> resultado = new ArrayList<>();

        for (Recepcion rec : lista) {
            if (rec.getId().equals(original)) {
                resultado.add(r);     // reemplazar con el nuevo
                actualizado = true;
            } else {
                resultado.add(rec);
            }
        }

        if (!actualizado) {
            resultado.add(r);         // es nuevo, agregar al final
        }

        // Reescribir archivo completo
        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write("id;idReserva;matricula;fechaEntrada;fechaRecepcion;observacion");
        bw.newLine();
        for (Recepcion rec : resultado) {
            bw.write(rec.toString());
            bw.newLine();
        }
        bw.close();
    }

    public void eliminar(String id) throws IOException {
        List<Recepcion> lista = listar();
        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write("id;idReserva;matricula;fechaEntrada;fechaRecepcion;observacion");
        bw.newLine();
        for (Recepcion r : lista) {
            if (!r.getId().equals(id)) {
                bw.write(r.toString());
                bw.newLine();
            }
        }
        bw.close();
    }
}
