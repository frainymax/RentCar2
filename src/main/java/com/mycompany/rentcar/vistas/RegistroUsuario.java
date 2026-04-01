package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.UsuarioDAO;
import com.mycompany.rentcar.modelo.Usuario;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.io.IOException;

public class RegistroUsuario extends MantenimientoBase {

    private MenuAdmin menu;
    private UsuarioDAO dao = new UsuarioDAO();

    public RegistroUsuario(MenuAdmin menu) {
        super();
        this.menu = menu;

        setTitle("Mantenimiento de Usuarios");

        modelo.addColumn("Login");
        modelo.addColumn("Clave");
        modelo.addColumn("Nombre");
        modelo.addColumn("Apellido");
        modelo.addColumn("Email");
        modelo.addColumn("Nivel");

        // Combo nivel
        String[] niveles = {"", "0 - ADMIN", "1 - USUARIO"};
        JComboBox<String> comboNivel = new JComboBox<>(niveles);
        tabla.getColumnModel().getColumn(5)
                .setCellEditor(new DefaultCellEditor(comboNivel));

        cargarDatos();

        //  EVENTO DEL LOGIN 
        DefaultCellEditor editorLogin = new DefaultCellEditor(new JTextField());

        editorLogin.addCellEditorListener(new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {

                int fila = tabla.getSelectedRow();
                if (fila == -1) return;

                String login = modelo.getValueAt(fila, 0).toString().trim();
                if (login.isEmpty()) return;

                try {
                    Usuario u = dao.buscarPorLogin(login);

                    if (u != null) {
                        modelo.setValueAt(u.getPass(), fila, 1);
                        modelo.setValueAt(u.getNombre(), fila, 2);
                        modelo.setValueAt(u.getApellido(), fila, 3);
                        modelo.setValueAt(u.getEmail(), fila, 4);
                        modelo.setValueAt(
                                u.getNivel() == 0 ? "0 - ADMIN" : "1 - USUARIO",
                                fila, 5
                        );

                        JOptionPane.showMessageDialog(RegistroUsuario.this,
                                "Modificando usuario existente");

                    } else {
                        JOptionPane.showMessageDialog(RegistroUsuario.this,
                                "Creando nuevo usuario");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) { }
        });

        tabla.getColumnModel().getColumn(0).setCellEditor(editorLogin);

        btnVolver.addActionListener(e -> {
            menu.setVisible(true);
            dispose();
        });
    }

    @Override
    protected void cargarDatos() {
        try {
            modelo.setRowCount(0);

            for (Usuario u : dao.obtenerUsuarios()) {
                modelo.addRow(new Object[]{
                        u.getLogin(),
                        u.getPass(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getEmail(),
                        u.getNivel() == 0 ? "0 - ADMIN" : "1 - USUARIO"
                });
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error cargando usuarios");
        }
    }

    @Override
    protected void nuevo() {
        modelo.addRow(new Object[]{"", "", "", "", "", ""});
    }

    @Override
    protected void guardar() {

        //  Forzar que termine la edición antes de leer la tabla
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila");
            return;
        }

        try {
            String login = modelo.getValueAt(fila, 0).toString().trim();
            String pass = modelo.getValueAt(fila, 1).toString().trim();
            String nombre = modelo.getValueAt(fila, 2).toString().trim();
            String apellido = modelo.getValueAt(fila, 3).toString().trim();

            String email = "";
            if (modelo.getValueAt(fila, 4) != null) {
                email = modelo.getValueAt(fila, 4).toString().trim();
            }

            String nivelTexto = modelo.getValueAt(fila, 5).toString().trim();

            //  Validación obligatorios
            if (login.isEmpty() || pass.isEmpty()
                    || nombre.isEmpty() || apellido.isEmpty()
                    || nivelTexto.isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Login, Clave, Nombre y Apellido son obligatorios");
                return;
            }

            int nivel = nivelTexto.startsWith("0") ? 0 : 1;

            Usuario u = new Usuario(login, pass, nivel, nombre, apellido, email);

            dao.guardarUsuario(u);

            JOptionPane.showMessageDialog(this, "Usuario guardado correctamente");
            cargarDatos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminar() {

        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione la fila a eliminar");
            return;
        }

        try {
            String login = modelo.getValueAt(fila, 0).toString().trim();

            dao.eliminarUsuario(login);

            JOptionPane.showMessageDialog(this, "Usuario eliminado");
            cargarDatos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error eliminando usuario");
        }
    }
}