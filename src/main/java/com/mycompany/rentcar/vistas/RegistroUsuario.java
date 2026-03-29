package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.UsuarioDAO;
import com.mycompany.rentcar.modelo.Usuario;
import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


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

        String[] niveles = {"", "0 - ADMIN", "1 - USUARIO"};

        JComboBox<String> comboNivel = new JComboBox<>(niveles);
        tabla.getColumnModel().getColumn(5)
                .setCellEditor(new DefaultCellEditor(comboNivel));

        cargarDatos();

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
        modelo.addRow(new Object[]{"", "", "", "", "", "1"});
    }

   @Override
protected void guardar() {
    try {

        List<Usuario> lista = new ArrayList<>();

        for (int i = 0; i < modelo.getRowCount(); i++) {

            String login = modelo.getValueAt(i, 0).toString().trim();
            String pass = modelo.getValueAt(i, 1).toString().trim();
            String nombre = modelo.getValueAt(i, 2).toString().trim();
            String apellido = modelo.getValueAt(i, 3).toString().trim();

            String email = "";
            if (modelo.getValueAt(i, 4) != null) {
                email = modelo.getValueAt(i, 4).toString().trim();
            }

            String nivelTexto = modelo.getValueAt(i, 5).toString().trim();
            int nivel = nivelTexto.startsWith("0") ? 0 : 1;

            if (login.isEmpty() || pass.isEmpty()
                    || nombre.isEmpty() || apellido.isEmpty()
                    || nivelTexto.isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Faltan campos obligatorios en la fila " + (i + 1));
                return;
            }

            lista.add(new Usuario(login, pass, nivel, nombre, apellido, email));
        }

        dao.reescribirArchivo(lista);

        JOptionPane.showMessageDialog(this, "Usuarios guardados correctamente");
        cargarDatos();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al guardar");
    }
}

    @Override
    protected void eliminar() {
        int fila = tabla.getSelectedRow();

        if (fila != -1) {
            modelo.removeRow(fila);
        }
    }
}
