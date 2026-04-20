package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.*;
import com.mycompany.rentcar.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class RegistroReserva extends MantenimientoBase {

    private ReservaDAO    dao           = new ReservaDAO();
    private VehiculoDAO   vehiculoDAO   = new VehiculoDAO();
    private ClienteDAO    clienteDAO    = new ClienteDAO();
    private OfertaDAO     ofertaDAO     = new OfertaDAO();
    private GamaDAO       gamaDAO       = new GamaDAO();
    private RecepcionDAO  recepcionDAO  = new RecepcionDAO();

    private MenuAdmin menu;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ─── Campos del formulario ───────────────────────────────────────────────

    JTextField txtIdReserva    = new JTextField(15);  // solo lectura, autogenerado
    JTextField txtMatricula    = new JTextField(15);
    JTextField txtCedula       = new JTextField(15);
    JTextField txtOferta       = new JTextField(15);
    JTextField txtNombre       = new JTextField(15);  // solo lectura
    JTextField txtVehiculo     = new JTextField(15);  // solo lectura
    JTextField txtFechaReserva = new JTextField(15);  // solo lectura, hoy
    JTextField txtDias         = new JTextField(15);  // solo lectura, calculado
    JTextField txtTotal        = new JTextField(15);  // solo lectura, calculado
    JTextField txtObs          = new JTextField(15);

    JSpinner spnSalida  = new JSpinner(new SpinnerDateModel());
    JSpinner spnEntrada = new JSpinner(new SpinnerDateModel());

    JLabel lblEstado = new JLabel("Creando");

    // ─── Tabla ───────────────────────────────────────────────────────────────

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    // ─── Constructor ─────────────────────────────────────────────────────────

    public RegistroReserva(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Reservas");
        setSize(660, 600);
        setLocationRelativeTo(null);

        spnSalida.setEditor(new JSpinner.DateEditor(spnSalida,   "yyyy-MM-dd"));
        spnEntrada.setEditor(new JSpinner.DateEditor(spnEntrada, "yyyy-MM-dd"));

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));

        form.add(new JLabel("Estado"));        form.add(lblEstado);
        form.add(new JLabel("ID Reserva"));    form.add(txtIdReserva);
        form.add(new JLabel("Matrícula"));     form.add(txtMatricula);
        form.add(new JLabel("Vehículo"));      form.add(txtVehiculo);
        form.add(new JLabel("Cédula"));        form.add(txtCedula);
        form.add(new JLabel("Cliente"));       form.add(txtNombre);
        form.add(new JLabel("Oferta"));        form.add(txtOferta);
        form.add(new JLabel("Fecha Reserva")); form.add(txtFechaReserva);
        form.add(new JLabel("Fecha Salida"));  form.add(spnSalida);
        form.add(new JLabel("Fecha Entrada")); form.add(spnEntrada);
        form.add(new JLabel("Días"));          form.add(txtDias);
        form.add(new JLabel("Total RD$"));     form.add(txtTotal);
        form.add(new JLabel("Observación"));   form.add(txtObs);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setPreferredSize(new Dimension(640, 280));

        // ===== TABLA =====
        modelo.setColumnIdentifiers(new String[]{
            "ID", "Matrícula", "Cédula", "F.Reserva", "Salida", "Entrada", "Días", "Total", "Obs"
        });
        tabla.setModel(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                btnEliminar.setEnabled(true);
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);

        JPanel contenedor = new JPanel(new BorderLayout(0, 5));
        contenedor.add(scrollForm,  BorderLayout.NORTH);
        contenedor.add(scrollTabla, BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);

        // ===== CAMPOS SOLO LECTURA =====
        setReadOnly(txtIdReserva);
        setReadOnly(txtNombre);
        setReadOnly(txtVehiculo);
        setReadOnly(txtFechaReserva);
        setReadOnly(txtDias);
        setReadOnly(txtTotal);

        txtFechaReserva.setText(LocalDate.now().format(FMT));

        generarNuevoId();
        eventos();
        cargarTabla();
    }

    // ─── Utilidad campo solo lectura ─────────────────────────────────────────

    private void setReadOnly(JTextField campo) {
        campo.setEditable(false);
        campo.setBackground(new Color(230, 230, 230));
    }

    // ─── ID Autogenerado ─────────────────────────────────────────────────────

    private void generarNuevoId() {
        try {
            List<Reserva> lista = dao.listar();
            int maxId = 0;
            for (Reserva r : lista) {
                if (r.getIdReserva() > maxId) maxId = r.getIdReserva();
            }
            txtIdReserva.setText(String.valueOf(maxId + 1));
        } catch (Exception e) {
            txtIdReserva.setText("1");
        }
    }

    // ─── Eventos ─────────────────────────────────────────────────────────────

    private void eventos() {

        txtMatricula.addActionListener(e -> {
            buscarVehiculo();
            txtCedula.requestFocus();
        });

        txtCedula.addActionListener(e -> {
            buscarCliente();
            txtOferta.requestFocus();
        });

        txtOferta.addActionListener(e -> {
            validarOferta();
            spnSalida.requestFocus();
        });

        spnSalida.addChangeListener(e  -> calcular());
        spnEntrada.addChangeListener(e -> calcular());

        txtObs.addActionListener(e -> btnGuardar.requestFocus());

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) cargarDesdeTabla(fila);
            }
        });
    }

    // ─── Búsqueda de Vehículo ────────────────────────────────────────────────

    private void buscarVehiculo() {
        String mat = txtMatricula.getText().trim();
        if (mat.isEmpty()) return;
        try {
            Vehiculo v = vehiculoDAO.buscar(mat);

            if (v == null) {
                JOptionPane.showMessageDialog(this, "Vehículo no existe");
                txtMatricula.setText("");
                txtVehiculo.setText("");
                return;
            }

            if (!v.isStatus()) {
                JOptionPane.showMessageDialog(this, "El vehículo ya está reservado");
                txtMatricula.setText("");
                txtVehiculo.setText("");
                return;
            }

            txtVehiculo.setText(v.getMarca() + " " + v.getModelo());

        } catch (Exception ignored) {}
    }

    // ─── Búsqueda de Cliente ─────────────────────────────────────────────────

    private void buscarCliente() {
        String ced = txtCedula.getText().trim();
        if (ced.isEmpty()) return;
        try {
            Cliente c = clienteDAO.buscar(ced);

            if (c != null) {
                txtNombre.setText(c.getNombre() + " " + c.getApellido());
            } else {
                JOptionPane.showMessageDialog(this, "Cliente no existe");
                txtCedula.setText("");
                txtNombre.setText("");
            }
        } catch (Exception ignored) {}
    }

    // ─── Validación de Oferta ────────────────────────────────────────────────

    private void validarOferta() {
        String idOferta = txtOferta.getText().trim();
        if (idOferta.isEmpty()) return;

        String mat = txtMatricula.getText().trim();
        if (mat.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero ingrese la matrícula del vehículo antes de seleccionar una oferta.");
            txtOferta.setText("");
            return;
        }

        try {
            Oferta o = ofertaDAO.buscar(idOferta);

            if (o == null) {
                JOptionPane.showMessageDialog(this, "La oferta no existe");
                txtOferta.setText("");
                return;
            }

            if (!o.getMatricula().equalsIgnoreCase(mat)) {
                JOptionPane.showMessageDialog(this,
                    "La oferta #" + idOferta + " no pertenece al vehículo " + mat
                    + ".\n(Esta oferta es para: " + o.getMatricula() + ")");
                txtOferta.setText("");
                return;
            }

            calcular();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar la oferta");
        }
    }

    // ─── Cálculo de días y total ─────────────────────────────────────────────

    /**
     * Reglas de fechas según el documento y el maestro:
     *
     *  1. Fecha Salida  >= Fecha Reserva (hoy)
     *  2. Fecha Entrada >= Fecha Reserva (hoy)
     *  3. Fecha Entrada >= Fecha Salida
     *  4. Si Salida == Entrada → se considera 1 día (mismo día cuenta como un día)
     *     Si Salida < Entrada  → días = (Entrada - Salida) + 1
     *     Ejemplo: sale el 20, entra el 22 → 3 días (20, 21, 22)
     */
    private void calcular() {
        try {
            LocalDate hoy     = LocalDate.now();
            LocalDate salida  = spinnerALocalDate(spnSalida);
            LocalDate entrada = spinnerALocalDate(spnEntrada);

            // Validación silenciosa mientras el usuario navega:
            // Si las fechas no tienen sentido todavía, simplemente limpia sin molestar.
            if (salida.isBefore(hoy) || entrada.isBefore(hoy) || entrada.isBefore(salida)) {
                txtDias.setText("");
                txtTotal.setText("");
                return;
            }

            // ✅ Mismo día = 1 día. Días normales = diferencia + 1.
            long dias = ChronoUnit.DAYS.between(salida, entrada) + 1;
            txtDias.setText(String.valueOf(dias));

            double precio = obtenerPrecio();
            txtTotal.setText(String.format("%.2f", precio * dias));

        } catch (Exception ignored) {
            txtDias.setText("");
            txtTotal.setText("");
        }
    }

    private LocalDate spinnerALocalDate(JSpinner spinner) {
        Date fecha = (Date) spinner.getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private double obtenerPrecio() throws Exception {
        String idOferta = txtOferta.getText().trim();
        String mat      = txtMatricula.getText().trim();

        if (!idOferta.isEmpty()) {
            Oferta o = ofertaDAO.buscar(idOferta);
            if (o != null && o.getMatricula().equalsIgnoreCase(mat)) {
                return o.getPrecio();
            }
        }

        Vehiculo v = vehiculoDAO.buscar(mat);
        if (v == null) return 0;
        Gama g = gamaDAO.buscarPorId(v.getGama());
        if (g == null) return 0;
        return g.getPrecio();
    }

    // ─── Cargar desde tabla (modo solo lectura) ───────────────────────────────

    private void cargarDesdeTabla(int fila) {
        txtIdReserva.setText(modelo.getValueAt(fila, 0).toString());
        txtMatricula.setText(modelo.getValueAt(fila, 1).toString());
        txtCedula.setText(modelo.getValueAt(fila, 2).toString());
        txtFechaReserva.setText(modelo.getValueAt(fila, 3).toString());
        txtDias.setText(modelo.getValueAt(fila, 6).toString());
        txtTotal.setText(modelo.getValueAt(fila, 7).toString());
        txtObs.setText(modelo.getValueAt(fila, 8).toString());

        try {
            LocalDate lSalida  = LocalDate.parse(modelo.getValueAt(fila, 4).toString(), FMT);
            LocalDate lEntrada = LocalDate.parse(modelo.getValueAt(fila, 5).toString(), FMT);
            spnSalida.setValue(Date.from(lSalida.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            spnEntrada.setValue(Date.from(lEntrada.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } catch (Exception ignored) {}

        try {
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText().trim());
            if (v != null) txtVehiculo.setText(v.getMarca() + " " + v.getModelo());
        } catch (Exception ignored) {}

        try {
            Cliente c = clienteDAO.buscar(txtCedula.getText().trim());
            if (c != null) txtNombre.setText(c.getNombre() + " " + c.getApellido());
        } catch (Exception ignored) {}

        lblEstado.setText("Visualizando");
        bloquearFormulario();
        btnGuardar.setEnabled(false);
    }

    // ─── Bloqueo / Desbloqueo ─────────────────────────────────────────────────

    private void bloquearFormulario() {
        txtMatricula.setEditable(false);
        txtCedula.setEditable(false);
        txtOferta.setEditable(false);
        txtObs.setEditable(false);
        spnSalida.setEnabled(false);
        spnEntrada.setEnabled(false);
        Color gris = new Color(230, 230, 230);
        txtMatricula.setBackground(gris);
        txtCedula.setBackground(gris);
        txtOferta.setBackground(gris);
        txtObs.setBackground(gris);
    }

    private void desbloquearFormulario() {
        txtMatricula.setEditable(true);
        txtCedula.setEditable(true);
        txtOferta.setEditable(true);
        txtObs.setEditable(true);
        spnSalida.setEnabled(true);
        spnEntrada.setEnabled(true);
        txtMatricula.setBackground(Color.WHITE);
        txtCedula.setBackground(Color.WHITE);
        txtOferta.setBackground(Color.WHITE);
        txtObs.setBackground(Color.WHITE);
    }

    // ─── Cargar tabla ────────────────────────────────────────────────────────

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            List<Recepcion> recepciones = recepcionDAO.listar();

            for (Reserva r : dao.listar()) {
                boolean yaRecibido = recepciones.stream()
                    .anyMatch(rec -> rec.getMatricula().equalsIgnoreCase(r.getMatricula()));

                if (!yaRecibido) {
                    modelo.addRow(new Object[]{
                        r.getIdReserva(),
                        r.getMatricula(),
                        r.getCedula(),
                        r.getFechaReserva(),
                        r.getFechaSalida(),
                        r.getFechaEntrada(),
                        r.getDias(),
                        r.getTotal(),
                        r.getObservacion()
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    // ─── limpiarCampos ───────────────────────────────────────────────────────

    @Override
    protected void limpiarCampos() {
        txtMatricula.setText("");
        txtCedula.setText("");
        txtOferta.setText("");
        txtNombre.setText("");
        txtVehiculo.setText("");
        txtDias.setText("");
        txtTotal.setText("");
        txtObs.setText("");
        txtFechaReserva.setText(LocalDate.now().format(FMT));

        Date hoy = new Date();
        spnSalida.setValue(hoy);
        spnEntrada.setValue(hoy);

        lblEstado.setText("Creando");
        desbloquearFormulario();
        btnGuardar.setEnabled(true);

        tabla.clearSelection();
        btnEliminar.setEnabled(false);

        generarNuevoId();
        cargarTabla();
    }

    // ─── validarCampos ───────────────────────────────────────────────────────

    @Override
    protected boolean validarCampos() {

        if (txtMatricula.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La matrícula es obligatoria");
            return false;
        }
        if (txtCedula.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La cédula es obligatoria");
            return false;
        }
        if (txtDias.getText().trim().isEmpty() || txtTotal.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Seleccione las fechas de Salida y Entrada correctamente");
            return false;
        }

        LocalDate hoy     = LocalDate.now();
        LocalDate salida  = spinnerALocalDate(spnSalida);
        LocalDate entrada = spinnerALocalDate(spnEntrada);

        // ✅ Fecha Salida no puede ser anterior a Fecha Reserva (hoy)
        if (salida.isBefore(hoy)) {
            JOptionPane.showMessageDialog(this,
                "La Fecha de Salida no puede ser anterior a la fecha de reserva (" + hoy.format(FMT) + ")");
            return false;
        }

        // ✅ Fecha Entrada no puede ser anterior a Fecha Reserva (hoy)
        if (entrada.isBefore(hoy)) {
            JOptionPane.showMessageDialog(this,
                "La Fecha de Entrada no puede ser anterior a la fecha de reserva (" + hoy.format(FMT) + ")");
            return false;
        }

        // ✅ Fecha Entrada no puede ser anterior a Fecha Salida
        // (igual sí se permite → mismo día = 1 día)
        if (entrada.isBefore(salida)) {
            JOptionPane.showMessageDialog(this,
                "La Fecha de Entrada no puede ser anterior a la Fecha de Salida");
            return false;
        }

        // ✅ Revalidar oferta si se ingresó
        String idOferta = txtOferta.getText().trim();
        if (!idOferta.isEmpty()) {
            try {
                Oferta o = ofertaDAO.buscar(idOferta);
                if (o == null) {
                    JOptionPane.showMessageDialog(this, "La oferta ingresada no existe");
                    return false;
                }
                if (!o.getMatricula().equalsIgnoreCase(txtMatricula.getText().trim())) {
                    JOptionPane.showMessageDialog(this,
                        "La oferta no corresponde al vehículo seleccionado");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    // ─── guardarRegistro ─────────────────────────────────────────────────────

    @Override
    protected void guardarRegistro() {
        try {
            String strSalida  = spinnerALocalDate(spnSalida).format(FMT);
            String strEntrada = spinnerALocalDate(spnEntrada).format(FMT);

            Reserva r = new Reserva(
                Integer.parseInt(txtIdReserva.getText()),
                txtMatricula.getText().trim(),
                txtCedula.getText().trim(),
                txtOferta.getText().trim(),
                txtFechaReserva.getText().trim(),
                strSalida,
                strEntrada,
                txtObs.getText().trim(),
                Integer.parseInt(txtDias.getText().trim()),
                Double.parseDouble(txtTotal.getText().trim())
            );

            dao.guardar(r);

            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText().trim());
            if (v != null) {
                v.setStatus(false);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this,
                "Reserva #" + r.getIdReserva() + " guardada correctamente");
            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la reserva");
        }
    }

    // ─── eliminarRegistro ────────────────────────────────────────────────────

    @Override
    protected void eliminarRegistro() {
        try {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this,
                    "Seleccione una reserva de la tabla para eliminar");
                return;
            }

            int    idReserva = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            String matricula = modelo.getValueAt(fila, 1).toString();

            int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar reserva #" + idReserva + "?\n"
                + "El vehículo " + matricula + " quedará disponible nuevamente.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            Vehiculo v = vehiculoDAO.buscar(matricula);
            if (v != null) {
                v.setStatus(true);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            dao.eliminar(idReserva);

            JOptionPane.showMessageDialog(this,
                "Reserva eliminada. Vehículo " + matricula + " disponible nuevamente.");
            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar la reserva");
        }
    }

    // ─── volver ──────────────────────────────────────────────────────────────

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
}
