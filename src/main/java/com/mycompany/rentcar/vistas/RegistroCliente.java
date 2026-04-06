package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.ClienteDAO;
import com.mycompany.rentcar.modelo.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroCliente extends MantenimientoBase {

    private ClienteDAO dao = new ClienteDAO();
    private String original = "";
    private MenuAdmin menu;

    // ===== CAMPOS =====
    JTextField txtCedula = new JTextField(10);
    JTextField txtNombre = new JTextField(10);
    JTextField txtApellido = new JTextField(10);
    JTextField txtDireccion = new JTextField(10);
    JTextField txtEmail = new JTextField(10);
    JTextField txtTelefono = new JTextField(10);

    // TABLA
    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    public RegistroCliente(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Mantenimiento de Clientes");

        JPanel contenedor = new JPanel(new BorderLayout());

        JPanel f = new JPanel(new GridLayout(6, 2, 5, 5));

        f.add(new JLabel("Cédula"));
        f.add(txtCedula);
        f.add(new JLabel("Nombre"));
        f.add(txtNombre);
        f.add(new JLabel("Apellido"));
        f.add(txtApellido);
        f.add(new JLabel("Dirección"));
        f.add(txtDireccion);
        f.add(new JLabel("Email"));
        f.add(txtEmail);
        f.add(new JLabel("Teléfono"));
        f.add(txtTelefono);

        contenedor.add(f, BorderLayout.NORTH);

        modelo.setColumnIdentifiers(new String[]{
            "Cedula", "Nombre", "Apellido", "Telefono"
        });

        tabla.setModel(modelo);
        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(new JScrollPane(contenedor));

        eventos();
        cargarTabla();
    }

    // ================= EVENTOS =================
    private void eventos() {

        // ===== VALIDAR CÉDULA =====
        txtCedula.addActionListener(e -> {
            try {
                Cliente c = dao.buscar(txtCedula.getText().trim());

                if (c != null) {
                    original = c.getCedula();

                    txtNombre.setText(c.getNombre());
                    txtApellido.setText(c.getApellido());
                    txtDireccion.setText(c.getDireccion());
                    txtEmail.setText(c.getEmail());
                    txtTelefono.setText(c.getTelefono());

                    estadoModificar();  
                } else {
                    original = "";
                    estadoNuevo();  
                }

            } catch (Exception ignored) {
            }
        });

        // CLICK TABLA
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtCedula.setText(modelo.getValueAt(fila, 0).toString());
                txtCedula.postActionEvent();
            }
        });
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);

            for (Cliente c : dao.listar()) {
                modelo.addRow(new Object[]{
                    c.getCedula(),
                    c.getNombre(),
                    c.getApellido(),
                    c.getTelefono()
                });
            }

        } catch (Exception ignored) {
        }
    }

    // ================= MANTENIMIENTO BASE =================
    @Override
    protected void limpiarCampos() {
        txtCedula.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDireccion.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");

        original = "";
        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {

        // CAMPOS OBLIGATORIOS
        if (txtCedula.getText().isEmpty()
                || txtNombre.getText().isEmpty()
                || txtApellido.getText().isEmpty()
                || txtDireccion.getText().isEmpty()
                || txtTelefono.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos");
            return false;
        }

        // VALIDAR CÉDULA
if (!txtCedula.getText().matches("\\d+")) {
    JOptionPane.showMessageDialog(this, "Cédula debe ser numérica");
    return false;
}

// VALIDAR TELÉFONO
if (!txtTelefono.getText().matches("\\d+")) {
    JOptionPane.showMessageDialog(this, "Teléfono debe ser numérico");
    return false;
}
        return true;
    }


    @Override
    protected void guardarRegistro() {
        try {
            Cliente c = new Cliente(
                    txtCedula.getText(),
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtDireccion.getText(),
                    txtEmail.getText(),
                    txtTelefono.getText()
            );

            dao.guardar(c, original);

            JOptionPane.showMessageDialog(this, "Cliente guardado");

            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {
            dao.eliminar(original);
            JOptionPane.showMessageDialog(this, "Cliente eliminado");
            limpiarCampos();
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
