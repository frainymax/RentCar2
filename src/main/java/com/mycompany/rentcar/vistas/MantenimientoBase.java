package com.mycompany.rentcar.vistas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public abstract class MantenimientoBase extends JFrame {

    protected JTable tabla;
    protected DefaultTableModel modelo;

    protected JButton btnNuevo = new JButton("Nuevo");
    protected JButton btnGuardar = new JButton("Guardar");
    protected JButton btnEliminar = new JButton("Eliminar");
    protected JButton btnVolver = new JButton("Volver");

    public MantenimientoBase() {
        tabla = new JTable();
        modelo = new DefaultTableModel();
        tabla.setModel(modelo);

        JScrollPane scroll = new JScrollPane(tabla);

        JPanel botones = new JPanel();
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnVolver);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(scroll);
        add(botones);

        setSize(700, 400);
        setLocationRelativeTo(null);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
    }

    protected abstract void nuevo();
    protected abstract void guardar();
    protected abstract void eliminar();
    protected abstract void cargarDatos();
}