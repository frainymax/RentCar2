package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.RecepcionDAO;
import com.mycompany.rentcar.modelo.Recepcion;

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
 * Ventana de Consultas de Recepción de Vehículos.
 * Maneja 1 consulta del menú:
 *   n) Recepción por fecha
 */
public class ConsultaRecepcion extends JFrame {

    private RecepcionDAO dao = new RecepcionDAO();
    private MenuAdmin    menu;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable tabla = new JTable(modelo);

    private JSpinner spDesde = new JSpinner(new SpinnerDateModel());
    private JSpinner spHasta = new JSpinner(new SpinnerDateModel());

    private JButton btnPorFecha = new JButton("Buscar por Fecha");
    private JButton btnTodos    = new JButton("Todas");
    private JButton btnVolver   = new JButton("Volver");

    private JLabel lblResultados = new JLabel("Resultados: 0");

    public ConsultaRecepcion(MenuAdmin m) {
        this.menu = m;
        setTitle("Consulta de Recepción de Vehículos");
        setSize(820, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Formato spinners
        spDesde.setEditor(new JSpinner.DateEditor(spDesde, "yyyy-MM-dd"));
        spHasta.setEditor(new JSpinner.DateEditor(spHasta, "yyyy-MM-dd"));

        // ── Columnas ─────────────────────────────────────────────────────────
        modelo.setColumnIdentifiers(new String[]{
            "ID Recepción", "ID Reserva", "Matrícula",
            "Fecha Entrada", "Fecha Recepción", "Observación"
        });
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // ── Panel filtros ─────────────────────────────────────────────────────
        JPanel pFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pFiltros.setBorder(BorderFactory.createTitledBorder("n) Recepción por Fecha de Recepción"));
        pFiltros.add(new JLabel("Desde:"));
        pFiltros.add(spDesde);
        pFiltros.add(new JLabel("  Hasta:"));
        pFiltros.add(spHasta);
        pFiltros.add(btnPorFecha);

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
        add(pFiltros,               BorderLayout.NORTH);
        add(new JScrollPane(tabla),  BorderLayout.CENTER);
        add(pSur,                   BorderLayout.SOUTH);

        eventos();
        consultarTodas();
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    private void eventos() {
        btnTodos.addActionListener(e -> consultarTodas());
        btnPorFecha.addActionListener(e -> consultarPorFecha());
        btnVolver.addActionListener(e -> {
            dispose();
            menu.setVisible(true);
        });
    }

    // ── Todas las recepciones ─────────────────────────────────────────────────

    private void consultarTodas() {
        try {
            cargarTabla(dao.listar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recepciones");
        }
    }

    // ── n) Por rango de fecha de recepción ────────────────────────────────────

    private void consultarPorFecha() {
        try {
            LocalDate desde = spinnerALocalDate(spDesde);
            LocalDate hasta = spinnerALocalDate(spHasta);

            if (hasta.isBefore(desde)) {
                JOptionPane.showMessageDialog(this,
                    "La fecha 'Hasta' no puede ser anterior a la fecha 'Desde'");
                return;
            }

            List<Recepcion> resultado = new ArrayList<>();
            for (Recepcion r : dao.listar()) {
                try {
                    LocalDate fechaRec = LocalDate.parse(r.getFechaRecepcion(), FMT);
                    if (!fechaRec.isBefore(desde) && !fechaRec.isAfter(hasta)) {
                        resultado.add(r);
                    }
                } catch (Exception ignored) {}
            }

            cargarTabla(resultado);
            if (resultado.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay recepciones entre "
                    + desde.format(FMT) + " y " + hasta.format(FMT));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar por fecha");
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void cargarTabla(List<Recepcion> lista) {
        modelo.setRowCount(0);
        for (Recepcion r : lista) {
            modelo.addRow(new Object[]{
                r.getId(),
                r.getIdReserva(),
                r.getMatricula(),
                r.getFechaEntrada(),
                r.getFechaRecepcion(),
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
