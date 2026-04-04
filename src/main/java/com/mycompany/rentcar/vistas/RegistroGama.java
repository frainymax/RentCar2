package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.modelo.Gama;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroGama extends MantenimientoBase {

    private GamaDAO dao = new GamaDAO();
    private MenuAdmin menu;

    private JTextField txtId = new JTextField(10);
    private JTextField txtDescripcion = new JTextField(10);
    private JTextField txtPrecio = new JTextField(10);

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    private int idOriginal = -1;

    public RegistroGama(MenuAdmin menu) {
        super();
        this.menu = menu;

        setTitle("Mantenimiento de Gamas");

        JPanel form = new JPanel(new GridLayout(3,2,10,10));
        form.add(new JLabel("ID Gama"));
        form.add(txtId);
        form.add(new JLabel("Descripción"));
        form.add(txtDescripcion);
        form.add(new JLabel("Precio"));
        form.add(txtPrecio);

        add(form);

        modelo.setColumnIdentifiers(new String[]{"ID","Descripción","Precio"});
        tabla.setModel(modelo);
        tabla.setEnabled(false);

        add(new JScrollPane(tabla));

        cargarTabla();
        eventos();
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Gama g : dao.obtenerTodas()) {
                modelo.addRow(new Object[]{
                        g.getIdGama(),
                        g.getDescripcion(),
                        g.getPrecio()
                });
            }
        } catch (Exception e) {}
    }

    private void eventos() {
        txtId.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtId.getText());
                Gama g = dao.buscarPorId(id);

                if (g != null) {
                    idOriginal = g.getIdGama();
                    txtDescripcion.setText(g.getDescripcion());
                    txtPrecio.setText(String.valueOf(g.getPrecio()));

                    lblEstado.setText("Modificando");
                    btnEliminar.setEnabled(true);
                } else {
                    lblEstado.setText("Creando");
                    btnEliminar.setEnabled(false);
                }
            } catch (Exception ex) {}
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtId.setText(modelo.getValueAt(fila,0).toString());
                txtId.postActionEvent();
            }
        });
    }

    @Override
    protected void nuevo() {
        txtId.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        lblEstado.setText("Nueva Gama");
        idOriginal = -1;
    }

    @Override
    protected void guardar() {
        try {
            Gama g = new Gama(
                    Integer.parseInt(txtId.getText()),
                    txtDescripcion.getText(),
                    Double.parseDouble(txtPrecio.getText())
            );

            dao.guardar(g, idOriginal == -1 ? g.getIdGama() : idOriginal);
            cargarTabla();
            nuevo();
        } catch (Exception e) {}
    }

    @Override
    protected void eliminar() {}

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
}