package com.mycompany.rentcar.dao;

import com.mycompany.rentcar.modelo.Vehiculo;

import java.io.*;
import java.util.*;

public class VehiculoDAO {

    private final String RUTA = "archivos/vehiculos.txt";

    private void asegurar() throws IOException {
        File f = new File(RUTA);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("matricula,marca,modelo,tipoVeh,tipoMotor,gama,desc,techo,aire,cuero,color,auto,status");
            bw.newLine();
            bw.close();
        }
    }

    public Vehiculo buscar(String matricula) throws IOException {
        asegurar();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();
        String l;
        while ((l = br.readLine()) != null) {
            String[] d = l.split(",", -1);
            if (d[0].equals(matricula)) {
                br.close();
                return new Vehiculo(
                        d[0], d[1], d[2],
                        Integer.parseInt(d[3]),
                        Integer.parseInt(d[4]),
                        d[5], d[6],
                        Boolean.parseBoolean(d[7]),
                        Boolean.parseBoolean(d[8]),
                        Boolean.parseBoolean(d[9]),
                        d[10],
                        Boolean.parseBoolean(d[11]),
                        Boolean.parseBoolean(d[12])
                );
            }
        }
        br.close();
        return null;
    }

    public void guardar(Vehiculo v, String original) throws IOException {
        asegurar();
        List<String> lineas = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        String enc = br.readLine();
        boolean act = false;

        String l;
        while ((l = br.readLine()) != null) {
            if (l.split(",")[0].equals(original)) {
                lineas.add(v.toString());
                act = true;
            } else lineas.add(l);
        }
        br.close();

        if (!act) lineas.add(v.toString());

        BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA,false));
        bw.write(enc); bw.newLine();
        for(String s: lineas){ bw.write(s); bw.newLine(); }
        bw.close();
    }

    public List<Vehiculo> listar() throws IOException {
        asegurar();
        List<Vehiculo> l = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(RUTA));
        br.readLine();
        String x;
        while((x=br.readLine())!=null){
            String[] d = x.split(",", -1);
            l.add(new Vehiculo(
                    d[0], d[1], d[2],
                    Integer.parseInt(d[3]),
                    Integer.parseInt(d[4]),
                    d[5], d[6],
                    Boolean.parseBoolean(d[7]),
                    Boolean.parseBoolean(d[8]),
                    Boolean.parseBoolean(d[9]),
                    d[10],
                    Boolean.parseBoolean(d[11]),
                    Boolean.parseBoolean(d[12])
            ));
        }
        br.close();
        return l;
    }
}