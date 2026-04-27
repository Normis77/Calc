package ui;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.*;

public class VentanaPrincipal extends JFrame {

    private JTabbedPane pestanas;
    private JPanel      panelParticularidadesContenedor;
    private JComboBox<String> comboEspecies;
    private Animal animalActual;
    private DefaultTableModel modeloResultados;

    // -------------------------------------------------------------------------
    public VentanaPrincipal() {
        setTitle("Sistema de Formulacion Nutricional Veterinaria");
        setSize(980, 740);
        setMinimumSize(new Dimension(820, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pestanas = new JTabbedPane();
        pestanas.addTab("Ingredientes",           crearPanelIngredientes());
        pestanas.addTab("Definicion Requerimientos", crearPanelRequerimientos());
        pestanas.addTab("Calculo de Racion",      crearPanelCalculo());

        pestanas.setSelectedIndex(1);
        add(pestanas);
    }

    // -------------------------------------------------------------------------
    //  Ingredientes
    // -------------------------------------------------------------------------
    private JPanel crearPanelIngredientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Especie","Alimento","Grasa/PC","Energia","Proteina","Ca","P","Mg","Na","Cl","K"};
        Object[][] data = {
            {"Equino","Heno",           "2.70 g","2.10 Mcal","8.4 g/kg","7.2 g","0.22 g","0.17 g","0.04 g","0.19 g","0.20 g"},
            {"Equino","Avena",          "4.70 g","2.77 Mcal","8.6 g/kg","1.1 g","0.31 g","0.13 g","0.05 g","0.04 g","0.30 g"},
            {"Equino","Maiz",           "4.11 g","3.27 Mcal","6.7 g/kg","0.4 g","0.29 g","0.09 g","0.03 g","0.06 g","0.30 g"},
            {"Bovino","Heno 1er corte", "-","2.10 MJ/kg","55 g/kg","-","-","-","-","-","-"},
            {"Bovino","Heno 2do corte", "-","4.40 MJ/kg","115 g/kg","-","-","-","-","-","-"},
            {"Bovino","Soya",           "-","7.10 MJ/kg","450 g/kg","-","-","-","-","-","-"},
            {"Bovino","Cebada",         "-","7.25 MJ/kg","105 g/kg","-","-","-","-","-","-"},
            {"Bovino","Alfalfa",        "-","2.20 MJ/kg","33 g/kg","-","-","-","-","-","-"},
            {"Bovino","Avena",          "-","7.70 MJ/kg","148 g/kg","-","-","-","-","-","-"},
            {"Bovino","Concentrado",    "-","6.50 MJ/kg","220 g/kg","-","-","-","-","-","-"},
            {"Canino","Rufo (baja)",    "9%","-","18%","-","-","-","-","-","-"},
            {"Canino","Hills (alta)",   "14.3%","-","25%","-","-","-","-","-","-"},
            {"Canino","Monello (media)","11%","-","23%","-","-","-","-","-","-"},
            {"Felino","Rufo (baja)",    "10%","-","30%","-","-","-","-","-","-"},
            {"Felino","Hills (alta)",   "20.6%","-","33%","-","-","-","-","-","-"},
            {"Felino","Purina (media)", "11%","-","31%","-","-","-","-","-","-"},
        };
        JTable tabla = new JTable(new DefaultTableModel(data, cols));
        tabla.setEnabled(false);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
            "Referencia Nutricional - Todos los Ingredientes disponibles"));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(new JLabel(
            "  Mcal = Megacalorias  |  MJ = MegaJoules  |  PC = Proteina Cruda  |  - = No aplica"),
            BorderLayout.SOUTH);
        return panel;
    }

    // -------------------------------------------------------------------------
    //  Definición de Requerimientos
    // -------------------------------------------------------------------------
    private JPanel crearPanelRequerimientos() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));

        // Selector de especie
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        fila.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        fila.add(new JLabel("Seleccione Especie:"));

        String[] especies = {"Seleccione...", "Equino", "Porcino", "Canino", "Felino", "Bovino"};
        comboEspecies = new JComboBox<>(especies);
        comboEspecies.setFont(new Font("SansSerif", Font.BOLD, 13));
        comboEspecies.setPreferredSize(new Dimension(160, 28));
        fila.add(comboEspecies);
        fila.add(new JLabel("  <- Seleccione una especie para ver su formulario"));

        // Panel central que cambia según la especie
        panelParticularidadesContenedor = new JPanel(new BorderLayout());
        panelParticularidadesContenedor.setBorder(BorderFactory.createTitledBorder("Datos Especificos"));

        JLabel lblBienvenida = new JLabel(
            "<html><center><br><br>" +
            "<b>Sistema de Formulación Nutricional Veterinaria</b><br><br>" +
            "Seleccione una especie en el menú superior para comenzar.<br><br>",
            SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panelParticularidadesContenedor.add(lblBienvenida, BorderLayout.CENTER);

        comboEspecies.addActionListener(e -> actualizarParticularidades());

        // Botón de cálculo
        JButton btnCalcular = new JButton("Realizar Cálculo");
        btnCalcular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCalcular.setBackground(new Color(34, 139, 34));
        btnCalcular.setForeground(Color.BLACK);
        btnCalcular.setFocusPainted(false);
        btnCalcular.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        btnCalcular.addActionListener(e -> realizarCalculo());

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panelBoton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        panelBoton.add(btnCalcular);

        principal.add(fila, BorderLayout.NORTH);
        principal.add(new JScrollPane(panelParticularidadesContenedor), BorderLayout.CENTER);
        principal.add(panelBoton, BorderLayout.SOUTH);
        return principal;
    }

    // Reemplaza el panel de datos con el de la especie seleccionada
    private void actualizarParticularidades() {
        panelParticularidadesContenedor.removeAll();
        String sel = (String) comboEspecies.getSelectedItem();

        switch (sel) {
            case "Equino":  animalActual = new Equino(0.0,  "Kg"); break;
            case "Porcino": animalActual = new Porcino(0.0, "Kg"); break;
            case "Canino":  animalActual = new Canino(0.0,  "Kg"); break;
            case "Felino":  animalActual = new Felino(0.0,  "Kg"); break;
            case "Bovino":  animalActual = new Bovino(0.0,  "Kg"); break;
            default:
                animalActual = null;
                JLabel lbl = new JLabel(
                    "Seleccione una especie para ver su formulario.", SwingConstants.CENTER);
                panelParticularidadesContenedor.add(lbl, BorderLayout.CENTER);
                panelParticularidadesContenedor.revalidate();
                panelParticularidadesContenedor.repaint();
                return;
        }

        panelParticularidadesContenedor.add(animalActual.obtenerPanelPrincipal(), BorderLayout.NORTH);
        panelParticularidadesContenedor.revalidate();
        panelParticularidadesContenedor.repaint();
    }

    // Ejecuta el calculo y navega a la pestana de resultados
    private void realizarCalculo() {
        if (animalActual == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una especie antes de calcular.",
                "Sin especie seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double resultado = animalActual.calcularRequerimientoEnergia();
        if (resultado <= 0) return;  

        Map<String, String> resultados = animalActual.getResultadosParaTabla();
        modeloResultados.setRowCount(0);
        for (Map.Entry<String, String> e : resultados.entrySet()) {
            modeloResultados.addRow(new Object[]{e.getKey(), e.getValue()});
        }

        pestanas.setSelectedIndex(2);
    }

    // -------------------------------------------------------------------------
    //  Cálculo de Ración
    // -------------------------------------------------------------------------
    private JPanel crearPanelCalculo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columnas = {"Concepto / Nutriente", "Resultado"};
        modeloResultados = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable tablaResultados = new JTable(modeloResultados);
        tablaResultados.setRowHeight(24);
        tablaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablaResultados.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tablaResultados.getTableHeader().setReorderingAllowed(false);
        tablaResultados.getColumnModel().getColumn(0).setPreferredWidth(370);
        tablaResultados.getColumnModel().getColumn(1).setPreferredWidth(320);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String texto = val != null ? val.toString() : "";
                String concepto = t.getValueAt(row, 0) != null ? t.getValueAt(row, 0).toString() : "";

                // Filas de seccion: fondo oscuro
                if (concepto.equals("ENERGIA") || concepto.equals("PROTEINA") ||
                    concepto.equals("MINERALES - Requerido / Aportado") ||
                    concepto.equals("RACION PROPUESTA") ||
                    concepto.equals("VERIFICACION DE LA RACION") ||
                    concepto.equals("REQUERIMIENTOS DE MANTENIMIENTO") ||
                    concepto.equals("ANALISIS DE LA RACION") ||
                    concepto.equals("EVALUACION DE LA RACION") ||
                    concepto.equals("AJUSTE F1/F2") ||
                    concepto.equals("RACION") ||
                    concepto.equals("PERDIDA DE PESO") ||
                    concepto.equals("DENSIDAD ENERGETICA (concentrado personalizado)")) {
                    setBackground(new Color(44, 62, 80));
                    setForeground(Color.WHITE);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (texto.contains("DEFICIT") || texto.contains("NO ADECUADA") || texto.contains("NO")) {
                    setBackground(new Color(255, 200, 200));
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                } else if (texto.contains("OK") || texto.contains("ADECUADA") || texto.contains("ideal")) {
                    setBackground(new Color(200, 240, 200));
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                } else {
                    setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return this;
            }
        };
        tablaResultados.getColumnModel().getColumn(0).setCellRenderer(renderer);
        tablaResultados.getColumnModel().getColumn(1).setCellRenderer(renderer);

        JScrollPane scroll = new JScrollPane(tablaResultados);
        scroll.setBorder(BorderFactory.createTitledBorder("Desglose Completo de Resultados"));
        panel.add(scroll, BorderLayout.CENTER);

        // Boton para volver a modificar datos
        JButton btnVolver = new JButton("Modificar Datos");
        btnVolver.addActionListener(e -> pestanas.setSelectedIndex(1));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnVolver);
        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }
}