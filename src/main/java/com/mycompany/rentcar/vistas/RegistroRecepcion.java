package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.*;
import com.mycompany.rentcar.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class RegistroRecepcion extends MantenimientoBase {

    private RecepcionDAO dao = new RecepcionDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private VehiculoDAO vehiculoDAO = new VehiculoDAO();

    private String original = "";
    private MenuAdmin menu;

    JTextField txtId = new JTextField(15);
    JTextField txtMatricula = new JTextField(15);
    JTextField txtFechaEntrada = new JTextField(15);

    // 🔥 CALENDARIO
    JSpinner spFechaRecepcion = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spFechaRecepcion, "yyyy-MM-dd");

    JTextField txtObs = new JTextField(15);

    JLabel lblEstado = new JLabel("Creando");

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel(){
        public boolean isCellEditable(int r, int c){ return false; }
    };

    public RegistroRecepcion(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Recepción de Vehículos");
        setSize(600,500);
        setLocationRelativeTo(null);

        JPanel contenedor = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,5,5));

        spFechaRecepcion.setEditor(editor);

        form.add(new JLabel("Estado")); form.add(lblEstado);
        form.add(new JLabel("ID")); form.add(txtId);
        form.add(new JLabel("Matrícula")); form.add(txtMatricula);
        form.add(new JLabel("Fecha Entrada")); form.add(txtFechaEntrada);
        form.add(new JLabel("Fecha Recepción")); form.add(spFechaRecepcion);
        form.add(new JLabel("Observación")); form.add(txtObs);

        contenedor.add(form, BorderLayout.NORTH);

        modelo.setColumnIdentifiers(new String[]{
                "ID","Matricula","Fecha Entrada","Fecha Recepción"
        });

        tabla.setModel(modelo);
        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(contenedor);

        txtFechaEntrada.setEnabled(false);

        eventos();
        cargarTabla();
    }

    private void eventos(){

        // ENTER FLOW
        txtId.addActionListener(e -> txtMatricula.requestFocus());
        txtMatricula.addActionListener(e -> {
            cargarReserva();
            spFechaRecepcion.requestFocus();
        });

        // CLICK TABLA
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int fila = tabla.getSelectedRow();

                txtId.setText(modelo.getValueAt(fila,0).toString());
                txtMatricula.setText(modelo.getValueAt(fila,1).toString());
                txtFechaEntrada.setText(modelo.getValueAt(fila,2).toString());

                // convertir string → date
                LocalDate fecha = LocalDate.parse(modelo.getValueAt(fila,3).toString());
                Date date = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
                spFechaRecepcion.setValue(date);

                original = txtId.getText();

                txtId.setEnabled(false); // 🔥 BLOQUEAR ID
                txtMatricula.setEnabled(false);

                lblEstado.setText("Modificando");
                estadoModificar();
            }
        });
    }

   private void cargarReserva(){
    try{
        boolean encontrada = false;

        for(Reserva r: reservaDAO.listar()){
            if(r.getMatricula().equals(txtMatricula.getText())){
                txtFechaEntrada.setText(r.getFechaEntrada());
                encontrada = true;
                break;
            }
        }

        if(!encontrada){
            txtFechaEntrada.setText("");
            JOptionPane.showMessageDialog(this,"No hay reserva para este vehículo");
        }

    }catch(Exception e){
        JOptionPane.showMessageDialog(this,"Error cargando reserva");
    }
}

    private String getFechaSpinner(){
        Date date = (Date) spFechaRecepcion.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private void cargarTabla(){
        modelo.setRowCount(0);

        try{
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
        txtObs.setText("");

        spFechaRecepcion.setValue(new Date());

        original = "";
        lblEstado.setText("Creando");

        txtId.setEnabled(true); // 🔥 desbloquear
        txtMatricula.setEnabled(true);

        tabla.clearSelection(); // 🔥 FIX BUG

        cargarTabla();
    }

    @Override
  
protected boolean validarCampos() {

    if(txtId.getText().isEmpty()
            || txtMatricula.getText().isEmpty()){
        JOptionPane.showMessageDialog(this,"Campos obligatorios");
        return false;
    }

    // 🔥 VALIDAR QUE HAYA RESERVA CARGADA
    if(txtFechaEntrada.getText().isEmpty()){
        JOptionPane.showMessageDialog(this,"Debe cargar una reserva válida");
        return false;
    }

    try{
        Recepcion existente = dao.buscar(txtId.getText());

        if(existente != null && !txtId.getText().equals(original)){
            JOptionPane.showMessageDialog(this,"ID ya existe");
            return false;
        }

        LocalDate entrada;

try {
    entrada = LocalDate.parse(txtFechaEntrada.getText());
} catch (Exception e) {

    java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", java.util.Locale.ENGLISH);

    entrada = java.time.ZonedDateTime
            .parse(txtFechaEntrada.getText(), formatter)
            .toLocalDate();
}
        LocalDate recepcion = LocalDate.parse(getFechaSpinner());

        if(recepcion.isBefore(entrada)){
            JOptionPane.showMessageDialog(this,"La recepción no puede ser menor que la entrada");
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
                    getFechaSpinner(),
                    txtObs.getText()
            );

            dao.guardar(r, original);

            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());
            if(v != null){
                v.setStatus(true);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this,"Guardado");
            limpiarCampos();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try {

            String id = txtId.getText();

            if(id.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleccione o cargue una recepción");
                return;
            }

            int op = JOptionPane.showConfirmDialog(this, "¿Eliminar?");
            if (op != JOptionPane.YES_OPTION) return;

            Recepcion r = dao.buscar(id);

            if(r == null){
                JOptionPane.showMessageDialog(this,"No existe");
                return;
            }

            dao.eliminar(id);

            Vehiculo v = vehiculoDAO.buscar(r.getMatricula());
            if (v != null) {
                v.setStatus(false);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this, "Eliminado");
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