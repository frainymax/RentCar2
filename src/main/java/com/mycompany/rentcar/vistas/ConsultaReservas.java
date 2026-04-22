package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.ReservaDAO;
import com.mycompany.rentcar.modelo.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Ventana de Consultas de Reservas.
 * Maneja 2 consultas del menú:
 *   l) Reservas por rango de fechas
 *   m) Reservas por número de días
 */
public class ConsultaReservas extends JFrame {

    private ReservaDAO dao = new ReservaDAO();
    private MenuAdmin  menu;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable tabla = new JTable(modelo);

    // Filtro por fechas
    private JSpinner spDesde = new JSpinner(new SpinnerDateModel());
    private JSpinner spHasta = new JSpinner(new SpinnerDateModel());
    private JButton  btnPorFecha = new JButton("Buscar por Fecha");

    // Filtro por días
    private JTextField txtDiasMin = new JTextField(5);
    private JTextField txtDiasMax = new JTextField(5);
    private JButton    btnPorDias = new JButton("Buscar por Días");

    private JButton btnTodos  = new JButton("Todas");
    private JButton btnVolver = new JButton("Volver");

    private JLabel lblResultados = new JLabel("Resultados: 0");

    public ConsultaReservas(MenuAdmin m) {
        this.menu = m;
        setTitle("Consulta de Reservas");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Formato spinners
        spDesde.setEditor(new JSpinner.DateEditor(spDesde, "yyyy-MM-dd"));
        spHasta.setEditor(new JSpinner.DateEditor(spHasta, "yyyy-MM-dd"));

        // ── Columnas ─────────────────────────────────────────────────────────
        modelo.setColumnIdentifiers(new String[]{
            "ID", "Matrícula", "Cédula", "F.Reserva",
            "F.Salida", "F.Entrada", "Días", "Total RD$", "Observación"
        });
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ── Panel filtro por fechas ───────────────────────────────────────────
        JPanel pFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pFechas.setBorder(BorderFactory.createTitledBorder("l) Por rango de Fecha de Reserva"));
        pFechas.add(new JLabel("Desde:"));
        pFechas.add(spDesde);
        pFechas.add(new JLabel("  Hasta:"));
        pFechas.add(spHasta);
        pFechas.add(btnPorFecha);

        // ── Panel filtro por días ─────────────────────────────────────────────
        JPanel pDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pDias.setBorder(BorderFactory.createTitledBorder("m) Por número de Días"));
        pDias.add(new JLabel("Días mínimo:"));
        pDias.add(txtDiasMin);
        pDias.add(new JLabel("  Días máximo:"));
        pDias.add(txtDiasMax);
        pDias.add(btnPorDias);
        pDias.add(new JLabel("  (dejar máximo vacío = exacto)"));

        // ── Panel filtros norte ───────────────────────────────────────────────
        JPanel pNorte = new JPanel(new GridLayout(2, 1));
        pNorte.add(pFechas);
        pNorte.add(pDias);

        // ── Panel sur ────────────────────────────────────────────────────────
        JPanel pSur = new JPanel(new BorderLayout());
        JPanel pSurIzq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pSurIzq.add(btnTodos);
        pSurIzq.add(lblResultados);
        pSur.add(pSurIzq,  BorderLayout.WEST);
        pSur.add(btnVolver, BorderLayout.EAST);
        pSur.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // ── Layout principal ──────────────────────────────────────────────────
        setLayout(new BorderLayout(0, 4));
        add(pNorte,               BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(pSur,                 BorderLayout.SOUTH);

        eventos();
        consultarTodas();
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    private void eventos() {
        btnTodos.addActionListener(e -> consultarTodas());
        btnPorFecha.addActionListener(e -> consultarPorFecha());
        btnPorDias.addActionListener(e -> consultarPorDias());
        txtDiasMin.addActionListener(e -> consultarPorDias());

        btnVolver.addActionListener(e -> {
            dispose();
            menu.setVisible(true);
        });
    }

    // ── Todas las reservas ────────────────────────────────────────────────────

    private void consultarTodas() {
        try {
            cargarTabla(dao.listar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar reservas");
        }
    }

    // ── l) Por rango de fechas ────────────────────────────────────────────────

    private void consultarPorFecha() {
        try {
            LocalDate desde = spinnerALocalDate(spDesde);
            LocalDate hasta = spinnerALocalDate(spHasta);

            if (hasta.isBefore(desde)) {
                JOptionPane.showMessageDialog(this,
                    "La fecha 'Hasta' no puede ser anterior a la fecha 'Desde'");
                return;
            }

            List<Reserva> resultado = new ArrayList<>();
            for (Reserva r : dao.listar()) {
                try {
                    LocalDate fechaReserva = LocalDate.parse(r.getFechaReserva(), FMT);
                    if (!fechaReserva.isBefore(desde) && !fechaReserva.isAfter(hasta)) {
                        resultado.add(r);
                    }
                } catch (Exception ignored) {}
            }

            cargarTabla(resultado);
            if (resultado.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay reservas entre " + desde.format(FMT) + " y " + hasta.format(FMT));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar por fecha");
        }
    }

    // ── m) Por días ───────────────────────────────────────────────────────────

    private void consultarPorDias() {
        String minStr = txtDiasMin.getText().trim();
        String maxStr = txtDiasMax.getText().trim();

        if (minStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese al menos el número mínimo de días");
            return;
        }

        try {
            int min = Integer.parseInt(minStr);
            // Si no hay máximo, buscar exactamente ese número de días
            int max = maxStr.isEmpty() ? min : Integer.parseInt(maxStr);

            if (min > max) {
                JOptionPane.showMessageDialog(this,
                    "El mínimo de días no puede ser mayor que el máximo");
                return;
            }

            List<Reserva> resultado = new ArrayList<>();
            for (Reserva r : dao.listar()) {
                if (r.getDias() >= min && r.getDias() <= max) {
                    resultado.add(r);
                }
            }

            cargarTabla(resultado);
            if (resultado.isEmpty()) {
                String rango = maxStr.isEmpty()
                    ? "exactamente " + min + " día(s)"
                    : "entre " + min + " y " + max + " días";
                JOptionPane.showMessageDialog(this,
                    "No hay reservas con " + rango);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Los valores de días deben ser números enteros");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar por días");
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void cargarTabla(List<Reserva> lista) {
        modelo.setRowCount(0);
        for (Reserva r : lista) {
            modelo.addRow(new Object[]{
                r.getIdReserva(),
                r.getMatricula(),
                r.getCedula(),
                r.getFechaReserva(),
                r.getFechaSalida(),
                r.getFechaEntrada(),
                r.getDias(),
                String.format("%.2f", r.getTotal()),
                r.getObservacion()
            });
        }
        lblResultados.setText("Resultados: " + lista.size());
    }

    private LocalDate spinnerALocalDate(JSpinner spinner) {
        Date fecha = (Date) spinner.getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
