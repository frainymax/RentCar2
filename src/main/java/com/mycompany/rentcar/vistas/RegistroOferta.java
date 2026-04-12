package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.OfertaDAO;
import com.mycompany.rentcar.dao.VehiculoDAO;
import com.mycompany.rentcar.modelo.Oferta;
import com.mycompany.rentcar.modelo.Vehiculo;
import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.modelo.Gama;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroOferta extends MantenimientoBase {

    private GamaDAO gamaDAO = new GamaDAO();
    private OfertaDAO dao = new OfertaDAO();
    private VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private String original = "";
    private MenuAdmin menu;

    JTextField txtId = new JTextField(10);
    JTextField txtMatricula = new JTextField(10);
    JTextField txtDescripcion = new JTextField(10);
    JTextField txtPrecio = new JTextField(10); // % descuento

    JTextField txtMarca = new JTextField(10);
    JTextField txtModelo = new JTextField(10);
    JTextField txtPrecioGama = new JTextField(10);
    JTextField txtPrecioFinal = new JTextField(10); // 👈 NUEVO

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    public RegistroOferta(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Mantenimiento de Ofertas");

        JPanel contenedor = new JPanel(new BorderLayout());

        JPanel f = new JPanel(new GridLayout(8, 2, 5, 5));

        f.add(new JLabel("Id Oferta"));
        f.add(txtId);
        f.add(new JLabel("Matricula"));
        f.add(txtMatricula);
        f.add(new JLabel("Marca"));
        f.add(txtMarca);
        f.add(new JLabel("Modelo"));
        f.add(txtModelo);
        f.add(new JLabel("Precio Gama"));
        f.add(txtPrecioGama);
        f.add(new JLabel("Descripción"));
        f.add(txtDescripcion);
        f.add(new JLabel("Descuento (%)"));
        f.add(txtPrecio);
        f.add(new JLabel("Precio Final")); // 👈 NUEVO
        f.add(txtPrecioFinal);

        txtMarca.setEnabled(false);
        txtModelo.setEnabled(false);
        txtPrecioGama.setEnabled(false);
        txtPrecioFinal.setEnabled(false);
        txtPrecioFinal.setBackground(Color.LIGHT_GRAY);

        contenedor.add(f, BorderLayout.NORTH);

        modelo.setColumnIdentifiers(new String[]{
            "ID", "Matricula", "Descripcion", "Precio Final"
        });

        tabla.setModel(modelo);
        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(new JScrollPane(contenedor));

        eventos();
        cargarTabla();
    }

    private void eventos() {

        // ===== BUSCAR OFERTA =====
        txtId.addActionListener(e -> {
            try {
                Oferta o = dao.buscar(txtId.getText());

                if (o != null) {
                    original = o.getId();

                    txtMatricula.setText(o.getMatricula());
                    txtDescripcion.setText(o.getDescripcion());

                    txtPrecioFinal.setText(String.valueOf(o.getPrecio())); // 👈 correcto
                    txtPrecio.setText(""); // 👈 evitar confusión

                    buscarVehiculo();
                    JOptionPane.showMessageDialog(this, "Modificando");
                    estadoModificar();
                } else {
                    original = "";
                    JOptionPane.showMessageDialog(this, "Creando");
                    estadoNuevo();
                }

            } catch (Exception ignored) {
            }
        });

        // ===== BUSCAR VEHÍCULO =====
        txtMatricula.addActionListener(e -> {
            buscarVehiculo();
            txtDescripcion.requestFocus();
        });

        // ===== CALCULAR PRECIO =====
        txtPrecio.addActionListener(e -> calcularPrecioFinal());

        // ===== CLICK TABLA =====
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtId.setText(modelo.getValueAt(fila, 0).toString());
                txtId.postActionEvent();
            }
        });
    }

  private void calcularPrecioFinal() {
    try {
        double porcentaje = Double.parseDouble(txtPrecio.getText());
        double precioGama = Double.parseDouble(txtPrecioGama.getText());

        // 🔴 VALIDACIÓN (modo profe)
        if (porcentaje > 15) {
            JOptionPane.showMessageDialog(this, "Máximo 15%");
            txtPrecioFinal.setText("");
            txtPrecio.setText("");
            txtPrecio.requestFocus();
            return;
        }

        if (porcentaje <= 0) {
            JOptionPane.showMessageDialog(this, "Debe ser mayor que 0%");
            txtPrecioFinal.setText("");
            txtPrecio.setText("");
            txtPrecio.requestFocus();
            return;
        }

        double descuento = precioGama * (porcentaje / 100);
        double precioFinal = precioGama - descuento;

        txtPrecioFinal.setText(String.valueOf(precioFinal));

    } catch (Exception e) {
        txtPrecioFinal.setText("");
    }
}

    private void buscarVehiculo() {
        try {
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());

            if (v != null) {
                txtMarca.setText(v.getMarca());
                txtModelo.setText(v.getModelo());

                Gama g = gamaDAO.buscarPorId(v.getGama());
                if (g != null) {
                    txtPrecioGama.setText(String.valueOf(g.getPrecio()));
                }

                calcularPrecioFinal(); // 👈 recalcula si ya hay %

            } else {
                txtMarca.setText("");
                txtModelo.setText("");
                txtPrecioGama.setText("");
                txtPrecioFinal.setText("");
                JOptionPane.showMessageDialog(this, "Matrícula no existe");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error buscando vehículo");
        }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);

            for (Oferta o : dao.listar()) {
                modelo.addRow(new Object[]{
                    o.getId(),
                    o.getMatricula(),
                    o.getDescripcion(),
                    o.getPrecio()
                });
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void limpiarCampos() {
        txtId.setText("");
        txtMatricula.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtPrecioGama.setText("");
        txtPrecioFinal.setText("");

        original = "";
        cargarTabla();
    }

   @Override
protected boolean validarCampos() {

    if (txtId.getText().isEmpty()
            || txtMatricula.getText().isEmpty()
            || txtDescripcion.getText().isEmpty()
            || txtPrecio.getText().isEmpty()) {

        JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos");
        return false;
    }

    try {
        Integer.parseInt(txtId.getText());
        double porcentaje = Double.parseDouble(txtPrecio.getText());

        // 🔴 MISMA VALIDACIÓN (backup)
        if (porcentaje > 15) {
            JOptionPane.showMessageDialog(this, "Máximo 15%");
            return false;
        }

        if (porcentaje <= 0) {
            JOptionPane.showMessageDialog(this, "Debe ser mayor que 0%");
            return false;
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Datos inválidos");
        return false;
    }

    try {
        Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());
        if (v == null) {
            JOptionPane.showMessageDialog(this, "Vehículo no existe");
            return false;
        }
    } catch (Exception e) {
        return false;
    }

    return true;
}

    @Override
    protected void guardarRegistro() {
        try {

            double porcentaje = Double.parseDouble(txtPrecio.getText());
            double precioGama = Double.parseDouble(txtPrecioGama.getText());

            double descuento = precioGama * (porcentaje / 100);
            double precioFinal = precioGama - descuento;

            Oferta o = new Oferta(
                    txtId.getText(),
                    txtMatricula.getText(),
                    txtDescripcion.getText(),
                    precioFinal
            );

            dao.guardar(o, original);
            JOptionPane.showMessageDialog(this, "Guardado. Precio final: " + precioFinal);

            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {
            dao.eliminar(original);
            JOptionPane.showMessageDialog(this, "Oferta eliminada");
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
