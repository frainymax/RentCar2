package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.*;
import com.mycompany.rentcar.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class RegistroRecepcion extends MantenimientoBase {

    private RecepcionDAO recepcionDAO = new RecepcionDAO();
    private ReservaDAO   reservaDAO   = new ReservaDAO();
    private VehiculoDAO  vehiculoDAO  = new VehiculoDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String originalIdRecepcion = ""; // para saber si estamos modificando
    private MenuAdmin menu;

    // ─── Campos del formulario ───────────────────────────────────────────────

    // ID Recepción: autogenerado, solo lectura
    JTextField txtIdRecepcion  = new JTextField(15);

    // ID Reserva: el usuario lo escribe → se busca y carga todo lo demás
    JTextField txtIdReserva    = new JTextField(15);

    // Campos que se cargan automáticamente desde la reserva (solo lectura)
    JTextField txtMatricula    = new JTextField(15);
    JTextField txtVehiculo     = new JTextField(15);
    JTextField txtCliente      = new JTextField(15);
    JTextField txtFechaEntrada = new JTextField(15); // Fecha Entrada de la reserva

    // Campos editables por el usuario
    JSpinner   spFechaRecepcion = new JSpinner(new SpinnerDateModel());
    JTextField txtObs           = new JTextField(15);

    JLabel lblEstado = new JLabel("Creando");

    // ─── Tabla ───────────────────────────────────────────────────────────────

    private JTable tabla = new JTable();
    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };

    // ─── Constructor ─────────────────────────────────────────────────────────

    public RegistroRecepcion(MenuAdmin m) {
        super();
        this.menu = m;

        setTitle("Recepción de Vehículos");
        setSize(640, 540);
        setLocationRelativeTo(null);

        // Formato spinner
        spFechaRecepcion.setEditor(new JSpinner.DateEditor(spFechaRecepcion, "yyyy-MM-dd"));

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));

        form.add(new JLabel("Estado"));           form.add(lblEstado);
        form.add(new JLabel("ID Recepción"));      form.add(txtIdRecepcion);   // autogenerado
        form.add(new JLabel("ID Reserva"));        form.add(txtIdReserva);     // el usuario escribe esto
        form.add(new JLabel("Matrícula"));         form.add(txtMatricula);     // autocompletado
        form.add(new JLabel("Vehículo"));          form.add(txtVehiculo);      // autocompletado
        form.add(new JLabel("Cliente"));           form.add(txtCliente);       // autocompletado
        form.add(new JLabel("Fecha Entrada"));     form.add(txtFechaEntrada);  // autocompletado
        form.add(new JLabel("Fecha Recepción"));   form.add(spFechaRecepcion); // editable
        form.add(new JLabel("Observación"));       form.add(txtObs);           // editable

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setPreferredSize(new Dimension(620, 230));

        // ===== TABLA =====
        modelo.setColumnIdentifiers(new String[]{
            "ID Rec.", "ID Res.", "Matrícula", "Fecha Entrada", "Fecha Recepción", "Obs"
        });
        tabla.setModel(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollTabla = new JScrollPane(tabla);

        // ===== LAYOUT =====
        JPanel contenedor = new JPanel(new BorderLayout(0, 5));
        contenedor.add(scrollForm,  BorderLayout.NORTH);
        contenedor.add(scrollTabla, BorderLayout.CENTER);
        add(contenedor, BorderLayout.CENTER);

        // ===== CAMPOS SOLO LECTURA =====
        setReadOnly(txtIdRecepcion);
        setReadOnly(txtMatricula);
        setReadOnly(txtVehiculo);
        setReadOnly(txtCliente);
        setReadOnly(txtFechaEntrada);

        generarNuevoIdRecepcion();
        eventos();
        cargarTabla();
    }

    // ─── Utilidad solo lectura ────────────────────────────────────────────────

    private void setReadOnly(JTextField campo) {
        campo.setEditable(false);
        campo.setBackground(new Color(230, 230, 230));
    }

    // ─── ID Recepción autogenerado ────────────────────────────────────────────

    /**
     * Lee todas las recepciones guardadas, toma el ID más alto y asigna ID+1.
     * Si no existe ninguna, asigna 1.
     */
    private void generarNuevoIdRecepcion() {
        try {
            List<Recepcion> lista = recepcionDAO.listar();
            int maxId = 0;
            for (Recepcion r : lista) {
                try {
                    int id = Integer.parseInt(r.getId());
                    if (id > maxId) maxId = id;
                } catch (NumberFormatException ignored) {}
            }
            txtIdRecepcion.setText(String.valueOf(maxId + 1));
        } catch (Exception e) {
            txtIdRecepcion.setText("1");
        }
    }

    // ─── Eventos ─────────────────────────────────────────────────────────────

    private void eventos() {

        // ID RESERVA: al presionar Enter busca la reserva y carga todos los datos
        txtIdReserva.addActionListener(e -> {
            buscarReservaPorId();
            spFechaRecepcion.requestFocus();
        });

        // También valida cuando el usuario sale del campo sin presionar Enter
        txtIdReserva.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!txtIdReserva.getText().trim().isEmpty()) {
                    buscarReservaPorId();
                }
            }
        });

        // OBSERVACIÓN: Enter mueve al botón Guardar
        txtObs.addActionListener(e -> btnGuardar.requestFocus());

        // CLICK TABLA: carga la recepción seleccionada en modo Modificando
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) cargarDesdeTabla(fila);
            }
        });
    }

    // ─── Buscar reserva por ID ────────────────────────────────────────────────

    /**
     * Flujo principal según el PDF:
     *  - El usuario escribe el ID de la Reserva
     *  - Si existe: carga matrícula, vehículo, cliente, fecha entrada (todo bloqueado)
     *  - Si no existe: avisa y limpia
     *
     * Además verifica si ya existe una recepción para esa reserva:
     *  - Si ya existe recepción: carga en modo "Modificando"
     *  - Si no existe: modo "Creando"
     */
    private void buscarReservaPorId() {
        String idReservaStr = txtIdReserva.getText().trim();
        if (idReservaStr.isEmpty()) return;

        try {
            int idReserva = Integer.parseInt(idReservaStr);

            // Buscar la reserva
            Reserva reserva = null;
            for (Reserva r : reservaDAO.listar()) {
                if (r.getIdReserva() == idReserva) {
                    reserva = r;
                    break;
                }
            }

            if (reserva == null) {
                JOptionPane.showMessageDialog(this,
                    "No existe ninguna reserva con ID #" + idReservaStr);
                limpiarDatosReserva();
                return;
            }

            // Cargar datos de la reserva en los campos (bloqueados)
            txtMatricula.setText(reserva.getMatricula());
            txtFechaEntrada.setText(reserva.getFechaEntrada());

            // Cargar nombre del vehículo
            try {
                Vehiculo v = vehiculoDAO.buscar(reserva.getMatricula());
                if (v != null) {
                    txtVehiculo.setText(v.getMarca() + " " + v.getModelo());
                }
            } catch (Exception ignored) {}

            // Cargar nombre del cliente
            try {
                com.mycompany.rentcar.dao.ClienteDAO clienteDAO =
                    new com.mycompany.rentcar.dao.ClienteDAO();
                com.mycompany.rentcar.modelo.Cliente c =
                    clienteDAO.buscar(reserva.getCedula());
                if (c != null) {
                    txtCliente.setText(c.getNombre() + " " + c.getApellido());
                }
            } catch (Exception ignored) {}

            // Verificar si ya existe una recepción para esta reserva
            Recepcion recepcionExistente = null;
            for (Recepcion rec : recepcionDAO.listar()) {
                if (rec.getMatricula().equalsIgnoreCase(reserva.getMatricula())) {
                    recepcionExistente = rec;
                    break;
                }
            }

            if (recepcionExistente != null) {
                // YA EXISTE → modo Modificando
                txtIdRecepcion.setText(recepcionExistente.getId());
                originalIdRecepcion = recepcionExistente.getId();

                // Cargar fecha de recepción en el spinner
                try {
                    LocalDate fechaRec = LocalDate.parse(recepcionExistente.getFechaRecepcion(), FMT);
                    spFechaRecepcion.setValue(
                        Date.from(fechaRec.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                } catch (Exception ignored) {}

                txtObs.setText(recepcionExistente.getObservacion() != null
                    ? recepcionExistente.getObservacion() : "");

                lblEstado.setText("Modificando");
                JOptionPane.showMessageDialog(this, "Modificando recepción existente");

            } else {
                // NO EXISTE → modo Creando
                originalIdRecepcion = "";
                generarNuevoIdRecepcion();
                spFechaRecepcion.setValue(new Date());
                txtObs.setText("");
                lblEstado.setText("Creando");
                JOptionPane.showMessageDialog(this, "Creando nueva recepción");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID de Reserva debe ser un número");
            txtIdReserva.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar la reserva");
        }
    }

    // ─── Cargar desde tabla ───────────────────────────────────────────────────

    /**
     * Al hacer click en la tabla, carga la recepción en modo Modificando.
     * El ID de Reserva y la Matrícula se bloquean (según el PDF: Id_Matricula no se puede modificar).
     */
    private void cargarDesdeTabla(int fila) {
        txtIdRecepcion.setText(modelo.getValueAt(fila, 0).toString());
        txtIdReserva.setText(modelo.getValueAt(fila, 1).toString());
        txtMatricula.setText(modelo.getValueAt(fila, 2).toString());
        txtFechaEntrada.setText(modelo.getValueAt(fila, 3).toString());
        txtObs.setText(modelo.getValueAt(fila, 5) != null
            ? modelo.getValueAt(fila, 5).toString() : "");

        // Cargar fecha recepción en el spinner
        try {
            LocalDate fechaRec = LocalDate.parse(modelo.getValueAt(fila, 4).toString(), FMT);
            spFechaRecepcion.setValue(
                Date.from(fechaRec.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } catch (Exception ignored) {}

        // Cargar datos descriptivos del vehículo
        try {
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText().trim());
            if (v != null) txtVehiculo.setText(v.getMarca() + " " + v.getModelo());
        } catch (Exception ignored) {}

        originalIdRecepcion = txtIdRecepcion.getText();
        lblEstado.setText("Modificando");
        estadoModificar();
    }

    // ─── Limpiar datos de reserva (si no se encontró) ────────────────────────

    private void limpiarDatosReserva() {
        txtMatricula.setText("");
        txtVehiculo.setText("");
        txtCliente.setText("");
        txtFechaEntrada.setText("");
    }

    // ─── Obtener fecha del spinner ────────────────────────────────────────────

    private String getFechaSpinner() {
        Date date = (Date) spFechaRecepcion.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FMT);
    }

    // ─── Cargar tabla ─────────────────────────────────────────────────────────

    private void cargarTabla() {
        modelo.setRowCount(0);
        try {
            for (Recepcion r : recepcionDAO.listar()) {
                modelo.addRow(new Object[]{
                    r.getId(),
                    r.getIdReserva(),        // ID de la reserva asociada
                    r.getMatricula(),
                    r.getFechaEntrada(),
                    r.getFechaRecepcion(),
                    r.getObservacion()
                });
            }
        } catch (Exception ignored) {}
    }

    // ─── limpiarCampos ────────────────────────────────────────────────────────

    @Override
    protected void limpiarCampos() {
        txtIdReserva.setText("");
        txtObs.setText("");

        limpiarDatosReserva();
        spFechaRecepcion.setValue(new Date());

        originalIdRecepcion = "";
        lblEstado.setText("Creando");

        txtIdReserva.setEditable(true);
        txtIdReserva.setBackground(Color.WHITE);

        tabla.clearSelection();
        generarNuevoIdRecepcion();
        cargarTabla();
    }

    // ─── validarCampos ────────────────────────────────────────────────────────

    @Override
    protected boolean validarCampos() {

        // ID Reserva obligatorio
        if (txtIdReserva.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el ID de la Reserva");
            return false;
        }

        // Debe haberse cargado la matrícula (reserva encontrada)
        if (txtMatricula.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe buscar una reserva válida ingresando el ID de Reserva");
            return false;
        }

        // Fecha Entrada debe estar cargada
        if (txtFechaEntrada.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No se encontró la Fecha de Entrada de la reserva");
            return false;
        }

        // Validar que Fecha Recepción >= Fecha Entrada de la reserva
        try {
            LocalDate fechaEntrada   = LocalDate.parse(txtFechaEntrada.getText().trim(), FMT);
            LocalDate fechaRecepcion = LocalDate.parse(getFechaSpinner(), FMT);

            if (fechaRecepcion.isBefore(fechaEntrada)) {
                JOptionPane.showMessageDialog(this,
                    "La Fecha de Recepción (" + getFechaSpinner() + ") no puede ser\n"
                    + "anterior a la Fecha de Entrada de la reserva ("
                    + txtFechaEntrada.getText() + ").");
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido");
            return false;
        }

        return true;
    }

    // ─── guardarRegistro ──────────────────────────────────────────────────────

    @Override
    protected void guardarRegistro() {
        try {
            Recepcion r = new Recepcion(
                txtIdRecepcion.getText(),       // ID recepción autogenerado
                txtIdReserva.getText().trim(),  // ID reserva asociada
                txtMatricula.getText().trim(),  // matrícula (cargada de la reserva)
                txtFechaEntrada.getText().trim(),
                getFechaSpinner(),
                txtObs.getText().trim()
            );

            recepcionDAO.guardar(r, originalIdRecepcion);

            // ✅ Cambiar status del vehículo a DISPONIBLE (ya fue recibido)
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText().trim());
            if (v != null) {
                v.setStatus(true);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            JOptionPane.showMessageDialog(this,
                "Recepción #" + r.getId() + " guardada correctamente");
            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la recepción");
        }
    }

    // ─── eliminarRegistro ─────────────────────────────────────────────────────

    @Override
    protected void eliminarRegistro() {
        try {
            String idRec = txtIdRecepcion.getText().trim();
            if (idRec.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Seleccione una recepción de la tabla para eliminar");
                return;
            }

            int op = JOptionPane.showConfirmDialog(this,
                "¿Eliminar recepción #" + idRec + "?\n"
                + "El vehículo " + txtMatricula.getText() + " volverá a estado reservado.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            // Al eliminar la recepción, el vehículo vuelve a estar reservado
            Vehiculo v = vehiculoDAO.buscar(txtMatricula.getText().trim());
            if (v != null) {
                v.setStatus(false);
                vehiculoDAO.guardar(v, v.getMatricula());
            }

            recepcionDAO.eliminar(idRec);

            JOptionPane.showMessageDialog(this, "Recepción eliminada correctamente.");
            limpiarCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar la recepción");
        }
    }

    // ─── volver ───────────────────────────────────────────────────────────────

    @Override
    protected void volver() {
        this.dispose();
        menu.setVisible(true);
    }
}