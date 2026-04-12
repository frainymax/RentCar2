package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.*;
import com.mycompany.rentcar.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class RegistroRecepcion extends MantenimientoBase {

    private RecepcionDAO dao = new RecepcionDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private VehiculoDAO vehiculoDAO = new VehiculoDAO();

    private String original = "";
    private MenuAdmin menu;

    JTextField txtId = new JTextField(15);
    JTextField txtMatricula = new JTextField(15);
    JTextField txtFechaEntrada = new JTextField(15);
    JTextField txtFechaRecepcion = new JTextField(15);
    JTextField txtObs = new JTextField(15);

    JLabel lblEstado = new JLabel("Creando");

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel(){
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RegistroRecepcion(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Recepción de Vehículos");
        setSize(600,500);
        setLocationRelativeTo(null);

        JPanel contenedor = new JPanel(new BorderLayout());

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(0,2,5,5));

        form.add(new JLabel("Estado")); form.add(lblEstado);
        form.add(new JLabel("ID")); form.add(txtId);
        form.add(new JLabel("Matrícula")); form.add(txtMatricula);
        form.add(new JLabel("Fecha Entrada")); form.add(txtFechaEntrada);
        form.add(new JLabel("Fecha Recepción")); form.add(txtFechaRecepcion);
        form.add(new JLabel("Observación")); form.add(txtObs);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setPreferredSize(new Dimension(600,180));

        contenedor.add(scrollForm, BorderLayout.NORTH);

        // ===== TABLA =====
        modelo.setColumnIdentifiers(new String[]{
                "ID","Matricula","Fecha Entrada","Fecha Recepción"
        });

        tabla.setModel(modelo);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(600,200));

        contenedor.add(scrollTabla, BorderLayout.CENTER);

        add(contenedor, BorderLayout.CENTER);

        // 🔥 BLOQUEOS
        txtFechaEntrada.setEnabled(false);

        eventos();
        cargarTabla();
    }

    private void eventos(){

        // ===== BUSCAR POR ID =====
        txtId.addActionListener(e -> {
            try{
                Recepcion r = dao.buscar(txtId.getText());

                if(r != null){
                    original = r.getId();

                    txtMatricula.setText(r.getMatricula());
                    txtFechaEntrada.setText(r.getFechaEntrada());
                    txtFechaRecepcion.setText(r.getFechaRecepcion());
                    txtObs.setText(r.getObservacion());

                    txtMatricula.setEnabled(false);

                    lblEstado.setText("Modificando");
                    JOptionPane.showMessageDialog(this,"Modificando");
                    estadoModificar();

                }else{
                    original = "";
                    txtMatricula.setEnabled(true);

                    lblEstado.setText("Creando");
                    JOptionPane.showMessageDialog(this,"Creando");
                    estadoNuevo();
                }

            }catch(Exception ignored){}
        });

        // ===== BUSCAR RESERVA =====
        txtMatricula.addActionListener(e -> cargarReserva());
    }

    private void cargarReserva(){
        try{
            for(Reserva r: reservaDAO.listar()){

                if(r.getMatricula().equals(txtMatricula.getText())){
                    txtFechaEntrada.setText(r.getFechaEntrada());
                    return;
                }
            }

            JOptionPane.showMessageDialog(this,"No hay reserva para esa matrícula");

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error buscando reserva");
        }
    }

    private void cargarTabla(){
        try{
            modelo.setRowCount(0);

            for(Recepcion r: dao.listar()){
                modelo.addRow(new Object[]{
                        r.getId(),
                        r.getMatricula(),
                        r.getFechaEntrada(),
                        r.getFechaRecepcion()
                });
            }

        }catch(Exception ignored){}
    }

    @Override
    protected void limpiarCampos() {
        txtId.setText("");
        txtMatricula.setText("");
        txtFechaEntrada.setText("");
        txtFechaRecepcion.setText("");
        txtObs.setText("");

        lblEstado.setText("Creando");
        original = "";

        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {

        if(txtId.getText().isEmpty()
                || txtMatricula.getText().isEmpty()
                || txtFechaRecepcion.getText().isEmpty()){

            JOptionPane.showMessageDialog(this,"Campos obligatorios");
            return false;
        }

        try{
            LocalDate entrada = LocalDate.parse(txtFechaEntrada.getText());
            LocalDate recepcion = LocalDate.parse(txtFechaRecepcion.getText());

            if(recepcion.isBefore(entrada)){
                JOptionPane.showMessageDialog(this,"Recepción menor que entrada");
                return false;
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Formato de fecha inválido");
            return false;
        }

        return true;
    }

    @Override
    protected void guardarRegistro() {
        try{

            Recepcion r = new Recepcion(
                    txtId.getText(),
                    txtMatricula.getText(),
                    txtFechaEntrada.getText(),
                    txtFechaRecepcion.getText(),
                    txtObs.getText()
            );

            dao.guardar(r, original);

            // 🔥 LIBERAR VEHÍCULO
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());

            if(v != null){
                v.setStatus(true);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this,"Recepción guardada");
            limpiarCampos();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error");
        }
    }

    @Override
protected void eliminarRegistro() {
    try {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una recepción");
            return;
        }

        int op = JOptionPane.showConfirmDialog(this, "¿Eliminar?");
        if (op != JOptionPane.YES_OPTION) return;

        String id = modelo.getValueAt(fila, 0).toString();
        String matricula = modelo.getValueAt(fila, 1).toString();

        dao.eliminar(id);

        // 🔥 IMPORTANTE: volver el vehículo a reservado
        Vehiculo v = vehiculoDAO.buscar(matricula);
        if (v != null) {
            v.setStatus(false); // reservado otra vez
            vehiculoDAO.guardar(v, v.getMatricula());
        }

        JOptionPane.showMessageDialog(this, "Recepción eliminada");
        limpiarCampos();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error");
    }
}

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
}