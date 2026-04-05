package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Reserva;

import java.io.*;
import java.util.*;

public class ReservaDAO {

    private final String RUTA = "archivos/reservas.txt";

    private void asegurar() throws IOException {
        File f = new File(RUTA);

        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("matricula,cedula,oferta,fechaReserva,fechaSalida,fechaEntrada,observacion,dias,total");
            bw.newLine();
            bw.close();
        }
    }

    public void guardar(Reserva r) throws IOException {
        asegurar();

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA, true));
        bw.write(r.toString());
        bw.newLine();
        bw.close();
    }

    public List<Reserva> listar() throws IOException {
        asegurar();

        List<Reserva> lista = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(",", -1);

            lista.add(new Reserva(
                    d[0], d[1], d[2],
                    d[3], d[4], d[5],
                    d[6],
                    Integer.parseInt(d[7]),
                    Double.parseDouble(d[8])
            ));
        }

        br.close();
        return lista;
    }

    public void eliminar(String matricula) throws IOException {
        asegurar();

        List<String> lineas = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();

        String l;
        while ((l = br.readLine()) != null) {
            if (!l.split(",")[0].equals(matricula)) {
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
}