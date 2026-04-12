package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.dao.VehiculoDAO;
import com.mycompany.rentcar.modelo.Gama;
import com.mycompany.rentcar.modelo.Vehiculo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroVehiculo extends MantenimientoBase {

    private boolean chkTocado = false;

    private VehiculoDAO dao = new VehiculoDAO();
    private GamaDAO gamaDAO = new GamaDAO();
    private String original = "";
    private MenuAdmin menu;

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    JTextField txtMat = new JTextField(10);
    JTextField txtMarca = new JTextField(10);
    JTextField txtModelo = new JTextField(10);
    JTextField txtGama = new JTextField(10);
    JTextField txtDescGama = new JTextField(10);
    JTextField txtPrecioGama = new JTextField(10);
    JTextField txtColor = new JTextField(10);

    JComboBox<String> cmbTipoVeh = new JComboBox<>(new String[]{"0-Normal", "1-Turístico"});
    JComboBox<String> cmbMotor = new JComboBox<>(new String[]{"0-Gasolina", "1-Diésel"});

    JCheckBox chkTecho = new JCheckBox("Techo");
    JCheckBox chkAire = new JCheckBox("Aire");
    JCheckBox chkCuero = new JCheckBox("Cuero");
    JCheckBox chkAuto = new JCheckBox("Automático");
    JCheckBox chkStatus = new JCheckBox("Disponible", true);

    public RegistroVehiculo(MenuAdmin m) {
        super();

        this.menu = m;
        chkStatus.setEnabled(false);
        txtDescGama.setEditable(false);
        txtPrecioGama.setEditable(false);
        txtDescGama.setBackground(Color.LIGHT_GRAY);
        txtPrecioGama.setBackground(Color.LIGHT_GRAY);

        JPanel contenedor = new JPanel(new BorderLayout());

        JPanel f = new JPanel(new GridLayout(12, 2, 5, 5));

        f.add(new JLabel("Matricula"));
        f.add(txtMat);
        f.add(new JLabel("Marca"));
        f.add(txtMarca);
        f.add(new JLabel("Modelo"));
        f.add(txtModelo);
        f.add(new JLabel("Tipo Vehículo"));
        f.add(cmbTipoVeh);
        f.add(new JLabel("Tipo Motor"));
        f.add(cmbMotor);
        f.add(new JLabel("Gama"));
        f.add(txtGama);
        f.add(new JLabel("Desc Gama"));
        f.add(txtDescGama);
        f.add(new JLabel("Precio Gama"));
        f.add(txtPrecioGama);
        f.add(new JLabel("Color"));
        f.add(txtColor);
        f.add(chkTecho);
        f.add(chkAire);
        f.add(chkCuero);
        f.add(chkAuto);
        f.add(chkStatus);

        contenedor.add(f, BorderLayout.NORTH);

        modelo.setColumnIdentifiers(new String[]{
            "Matricula", "Marca", "Modelo", "Gama", "Color", "Status"
        });

        tabla.setModel(modelo);
        tabla.setEnabled(true); // 🔥 CLAVE

        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JScrollPane scrollGeneral = new JScrollPane(contenedor);
        add(scrollGeneral);

        eventos();
        cargarTabla();
    }

    private void eventos() {
        txtMat.addActionListener(e -> buscarVehiculo());
        txtGama.addActionListener(e -> buscarGama());

        txtMat.addActionListener(e -> txtMarca.requestFocus());
        txtMarca.addActionListener(e -> txtModelo.requestFocus());
        txtModelo.addActionListener(e -> cmbTipoVeh.requestFocus());
        cmbTipoVeh.addActionListener(e -> cmbMotor.requestFocus());
        cmbMotor.addActionListener(e -> txtGama.requestFocus());
        txtGama.addActionListener(e -> {
            buscarGama();
            txtColor.requestFocus();
        });
        txtColor.addActionListener(e -> chkTecho.requestFocus());

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                txtMat.setText(modelo.getValueAt(fila, 0).toString());
                buscarVehiculo(); // 🔥 YA NO postActionEvent
            }
        });
        chkTecho.addActionListener(e -> chkTocado = true);
        chkAire.addActionListener(e -> chkTocado = true);
        chkCuero.addActionListener(e -> chkTocado = true);
        chkAuto.addActionListener(e -> chkTocado = true);
    }

    private void buscarGama() {
        try {
            Gama g = gamaDAO.buscarPorId(txtGama.getText().trim());

            if (g != null) {
                txtDescGama.setText(g.getDescripcion());
                txtPrecioGama.setText(String.valueOf(g.getPrecio()));
            } else {
                txtDescGama.setText("");
                txtPrecioGama.setText("");
                JOptionPane.showMessageDialog(this, "Id Gama no existe");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error buscando Gama");
        }
    }

    private void buscarVehiculo() {
        try {
            Vehiculo v = dao.buscar(txtMat.getText());

            if (v != null) {
                original = v.getMatricula();

                txtMarca.setText(v.getMarca());
                txtModelo.setText(v.getModelo());
                txtGama.setText(v.getGama());
                txtColor.setText(v.getColor());

                cmbTipoVeh.setSelectedIndex(v.getTipoVehiculo());
                cmbMotor.setSelectedIndex(v.getTipoMotor());

                chkTecho.setSelected(v.isTecho());
                chkAire.setSelected(v.isAire());
                chkCuero.setSelected(v.isCuero());
                chkAuto.setSelected(v.isAutomatico());
                chkStatus.setSelected(v.isStatus());

                buscarGama();
                chkTocado = true;
                estadoModificar();
            } else {
                estadoNuevo();
            }
        } catch (Exception ignored) {
        }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Vehiculo v : dao.listar()) {
                modelo.addRow(new Object[]{
                    v.getMatricula(),
                    v.getMarca(),
                    v.getModelo(),
                    v.getGama(),
                    v.getColor(),
                    v.isStatus()
                });
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void limpiarCampos() {
        txtMat.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtGama.setText("");
        txtDescGama.setText("");
        txtPrecioGama.setText("");
        txtColor.setText("");

        chkTecho.setSelected(false);
        chkAire.setSelected(false);
        chkCuero.setSelected(false);
        chkAuto.setSelected(false);
        chkStatus.setSelected(true);

        original = "";
        chkTocado = false;
        cargarTabla();

    }

    @Override

    protected boolean validarCampos() {

        if (txtMat.getText().isEmpty()
                || txtMarca.getText().isEmpty()
                || txtModelo.getText().isEmpty()
                || txtGama.getText().isEmpty()
                || txtColor.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Campos obligatorios vacíos");
            return false;
        }

        if (!chkTocado) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar las características del vehículo (checks)");
            return false;
        }

        try {
            if (gamaDAO.buscarPorId(txtGama.getText().trim()) == null) {
                JOptionPane.showMessageDialog(this, "Id Gama no existe");
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
            Vehiculo v = new Vehiculo(
                    txtMat.getText(),
                    txtMarca.getText(),
                    txtModelo.getText(),
                    cmbTipoVeh.getSelectedIndex(),
                    cmbMotor.getSelectedIndex(),
                    txtGama.getText(),
                    txtDescGama.getText(),
                    chkTecho.isSelected(),
                    chkAire.isSelected(),
                    chkCuero.isSelected(),
                    txtColor.getText(),
                    chkAuto.isSelected(),
                    chkStatus.isSelected()
            );

            dao.guardar(v, original);
            JOptionPane.showMessageDialog(this, "Vehículo guardado");
            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {
            dao.eliminar(original); // 🔥 AHORA SÍ BORRA
            JOptionPane.showMessageDialog(this, "Vehículo eliminado");
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
