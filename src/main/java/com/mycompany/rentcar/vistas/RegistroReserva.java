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

    private MenuAdmin menu;

    JTextField txtMatricula = new JTextField(15);
    JTextField txtCedula = new JTextField(15);
    JTextField txtOferta = new JTextField(15);

    JTextField txtNombre = new JTextField(15);
    JTextField txtVehiculo = new JTextField(15);

    JTextField txtFechaReserva = new JTextField(15);
    JTextField txtSalida = new JTextField(15);
    JTextField txtEntrada = new JTextField(15);

    JTextField txtDias = new JTextField(15);
    JTextField txtTotal = new JTextField(15);
    JTextField txtObs = new JTextField(15);

    JLabel lblEstado = new JLabel("Creando");

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel() {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // 🔥 DESACTIVA EDICIÓN TOTAL
    }
};

   public RegistroReserva(MenuAdmin m) {
    super();
    this.menu = m;

    setTitle("Reservas");
    setSize(600, 500);
    setLocationRelativeTo(null);

    // 🔥 PANEL PRINCIPAL (NO TOCAR EL LAYOUT DEL BASE)
    JPanel contenedor = new JPanel(new BorderLayout());

    // ===== FORMULARIO ARRIBA =====
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

    JScrollPane scrollForm = new JScrollPane(form);
scrollForm.setPreferredSize(new Dimension(600, 220));
scrollForm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

contenedor.add(scrollForm, BorderLayout.NORTH);

    // ===== TABLA ABAJO =====
    modelo.setColumnIdentifiers(new String[]{
            "Matricula","Cedula","Dias","Total"
    });

    tabla.setModel(modelo);

    // 🔥 ACTIVAR BOTÓN ELIMINAR
    tabla.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            if (tabla.getSelectedRow() != -1) {
                btnEliminar.setEnabled(true);
            }
        }
    });

    JScrollPane scrollTabla = new JScrollPane(tabla);
    scrollTabla.setPreferredSize(new Dimension(600, 200));

    contenedor.add(scrollTabla, BorderLayout.CENTER);

    // 🔥 ESTA ES LA CLAVE
    add(contenedor, BorderLayout.CENTER);

    // ===== BLOQUEOS =====
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

        
        txtOferta.addActionListener(e -> {
    try {
        if (!txtOferta.getText().isEmpty()) {

            Oferta o = ofertaDAO.buscar(txtOferta.getText());

            if (o != null) {
                JOptionPane.showMessageDialog(this, "Oferta válida");
                 
            } else {
                JOptionPane.showMessageDialog(this, "Oferta no existe");
                txtOferta.setText("");
                txtTotal.setText("");
            }
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error buscando oferta");
    }
});
        txtMatricula.addActionListener(e -> {
            try{
                Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText());

                if(v != null){
                    txtVehiculo.setText(v.getMarca()+" "+v.getModelo());

                    if(!v.isStatus()){
                        JOptionPane.showMessageDialog(this,"Vehículo ya reservado");
                    }

                } else {
                    JOptionPane.showMessageDialog(this,"Vehículo no existe");
                }

            }catch(Exception ignored){}
        });

        txtCedula.addActionListener(e -> {
            try{
                Cliente c = clienteDAO.buscar(txtCedula.getText());

                if(c != null){
                    txtNombre.setText(c.getNombre()+" "+c.getApellido());
                } else {
                    JOptionPane.showMessageDialog(this,"Cliente no existe");
                }

            }catch(Exception ignored){}
        });

        txtEntrada.addActionListener(e -> calcular());
    }

    private void calcular(){
        try{
            LocalDate hoy = LocalDate.now();
            LocalDate salida = LocalDate.parse(txtSalida.getText());
            LocalDate entrada = LocalDate.parse(txtEntrada.getText());

            if(salida.isBefore(hoy)){
                JOptionPane.showMessageDialog(this,"Salida menor que hoy");
                return;
            }

            if(entrada.isBefore(salida)){
                JOptionPane.showMessageDialog(this,"Entrada menor que salida");
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

    for (Recepcion rec : new RecepcionDAO().listar()) {
        if (rec.getMatricula().equals(r.getMatricula())) {
            yaRecibido = true;
            break;
        }
    }

    if (!yaRecibido) {
        modelo.addRow(new Object[]{
                r.getMatricula(),
                r.getCedula(),
                r.getDias(),
                r.getTotal()
        });
    }
}

        }catch(Exception ignored){}
    }

    @Override
    protected void limpiarCampos() {
        txtMatricula.setText("");
        txtCedula.setText("");
        txtOferta.setText("");
        txtNombre.setText("");
        txtVehiculo.setText("");
        txtSalida.setText("");
        txtEntrada.setText("");
        txtDias.setText("");
        txtTotal.setText("");
        txtObs.setText("");
        txtFechaReserva.setText(LocalDate.now().toString());

        tabla.clearSelection();
        btnEliminar.setEnabled(false);

        cargarTabla();
    }

    @Override
    protected boolean validarCampos() {
        if(txtMatricula.getText().isEmpty()
                || txtCedula.getText().isEmpty()
                || txtSalida.getText().isEmpty()
                || txtEntrada.getText().isEmpty()){

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
                    txtSalida.getText(),
                    txtEntrada.getText(),
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
}