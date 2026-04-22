package com.mycompany.rentcar.vistas;

import com.mycompany.rentcar.dao.GamaDAO;
import com.mycompany.rentcar.dao.OfertaDAO;
import com.mycompany.rentcar.dao.VehiculoDAO;
import com.mycompany.rentcar.modelo.Gama;
import com.mycompany.rentcar.modelo.Oferta;
import com.mycompany.rentcar.modelo.Vehiculo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultaVehiculos extends JFrame {

    private VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private OfertaDAO   ofertaDAO   = new OfertaDAO();
    private GamaDAO     gamaDAO     = new GamaDAO();
    private MenuAdmin   menu;

    // ── Tablas ────────────────────────────────────────────────────────────────
    private DefaultTableModel modeloVeh = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable tablaVeh = new JTable(modeloVeh);

    private DefaultTableModel modeloOfe = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable tablaOfe = new JTable(modeloOfe);

    // ── Filtros ───────────────────────────────────────────────────────────────
    private JTextField txtMatricula   = new JTextField(10);
    private JTextField txtMarca       = new JTextField(10);
    private JTextField txtGama        = new JTextField(7);
    private JTextField txtPrecioDesde = new JTextField(7);
    private JTextField txtPrecioHasta = new JTextField(7);

    private JButton btnTodos        = new JButton("Todos");
    private JButton btnDisponibles  = new JButton("Disponibles");
    private JButton btnRentados     = new JButton("Rentados");
    private JButton btnPorMatricula = new JButton("Por Matrícula");
    private JButton btnPorMarca     = new JButton("Por Marca");
    private JButton btnPorGama      = new JButton("Por Gama");
    private JButton btnPorPrecio    = new JButton("Por Precio");
    private JButton btnCargarOfertas= new JButton("Cargar Ofertas");
    private JButton btnVolver       = new JButton("Volver");

    private JLabel lblResultadosVeh = new JLabel("Resultados: 0");
    private JLabel lblResultadosOfe = new JLabel("Resultados: 0");

    public ConsultaVehiculos(MenuAdmin m) {
        this.menu = m;
        setTitle("Consulta de Vehículos y Ofertas");
        setSize(1000, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── Columnas vehículos ────────────────────────────────────────────────
        modeloVeh.setColumnIdentifiers(new String[]{
            "Matrícula", "Marca", "Modelo", "Tipo", "Motor",
            "Gama", "Color", "Techo", "Aire", "Cuero", "Auto", "Status"
        });
        tablaVeh.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaVeh.setFillsViewportHeight(true);

        // ── Columnas ofertas ──────────────────────────────────────────────────
        modeloOfe.setColumnIdentifiers(new String[]{
            "ID Oferta", "Matrícula Vehículo", "Descripción", "Precio Oferta"
        });
        tablaOfe.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaOfe.setFillsViewportHeight(true);

        // ──────────────────────────────────────────────────────────────────────
        // PANEL DE FILTROS: 3 filas, cada una con FlowLayout LEFT
        // Sin JScrollPane para que no tape los botones
        // ──────────────────────────────────────────────────────────────────────

        // Fila 1: botones rápidos
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        fila1.add(btnTodos);
        fila1.add(btnDisponibles);
        fila1.add(btnRentados);

        // Fila 2: matrícula + marca
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        fila2.add(new JLabel("Matrícula:"));
        fila2.add(txtMatricula);
        fila2.add(btnPorMatricula);
        fila2.add(Box.createHorizontalStrut(20));
        fila2.add(new JLabel("Marca:"));
        fila2.add(txtMarca);
        fila2.add(btnPorMarca);

        // Fila 3: gama + precio
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        fila3.add(new JLabel("Gama:"));
        fila3.add(txtGama);
        fila3.add(btnPorGama);
        fila3.add(Box.createHorizontalStrut(20));
        fila3.add(new JLabel("Precio: desde"));
        fila3.add(txtPrecioDesde);
        fila3.add(new JLabel("hasta"));
        fila3.add(txtPrecioHasta);
        fila3.add(btnPorPrecio);

        JPanel pFiltros = new JPanel(new GridLayout(3, 1));
        pFiltros.setBorder(BorderFactory.createTitledBorder("Filtros de Vehículos"));
        pFiltros.add(fila1);
        pFiltros.add(fila2);
        pFiltros.add(fila3);

        // ── Pestaña vehículos ─────────────────────────────────────────────────
        JPanel panelVeh = new JPanel(new BorderLayout(0, 4));
        panelVeh.add(pFiltros,              BorderLayout.NORTH);
        panelVeh.add(new JScrollPane(tablaVeh), BorderLayout.CENTER);
        panelVeh.add(lblResultadosVeh,      BorderLayout.SOUTH);

        // ── Pestaña ofertas ───────────────────────────────────────────────────
        JPanel pOfeNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pOfeNorte.setBorder(BorderFactory.createTitledBorder("Ofertas"));
        pOfeNorte.add(btnCargarOfertas);

        JPanel panelOfe = new JPanel(new BorderLayout(0, 4));
        panelOfe.add(pOfeNorte,             BorderLayout.NORTH);
        panelOfe.add(new JScrollPane(tablaOfe), BorderLayout.CENTER);
        panelOfe.add(lblResultadosOfe,      BorderLayout.SOUTH);

        // ── Tabs ──────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vehículos", panelVeh);
        tabs.addTab("Ofertas",   panelOfe);

        // ── Sur ───────────────────────────────────────────────────────────────
        JPanel pSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pSur.add(btnVolver);

        setLayout(new BorderLayout());
        add(tabs,  BorderLayout.CENTER);
        add(pSur,  BorderLayout.SOUTH);

        eventos();
        consultarTodos();
        consultarOfertas();
    }

    private void eventos() {
        btnTodos.addActionListener(e -> consultarTodos());
        btnDisponibles.addActionListener(e -> filtrarPorStatus(true,  "disponibles"));
        btnRentados.addActionListener(e   -> filtrarPorStatus(false, "rentados/reservados"));

        btnPorMatricula.addActionListener(e -> consultarPorMatricula());
        txtMatricula.addActionListener(e    -> consultarPorMatricula());

        btnPorMarca.addActionListener(e -> consultarPorMarca());
        txtMarca.addActionListener(e    -> consultarPorMarca());

        btnPorGama.addActionListener(e -> consultarPorGama());
        txtGama.addActionListener(e    -> consultarPorGama());

        btnPorPrecio.addActionListener(e    -> consultarPorPrecio());
        txtPrecioHasta.addActionListener(e  -> consultarPorPrecio());

        btnCargarOfertas.addActionListener(e -> consultarOfertas());

        btnVolver.addActionListener(e -> {
            dispose();
            menu.setVisible(true);
        });
    }

    // ── d) Todos ──────────────────────────────────────────────────────────────
    private void consultarTodos() {
        try { cargarTablaVeh(vehiculoDAO.listar()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al cargar vehículos"); }
    }

    // ── e) Por matrícula ──────────────────────────────────────────────────────
    private void consultarPorMatricula() {
        String mat = txtMatricula.getText().trim();
        if (mat.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese una matrícula"); return; }
        try {
            Vehiculo v = vehiculoDAO.buscar(mat);
            modeloVeh.setRowCount(0);
            if (v != null) { agregarFilaVeh(v); lblResultadosVeh.setText("Resultados: 1"); }
            else { lblResultadosVeh.setText("Resultados: 0");
                   JOptionPane.showMessageDialog(this, "No se encontró vehículo: " + mat); }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al buscar vehículo"); }
    }

    // ── f/g) Disponibles / Rentados ───────────────────────────────────────────
    private void filtrarPorStatus(boolean status, String desc) {
        try {
            List<Vehiculo> res = new ArrayList<>();
            for (Vehiculo v : vehiculoDAO.listar())
                if (v.isStatus() == status) res.add(v);
            cargarTablaVeh(res);
            if (res.isEmpty()) JOptionPane.showMessageDialog(this, "No hay vehículos " + desc);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al filtrar"); }
    }

    // ── h) Por marca ──────────────────────────────────────────────────────────
    private void consultarPorMarca() {
        String marca = txtMarca.getText().trim();
        if (marca.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese una marca"); return; }
        try {
            List<Vehiculo> res = new ArrayList<>();
            for (Vehiculo v : vehiculoDAO.listar())
                if (v.getMarca().equalsIgnoreCase(marca)) res.add(v);
            cargarTablaVeh(res);
            if (res.isEmpty()) JOptionPane.showMessageDialog(this, "No hay vehículos de marca: " + marca);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al buscar por marca"); }
    }

    // ── i) Por gama ───────────────────────────────────────────────────────────
    private void consultarPorGama() {
        String gama = txtGama.getText().trim();
        if (gama.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese un ID de gama"); return; }
        try {
            List<Vehiculo> res = new ArrayList<>();
            for (Vehiculo v : vehiculoDAO.listar())
                if (v.getGama().equalsIgnoreCase(gama)) res.add(v);
            cargarTablaVeh(res);
            if (res.isEmpty()) JOptionPane.showMessageDialog(this, "No hay vehículos de gama: " + gama);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al buscar por gama"); }
    }

    // ── j) Ofertas ────────────────────────────────────────────────────────────
    private void consultarOfertas() {
        try {
            List<Oferta> lista = ofertaDAO.listar();
            modeloOfe.setRowCount(0);
            for (Oferta o : lista)
                modeloOfe.addRow(new Object[]{
                    o.getId(), o.getMatricula(),
                    o.getDescripcion(),
                    String.format("%.2f", o.getPrecio())
                });
            lblResultadosOfe.setText("Resultados: " + lista.size());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al cargar ofertas"); }
    }

    // ── k) Por precio ─────────────────────────────────────────────────────────
    private void consultarPorPrecio() {
        String desdeStr = txtPrecioDesde.getText().trim();
        String hastaStr = txtPrecioHasta.getText().trim();
        if (desdeStr.isEmpty() || hastaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese precio mínimo y máximo"); return;
        }
        try {
            double desde = Double.parseDouble(desdeStr);
            double hasta = Double.parseDouble(hastaStr);
            if (desde > hasta) {
                JOptionPane.showMessageDialog(this, "El precio mínimo no puede ser mayor que el máximo"); return;
            }
            List<Vehiculo> res = new ArrayList<>();
            for (Vehiculo v : vehiculoDAO.listar()) {
                Gama g = gamaDAO.buscarPorId(v.getGama());
                if (g != null && g.getPrecio() >= desde && g.getPrecio() <= hasta) res.add(v);
            }
            cargarTablaVeh(res);
            if (res.isEmpty())
                JOptionPane.showMessageDialog(this,
                    "No hay vehículos con precio entre " + desdeStr + " y " + hastaStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Los precios deben ser numéricos");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar por precio");
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    private void cargarTablaVeh(List<Vehiculo> lista) {
        modeloVeh.setRowCount(0);
        for (Vehiculo v : lista) agregarFilaVeh(v);
        lblResultadosVeh.setText("Resultados: " + lista.size());
    }

    private void agregarFilaVeh(Vehiculo v) {
        modeloVeh.addRow(new Object[]{
            v.getMatricula(),
            v.getMarca(),
            v.getModelo(),
            v.getTipoVehiculo() == 0 ? "Normal"   : "Turístico",
            v.getTipoMotor()    == 0 ? "Gasolina"  : "Diésel",
            v.getGama(),
            v.getColor(),
            v.isTecho()      ? "Sí" : "No",
            v.isAire()       ? "Sí" : "No",
            v.isCuero()      ? "Sí" : "No",
            v.isAutomatico() ? "Sí" : "No",
            v.isStatus()     ? "Disponible" : "Reservado"
        });
    }
}