package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.ClienteDAO;
import com.mycompany.rentcar.modelo.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ConsultaClientes extends JFrame {

    private ClienteDAO dao = new ClienteDAO();
    private MenuAdmin  menu;

    private DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable tabla = new JTable(modelo);

    private JTextField txtBuscarId   = new JTextField(14);
    private JTextField txtRangoDesde = new JTextField(10);
    private JTextField txtRangoHasta = new JTextField(10);

    private JButton btnTodos  = new JButton("Todos");
    private JButton btnPorId  = new JButton("Buscar por ID");
    private JButton btnRango  = new JButton("Buscar por Rango");
    private JButton btnVolver = new JButton("Volver");

    private JLabel lblResultados = new JLabel("Resultados: 0");

    public ConsultaClientes(MenuAdmin m) {
        this.menu = m;
        setTitle("Consulta de Clientes");
        setSize(860, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── Columnas ─────────────────────────────────────────────────────────
        modelo.setColumnIdentifiers(new String[]{
            "Cédula", "Nombre", "Apellido", "Dirección", "Email", "Teléfono"
        });
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setFillsViewportHeight(true);

        // ── Fila 1 de filtros: Todos + por ID ────────────────────────────────
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fila1.add(btnTodos);
        fila1.add(Box.createHorizontalStrut(10));
        fila1.add(new JLabel("ID Cédula:"));
        fila1.add(txtBuscarId);
        fila1.add(btnPorId);

        // ── Fila 2 de filtros: por rango ──────────────────────────────────────
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fila2.add(new JLabel("Rango — Desde:"));
        fila2.add(txtRangoDesde);
        fila2.add(new JLabel("Hasta:"));
        fila2.add(txtRangoHasta);
        fila2.add(btnRango);

        // ── Panel norte con las dos filas ─────────────────────────────────────
        JPanel pNorte = new JPanel(new GridLayout(2, 1));
        pNorte.setBorder(BorderFactory.createTitledBorder("Filtros"));
        pNorte.add(fila1);
        pNorte.add(fila2);

        // ── Panel sur ─────────────────────────────────────────────────────────
        JPanel pSur = new JPanel(new BorderLayout());
        pSur.add(lblResultados, BorderLayout.WEST);
        pSur.add(btnVolver,     BorderLayout.EAST);
        pSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // ── Layout principal ──────────────────────────────────────────────────
        // NORTE = filtros (tamaño natural, sin scroll)
        // CENTRO = tabla con scroll
        // SUR = resultado + volver
        setLayout(new BorderLayout(0, 4));
        add(pNorte,               BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(pSur,                 BorderLayout.SOUTH);

        eventos();
        consultarTodos();
    }

    private void eventos() {
        btnTodos.addActionListener(e -> consultarTodos());

        btnPorId.addActionListener(e -> consultarPorId());
        txtBuscarId.addActionListener(e -> consultarPorId());

        btnRango.addActionListener(e -> consultarPorRango());
        txtRangoHasta.addActionListener(e -> consultarPorRango());

        btnVolver.addActionListener(e -> {
            dispose();
            menu.setVisible(true);
        });
    }

    private void consultarTodos() {
        try {
            cargarTabla(dao.listar());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes");
        }
    }

    private void consultarPorId() {
        String id = txtBuscarId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID de cédula para buscar");
            return;
        }
        try {
            Cliente c = dao.buscar(id);
            modelo.setRowCount(0);
            if (c != null) {
                agregarFila(c);
                actualizarContador(1);
            } else {
                actualizarContador(0);
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún cliente con ID: " + id);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar cliente");
        }
    }

    private void consultarPorRango() {
        String desdeStr = txtRangoDesde.getText().trim();
        String hastaStr = txtRangoHasta.getText().trim();

        if (desdeStr.isEmpty() || hastaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Ingrese tanto el ID de inicio como el de fin del rango");
            return;
        }
        try {
            List<Cliente> lista = dao.listar();
            modelo.setRowCount(0);
            int count = 0;
            for (Cliente c : lista) {
                String ced = c.getCedula();
                if (ced.compareToIgnoreCase(desdeStr) >= 0 &&
                    ced.compareToIgnoreCase(hastaStr) <= 0) {
                    agregarFila(c);
                    count++;
                }
            }
            actualizarContador(count);
            if (count == 0) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron clientes en el rango: "
                    + desdeStr + " — " + hastaStr);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar por rango");
        }
    }

    private void cargarTabla(List<Cliente> lista) {
        modelo.setRowCount(0);
        for (Cliente c : lista) agregarFila(c);
        actualizarContador(lista.size());
    }

    private void agregarFila(Cliente c) {
        modelo.addRow(new Object[]{
            c.getCedula(), c.getNombre(), c.getApellido(),
            c.getDireccion(), c.getEmail(), c.getTelefono()
        });
    }

    private void actualizarContador(int n) {
        lblResultados.setText("Resultados: " + n);
    }
}