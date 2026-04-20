package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Reserva;

import java.io.*;
import java.util.*;

public class ReservaDAO {

    private final String RUTA = "archivos/reservas.txt";

    // Crea el archivo con encabezado si no existe
    private void asegurar() throws IOException {
        File f = new File(RUTA);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("idReserva;matricula;cedula;oferta;fechaReserva;fechaSalida;fechaEntrada;observacion;dias;total");
            bw.newLine();
            bw.close();
        }
    }

    // Guarda una nueva reserva al final del archivo
    public void guardar(Reserva r) throws IOException {
        asegurar();
        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, true));
        bw.write(r.toString()); // usa el toString() que incluye idReserva
        bw.newLine();
        bw.close();
    }

    // Lee todas las reservas del archivo
    public List<Reserva> listar() throws IOException {
        asegurar();
        List<Reserva> lista = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine(); // saltar encabezado
        String l;
        while ((l = br.readLine()) != null) {
            if (l.trim().isEmpty()) continue;
            String[] d = l.split(";", -1);
            if (d.length < 10) continue;
            lista.add(new Reserva(
                Integer.parseInt(d[0].trim()),  // idReserva
                d[1].trim(),                    // matricula
                d[2].trim(),                    // cedula
                d[3].trim(),                    // oferta
                d[4].trim(),                    // fechaReserva
                d[5].trim(),                    // fechaSalida
                d[6].trim(),                    // fechaEntrada
                d[7].trim(),                    // observacion
                Integer.parseInt(d[8].trim()),  // dias
                Double.parseDouble(d[9].trim()) // total
            ));
        }
        br.close();
        return lista;
    }

    // Elimina por idReserva (int) — el admin es el único que puede borrar
    public void eliminar(int idReserva) throws IOException {
        asegurar();
        List<Reserva> lista = listar();

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, false));
        bw.write("idReserva;matricula;cedula;oferta;fechaReserva;fechaSalida;fechaEntrada;observacion;dias;total");
        bw.newLine();
        for (Reserva r : lista) {
            if (r.getIdReserva() != idReserva) {
                bw.write(r.toString());
                bw.newLine();
            }
        }
        bw.close();
    }

    // Busca una reserva por matrícula — útil para Recepción de Vehículos
    public Reserva buscarPorMatricula(String matricula) throws IOException {
        for (Reserva r : listar()) {
            if (r.getMatricula().equalsIgnoreCase(matricula)) return r;
        }
        return null;
    }
}