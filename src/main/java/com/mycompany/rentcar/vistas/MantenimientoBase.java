package com.mycompany.rentcar.vistas;

import javax.swing.*;

public abstract class MantenimientoBase extends JFrame {

    protected JButton btnNuevo = new JButton("Nuevo");
    protected JButton btnGuardar = new JButton("Guardar");
    protected JButton btnEliminar = new JButton("Eliminar");
    protected JButton btnVolver = new JButton("Volver");

    protected JLabel lblEstado = new JLabel(" ");

    public MantenimientoBase() {

        JPanel botones = new JPanel();
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnVolver);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(lblEstado);
        add(botones);

        setSize(700, 500);
        setLocationRelativeTo(null);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnVolver.addActionListener(e -> volver()); // ← IMPORTANTE
    }

    protected abstract void nuevo();
    protected abstract void guardar();
    protected abstract void eliminar();
    protected abstract void volver(); // ← NUEVO
}