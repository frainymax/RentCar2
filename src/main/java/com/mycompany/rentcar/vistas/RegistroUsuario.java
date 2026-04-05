package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.UsuarioDAO;
import com.mycompany.rentcar.modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroUsuario extends MantenimientoBase {

    private String loginOriginal = "";
    private UsuarioDAO dao = new UsuarioDAO();
    private MenuAdmin menu;

    private JTextField txtLogin = new JTextField(10);
    private JPasswordField txtPass = new JPasswordField(10);
    private JTextField txtNombre = new JTextField(10);
    private JTextField txtApellido = new JTextField(10);
    private JTextField txtEmail = new JTextField(10);
    private JComboBox<String> cmbNivel =
            new JComboBox<>(new String[]{"0 - ADMIN", "1 - USUARIO"});

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    public RegistroUsuario(MenuAdmin menu) {

        super();
        this.menu = menu;

        setTitle("Mantenimiento de Usuarios");

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));

        form.add(new JLabel("Login")); form.add(txtLogin);
        form.add(new JLabel("Password")); form.add(txtPass);
        form.add(new JLabel("Nombre")); form.add(txtNombre);
        form.add(new JLabel("Apellido")); form.add(txtApellido);
        form.add(new JLabel("Email")); form.add(txtEmail);
        form.add(new JLabel("Nivel")); form.add(cmbNivel);

        add(form);

        modelo.setColumnIdentifiers(
                new String[]{"Login", "Nombre", "Apellido", "Email", "Nivel"}
        );

        tabla.setModel(modelo);
        tabla.setEnabled(true);

        add(new JScrollPane(tabla));

        cargarTabla();
        eventos();
    }

    private void eventos() {

        txtLogin.addActionListener(e -> {
            try {
                Usuario u = dao.buscarPorLogin(txtLogin.getText().trim());

                if (u != null) {
                    loginOriginal = u.getLogin();
                    txtPass.setText(u.getPass());
                    txtNombre.setText(u.getNombre());
                    txtApellido.setText(u.getApellido());
                    txtEmail.setText(u.getEmail());
                    cmbNivel.setSelectedIndex(u.getNivel());

                    estadoModificar();
                } else {
                    loginOriginal = "";
                    estadoNuevo();
                }

            } catch (Exception ignored) {}
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtLogin.setText(modelo.getValueAt(fila, 0).toString());
                txtLogin.postActionEvent();
            }
        });
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Usuario u : dao.obtenerUsuarios()) {
                modelo.addRow(new Object[]{
                        u.getLogin(), u.getNombre(),
                        u.getApellido(), u.getEmail(), u.getNivel()
                });
            }
        } catch (Exception ignored) {}
    }

    // ===== IMPLEMENTACIONES DEL BASE =====

    @Override
    protected void limpiarCampos() {
        txtLogin.setText("");
        txtPass.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        cmbNivel.setSelectedIndex(0);
        loginOriginal = "";
        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {
        if (txtLogin.getText().isEmpty()
                || txtPass.getPassword().length == 0
                || txtNombre.getText().isEmpty()
                || txtApellido.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos");
            return false;
        }
        return true;
    }

    @Override
    protected void guardarRegistro() {
        try {
            Usuario u = new Usuario(
                    txtLogin.getText(),
                    new String(txtPass.getPassword()),
                    cmbNivel.getSelectedIndex(),
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtEmail.getText()
            );

            dao.guardarUsuario(u, loginOriginal);
            loginOriginal = txtLogin.getText();

            JOptionPane.showMessageDialog(this, "Usuario guardado");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {
            dao.eliminarUsuario(loginOriginal);
            JOptionPane.showMessageDialog(this, "Usuario eliminado");
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