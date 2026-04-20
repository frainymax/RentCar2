package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.dao.VehiculoDAO;
import com.mycompany.rentcar.modelo.Gama;
import com.mycompany.rentcar.modelo.Vehiculo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroVehiculo extends MantenimientoBase {

    // ✅ FIX #3: chkTocado se maneja correctamente ahora.
    // Se pone true solo cuando el usuario interactúa, o cuando se carga un registro existente.
    private boolean chkTocado = false;

    private VehiculoDAO dao = new VehiculoDAO();
    private GamaDAO gamaDAO = new GamaDAO();
    private String original = "";
    private MenuAdmin menu;

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel();

    JTextField txtMat      = new JTextField(10);
    JTextField txtMarca    = new JTextField(10);
    JTextField txtModelo   = new JTextField(10);
    JTextField txtGama     = new JTextField(10);
    JTextField txtDescGama = new JTextField(10);
    JTextField txtPrecioGama = new JTextField(10);
    JTextField txtColor    = new JTextField(10);

    JComboBox<String> cmbTipoVeh = new JComboBox<>(new String[]{"0-Normal", "1-Turístico"});
    JComboBox<String> cmbMotor   = new JComboBox<>(new String[]{"0-Gasolina", "1-Diésel"});

    JCheckBox chkTecho  = new JCheckBox("Techo");
    JCheckBox chkAire   = new JCheckBox("Aire");
    JCheckBox chkCuero  = new JCheckBox("Cuero");
    JCheckBox chkAuto   = new JCheckBox("Automático");
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
        tabla.setEnabled(true);

        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JScrollPane scrollGeneral = new JScrollPane(contenedor);
        add(scrollGeneral);

        eventos();
        cargarTabla();
    }

    private void eventos() {
        // ✅ FIX #2: ActionListeners de txtMat UNIFICADOS en uno solo.
        // Antes había DOS addActionListener en txtMat: uno buscaba, otro movía foco.
        // El primero cancelaba al segundo. Ahora es uno solo que hace ambas cosas.
        txtMat.addActionListener(e -> {
            buscarVehiculo();
            txtMarca.requestFocus();
        });

        // ✅ FIX #2: Navegación con Enter corregida para todos los campos.
        // Antes cmbTipoVeh y cmbMotor usaban addActionListener que dispara
        // al cambiar selección, no solo al presionar Enter. Se usa KeyListener.
        txtMarca.addActionListener(e -> txtModelo.requestFocus());
        txtModelo.addActionListener(e -> cmbTipoVeh.requestFocus());

        // ComboBoxes: usar KeyListener para Enter, no ActionListener
        // (ActionListener en JComboBox se dispara al cambiar ítem, no al presionar Enter)
        cmbTipoVeh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    cmbMotor.requestFocus();
            }
        });
        cmbMotor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    txtGama.requestFocus();
            }
        });

        // ✅ FIX #4: txtGama tiene UN SOLO ActionListener que busca gama Y mueve foco.
        // Antes había DOS listeners en txtGama registrados (uno al inicio de eventos()
        // y otro al final), causando que buscarGama() se llamara dos veces y el foco
        // se moviera antes de que terminara la búsqueda.
        txtGama.addActionListener(e -> {
            buscarGama();
            txtColor.requestFocus();
        });

        txtColor.addActionListener(e -> chkTecho.requestFocus());

        // Click en tabla → carga el vehículo
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    txtMat.setText(modelo.getValueAt(fila, 0).toString());
                    buscarVehiculo();
                }
            }
        });

        // Checks marcan que el usuario los tocó
        chkTecho.addActionListener(e -> chkTocado = true);
        chkAire.addActionListener(e  -> chkTocado = true);
        chkCuero.addActionListener(e -> chkTocado = true);
        chkAuto.addActionListener(e  -> chkTocado = true);
    }

    // ✅ FIX #4: buscarGama() lee txtGama en el momento de llamarse.
    // Antes, cuando buscarVehiculo() hacía txtGama.setText(...) y luego llamaba
    // buscarGama(), esto funcionaba, pero el ActionListener de txtGama también
    // disparaba buscarGama() una segunda vez con texto a veces vacío (race condition
    // de eventos Swing). Ahora buscarGama() es llamado explícitamente solo desde
    // el ActionListener unificado y desde buscarVehiculo(), sin duplicación.
    private void buscarGama() {
        String idGama = txtGama.getText().trim();
        if (idGama.isEmpty()) {
            txtDescGama.setText("");
            txtPrecioGama.setText("");
            return;
        }
        try {
            Gama g = gamaDAO.buscarPorId(idGama);
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
            Vehiculo v = dao.buscar(txtMat.getText().trim());

            if (v != null) {
                original = v.getMatricula();

                txtMarca.setText(v.getMarca());
                txtModelo.setText(v.getModelo());
                txtColor.setText(v.getColor());

                cmbTipoVeh.setSelectedIndex(v.getTipoVehiculo());
                cmbMotor.setSelectedIndex(v.getTipoMotor());

                chkTecho.setSelected(v.isTecho());
                chkAire.setSelected(v.isAire());
                chkCuero.setSelected(v.isCuero());
                chkAuto.setSelected(v.isAutomatico());
                chkStatus.setSelected(v.isStatus());

                // ✅ FIX #4: Primero ponemos el texto en txtGama, LUEGO buscamos la gama.
                // Este orden ya existía antes, pero ahora es explícito y sin duplicación
                // de listeners. txtDescGama y txtPrecioGama se llenan correctamente
                // porque buscarGama() lee txtGama.getText() que ya tiene el valor.
                txtGama.setText(v.getGama());
                buscarGama(); // Llama directamente, sin disparar ActionListener

                // ✅ FIX #3: Marcar chkTocado = true al cargar un registro existente,
                // ya que los checks vienen del archivo (el usuario no necesita tocarlos
                // de nuevo para que sean válidos en modificación).
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

        original  = "";
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
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar las características del vehículo (checks)");
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
            dao.eliminar(original);
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