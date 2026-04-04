package com.mycompany.rentcar.vistas;

import javax.swing.*;

public abstract class MantenimientoBase extends JFrame {

    protected JButton btnNuevo = new JButton("Nuevo");
    protected JButton btnGuardar = new JButton("Guardar");
    protected JButton btnEliminar = new JButton("Eliminar");
    protected JButton btnVolver = new JButton("Volver");

    protected JLabel lblEstado = new JLabel("Nuevo registro");

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

        // 🔥 FLUJO CENTRALIZADO
        btnNuevo.addActionListener(e -> {
            limpiarCampos();
            estadoNuevo();
        });

        btnGuardar.addActionListener(e -> {
            if (validarCampos()) {
                guardarRegistro();
                limpiarCampos();
                estadoNuevo();
            }
        });

        btnEliminar.addActionListener(e -> {
            eliminarRegistro();
            limpiarCampos();
            estadoNuevo();
        });

        btnVolver.addActionListener(e -> volver());
    }

    // ===== MÉTODOS QUE HIJOS IMPLEMENTAN =====
    protected abstract void limpiarCampos();
    protected abstract boolean validarCampos();
    protected abstract void guardarRegistro();
    protected abstract void eliminarRegistro();
    protected abstract void volver();

    // ===== CONTROL DE ESTADO =====
    protected void estadoNuevo() {
        lblEstado.setText("Creando registro");
        btnEliminar.setEnabled(false);
    }

    protected void estadoModificar() {
        lblEstado.setText("Modificando registro");
        btnEliminar.setEnabled(true);
    }
}