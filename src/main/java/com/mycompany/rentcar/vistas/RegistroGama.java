package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.modelo.Gama;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroGama extends MantenimientoBase {

    private String idOriginal = "";

    private GamaDAO dao = new GamaDAO();
    private MenuAdmin menu;

    // ===== CAMPOS =====
    private JTextField txtId = new JTextField(10);
    private JTextField txtDescripcion = new JTextField(10);
    private JTextField txtPrecio = new JTextField(10);

    // ===== TABLA =====
    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    public RegistroGama(MenuAdmin menu) {

        super();
        this.menu = menu;

        setTitle("Mantenimiento de Gamas");

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));

        form.add(new JLabel("Id Gama"));
        form.add(txtId);

        form.add(new JLabel("Descripción"));
        form.add(txtDescripcion);

        form.add(new JLabel("Precio"));
        form.add(txtPrecio);

        add(form);

        modelo.setColumnIdentifiers(
                new String[]{"ID", "Descripción", "Precio"}
        );

        tabla.setModel(modelo);
        tabla.setEnabled(true);

        add(new JScrollPane(tabla));

        cargarTabla();
        eventos();
    }

    private void eventos() {

        txtId.addActionListener(e -> {
            try {
                Gama g = dao.buscarPorId(txtId.getText().trim());

                if (g != null) {
                    idOriginal = g.getId();
                    txtDescripcion.setText(g.getDescripcion());
                    txtPrecio.setText(String.valueOf(g.getPrecio()));

                    estadoModificar();
                } else {
                    idOriginal = "";
                    estadoNuevo();
                }

            } catch (Exception ignored) {}
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtId.setText(modelo.getValueAt(fila, 0).toString());
                txtId.postActionEvent();
            }
        });
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Gama g : dao.obtenerGamas()) {
                modelo.addRow(new Object[]{
                        g.getId(),
                        g.getDescripcion(),
                        g.getPrecio()
                });
            }
        } catch (Exception ignored) {}
    }

    // ===== IMPLEMENTACIÓN DEL BASE =====

    @Override
    protected void limpiarCampos() {
        txtId.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        idOriginal = "";
        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {

        if (txtId.getText().isEmpty()
                || txtDescripcion.getText().isEmpty()
                || txtPrecio.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos");
            return false;
        }

        try {
            Integer.parseInt(txtId.getText());
            Double.parseDouble(txtPrecio.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID debe ser entero y Precio decimal");
            return false;
        }

        return true;
    }

    @Override
    protected void guardarRegistro() {
        try {

            Gama g = new Gama(
                    txtId.getText(),
                    txtDescripcion.getText(),
                    Double.parseDouble(txtPrecio.getText())
            );

            dao.guardarGama(g, idOriginal);
            idOriginal = txtId.getText();

            JOptionPane.showMessageDialog(this, "Gama guardada");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {
            dao.eliminarGama(idOriginal);
            JOptionPane.showMessageDialog(this, "Gama eliminada");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar");
        }
    }

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
}