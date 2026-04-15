package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.*;
import com.mycompany.rentcar.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RegistroReserva extends MantenimientoBase {

    private ReservaDAO dao = new ReservaDAO();
    private VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private OfertaDAO ofertaDAO = new OfertaDAO();
    private GamaDAO gamaDAO = new GamaDAO();
    private RecepcionDAO recepcionDAO = new RecepcionDAO();

    private MenuAdmin menu;

    // 🔥 CONTROL DE MODO
    private boolean modoVisualizando = false;

    JTextField txtMatricula = new JTextField(15);
    JTextField txtCedula = new JTextField(15);
    JTextField txtOferta = new JTextField(15);

    JTextField txtNombre = new JTextField(15);
    JTextField txtVehiculo = new JTextField(15);

    JTextField txtFechaReserva = new JTextField(15);

    JSpinner txtSalida = new JSpinner(new SpinnerDateModel());
    JSpinner txtEntrada = new JSpinner(new SpinnerDateModel());

    JTextField txtDias = new JTextField(15);
    JTextField txtTotal = new JTextField(15);
    JTextField txtObs = new JTextField(15);

    JLabel lblEstado = new JLabel("Creando");

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RegistroReserva(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Reservas");
        setSize(600, 500);
        setLocationRelativeTo(null);

        txtSalida.setEditor(new JSpinner.DateEditor(txtSalida, "yyyy-MM-dd"));
        txtEntrada.setEditor(new JSpinner.DateEditor(txtEntrada, "yyyy-MM-dd"));

        JPanel contenedor = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0,2,5,5));

        form.add(new JLabel("Estado")); form.add(lblEstado);
        form.add(new JLabel("Matrícula")); form.add(txtMatricula);
        form.add(new JLabel("Cédula")); form.add(txtCedula);
        form.add(new JLabel("Oferta")); form.add(txtOferta);

        form.add(new JLabel("Cliente")); form.add(txtNombre);
        form.add(new JLabel("Vehículo")); form.add(txtVehiculo);

        form.add(new JLabel("Fecha Reserva")); form.add(txtFechaReserva);
        form.add(new JLabel("Fecha Salida")); form.add(txtSalida);
        form.add(new JLabel("Fecha Entrada")); form.add(txtEntrada);

        form.add(new JLabel("Días")); form.add(txtDias);
        form.add(new JLabel("Total")); form.add(txtTotal);
        form.add(new JLabel("Observación")); form.add(txtObs);

        contenedor.add(new JScrollPane(form), BorderLayout.NORTH);

        modelo.setColumnIdentifiers(new String[]{
    "Matricula","Cedula","F.Reserva","Salida","Entrada","Dias","Total","Obs","Oferta"
});

        tabla.setModel(modelo);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                btnEliminar.setEnabled(true);
            }
        });

        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);

        txtNombre.setEnabled(false);
        txtVehiculo.setEnabled(false);
        txtFechaReserva.setEnabled(false);
        txtDias.setEnabled(false);
        txtTotal.setEnabled(false);

        txtFechaReserva.setText(LocalDate.now().toString());

        eventos();
        cargarTabla();
    }

    private void eventos(){

       txtMatricula.addActionListener(e -> {

    if(modoVisualizando) return;

    try{

        String mat = txtMatricula.getText();

        // 🔥 BUSCAR RESERVA EXISTENTE
        for(Reserva r : dao.listar()){
            if(r.getMatricula().equals(mat)){

                // 👉 CARGAR COMO SI HICIERAS CLICK
                modoVisualizando = true;

                txtCedula.setText(r.getCedula());
                txtFechaReserva.setText(r.getFechaReserva());

                txtSalida.setValue(java.sql.Date.valueOf(r.getFechaSalida()));
                txtEntrada.setValue(java.sql.Date.valueOf(r.getFechaEntrada()));

                txtDias.setText(String.valueOf(r.getDias()));
                txtTotal.setText(String.valueOf(r.getTotal()));
                txtObs.setText(r.getObservacion());

                bloquearFormulario();
                return;
            }
        }

        // 🔥 SI NO EXISTE → CREAR NORMAL
        Vehiculo v = vehiculoDAO.buscar(mat);

        if(v != null){

            if(!v.isStatus()){
                JOptionPane.showMessageDialog(this,"Vehículo ya reservado");
                txtMatricula.setText("");
                return;
            }

            txtVehiculo.setText(v.getMarca()+" "+v.getModelo());
            txtCedula.requestFocus();

        } else {
            JOptionPane.showMessageDialog(this,"Vehículo no existe");
            txtMatricula.setText("");
        }

    }catch(Exception ignored){}
});

        txtCedula.addActionListener(e -> {
            if(modoVisualizando) return;

            try{
                Cliente c = clienteDAO.buscar(txtCedula.getText());

                if(c != null){
                    txtNombre.setText(c.getNombre()+" "+c.getApellido());
                    txtOferta.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this,"Cliente no existe");
                }

            }catch(Exception ignored){}
        });

        txtOferta.addActionListener(e -> {
    if(modoVisualizando) return;

    try {
        if (!txtOferta.getText().isEmpty()) {
            Oferta o = ofertaDAO.buscar(txtOferta.getText());

            if (o == null) {
                JOptionPane.showMessageDialog(this, "La oferta no existe");
                txtOferta.setText("");
                return;
            }

            // 🔥 VALIDACIÓN CRÍTICA:
            // Verificamos si la matrícula de la oferta coincide con la del txtMatricula
            String matriculaVehiculo = txtMatricula.getText();
            if (!o.getMatricula().equals(matriculaVehiculo)) {
                JOptionPane.showMessageDialog(this, "Esta oferta no pertenece a este vehículo.\n"
                        + "Vehículo requerido: " + o.getMatricula());
                txtOferta.setText("");
                return;
            }
            
            JOptionPane.showMessageDialog(this, "Oferta aplicada correctamente");
        }
        txtSalida.requestFocus();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error validando la oferta");
    }
});

     txtEntrada.addChangeListener(e -> {

    if(modoVisualizando) return;

    // 🔥 SOLO CALCULAR SI YA HAY MATRÍCULA
    if(txtMatricula.getText().isEmpty()) return;

    calcular();
});
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseClicked(java.awt.event.MouseEvent evt) {
        modoVisualizando = true; 

        int fila = tabla.getSelectedRow();

        txtMatricula.setText(modelo.getValueAt(fila, 0).toString());
        txtCedula.setText(modelo.getValueAt(fila, 1).toString());
        txtFechaReserva.setText(modelo.getValueAt(fila, 2).toString());

        txtSalida.setValue(java.sql.Date.valueOf(modelo.getValueAt(fila, 3).toString()));
        txtEntrada.setValue(java.sql.Date.valueOf(modelo.getValueAt(fila, 4).toString()));

        txtDias.setText(modelo.getValueAt(fila, 5).toString());
        txtTotal.setText(modelo.getValueAt(fila, 6).toString());
        txtObs.setText(modelo.getValueAt(fila, 7).toString());
        
        // 👉 PASO A: Si tu tabla tiene la oferta, cárgala. Si no, déjalo vacío para que cargue normal
        try { txtOferta.setText(modelo.getValueAt(fila, 8).toString()); } catch(Exception e) { txtOferta.setText(""); }

        // 👉 PASO B: Disparar la búsqueda de nombres
        cargarDatosRelacionados();

        bloquearFormulario();
    }
});
    }

    private void bloquearFormulario(){
        txtMatricula.setEnabled(false);
        txtCedula.setEnabled(false);
        txtOferta.setEnabled(false);
        txtSalida.setEnabled(false);
        txtEntrada.setEnabled(false);
        txtObs.setEnabled(false);
    }

    private void desbloquearFormulario(){
        txtMatricula.setEnabled(true);
        txtCedula.setEnabled(true);
        txtOferta.setEnabled(true);
        txtSalida.setEnabled(true);
        txtEntrada.setEnabled(true);
        txtObs.setEnabled(true);
    }

    private void calcular(){
        try{
            LocalDate hoy = LocalDate.now();

            LocalDate salida = ((java.util.Date) txtSalida.getValue())
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            LocalDate entrada = ((java.util.Date) txtEntrada.getValue())
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            if(salida.isBefore(hoy)){
                JOptionPane.showMessageDialog(this,"La fecha de salida no puede ser menor que hoy");
                return;
            }

            if(entrada.isBefore(salida)){
                JOptionPane.showMessageDialog(this,"La fecha de entrada no puede ser menor que la salida");
                return;
            }

            long dias = ChronoUnit.DAYS.between(salida, entrada);
            txtDias.setText(String.valueOf(dias));

            double precio;

            if(!txtOferta.getText().isEmpty()){
                Oferta o = ofertaDAO.buscar(txtOferta.getText());
                if(o == null){
                    JOptionPane.showMessageDialog(this,"Oferta no existe");
                    return;
                }
                precio = o.getPrecio();
            } else {
                Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());
                Gama g = gamaDAO.buscarPorId(v.getGama());
                precio = g.getPrecio();
            }

            txtTotal.setText(String.valueOf(precio * dias));

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error en fechas");
        }
    }

    private void cargarTabla(){
        try{
            modelo.setRowCount(0);

            for(Reserva r: dao.listar()) {

                boolean yaRecibido = false;

                for (Recepcion rec : recepcionDAO.listar()) {
                    if (rec.getMatricula().equals(r.getMatricula())) {
                        yaRecibido = true;
                        break;
                    }
                }

                if (!yaRecibido) {
                    modelo.addRow(new Object[]{
                            r.getMatricula(),
                            r.getCedula(),
                            r.getFechaReserva(),
                            r.getFechaSalida(),
                            r.getFechaEntrada(),
                            r.getDias(),
                            r.getTotal(),
                            r.getObservacion(),
                            r.getOferta()
                    }); // <-- Aquí estaba el error, faltaba cerrar el Object[] y el addRow
                }
            }

        }catch(Exception ignored){}
    }

    @Override
    protected void limpiarCampos() {

        modoVisualizando = false; // 🔥 volver a modo normal

        txtMatricula.setText("");
        txtCedula.setText("");
        txtOferta.setText("");
        txtNombre.setText("");
        txtVehiculo.setText("");

       modoVisualizando = true; // 🔥 evitar triggers

txtSalida.setValue(new java.util.Date());
txtEntrada.setValue(new java.util.Date());

modoVisualizando = false; // 🔥 volver normal

        txtDias.setText("");
        txtTotal.setText("");
        txtObs.setText("");
        txtFechaReserva.setText(LocalDate.now().toString());

        desbloquearFormulario();

        tabla.clearSelection();
        btnEliminar.setEnabled(false);

        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {

        if(txtMatricula.getText().isEmpty()
                || txtCedula.getText().isEmpty()
                || txtSalida.getValue() == null
                || txtEntrada.getValue() == null){

            JOptionPane.showMessageDialog(this,"Campos obligatorios");
            return false;
        }

        return true;
    }

    @Override
    protected void guardarRegistro() {
        try{
            Reserva r = new Reserva(
                    txtMatricula.getText(),
                    txtCedula.getText(),
                    txtOferta.getText(),
                    txtFechaReserva.getText(),
                    txtSalida.getValue().toString(),
                    txtEntrada.getValue().toString(),
                    txtObs.getText(),
                    Integer.parseInt(txtDias.getText()),
                    Double.parseDouble(txtTotal.getText())
            );

            dao.guardar(r);

            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());

            if(v != null){
                v.setStatus(false);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this,"Reserva guardada");
            limpiarCampos();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error");
        }
    }

    @Override
    protected void eliminarRegistro() {
        try{

            int fila = tabla.getSelectedRow();

            if(fila == -1){
                JOptionPane.showMessageDialog(this,"Seleccione una reserva");
                return;
            }

            int op = JOptionPane.showConfirmDialog(this,"¿Eliminar?");
            if(op != JOptionPane.YES_OPTION) return;

            String matricula = modelo.getValueAt(fila, 0).toString();

            Vehiculo v = vehiculoDAO.buscar(matricula);

            if(v != null){
                v.setStatus(true);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            dao.eliminar(matricula);

            JOptionPane.showMessageDialog(this,"Eliminado");
            limpiarCampos();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error");
        }
    }

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
    
    private void cargarDatosRelacionados() {
    try {
        // Buscar y mostrar nombre del Vehículo
        Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());
        if (v != null) txtVehiculo.setText(v.getMarca() + " " + v.getModelo());

        // Buscar y mostrar nombre del Cliente
        Cliente c = clienteDAO.buscar(txtCedula.getText());
        if (c != null) txtNombre.setText(c.getNombre() + " " + c.getApellido());
        
        // La oferta ya se carga en el txtOferta desde la tabla (paso siguiente)
    } catch (Exception ignored) {}
}
}