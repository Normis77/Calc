package model;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Equino extends Animal {
    // Componentes de Entrada
    private JTextField txtPesoVivo, txtHenoPct, txtAvenaPct, txtMaizPct, txtNaFuerte;
    private JComboBox<String> cbUnidadPeso, cbTipoTrabajo;
    private JLabel lblResultadoEquino;

    // Constantes de Base de Datos extraídas del documento 
    private final double ED_HENO = 2.10;
    private final double ED_AVENA = 2.77;
    private final double ED_MAIZ = 3.27;

    public Equino(double peso, String unidad) {
        super("Equino", peso, unidad);
        txtPesoVivo = new JTextField();
        txtHenoPct = new JTextField("70"); // Proporción ejemplo [cite: 147]
        txtAvenaPct = new JTextField("15");
        txtMaizPct = new JTextField("15");
        txtNaFuerte = new JTextField(""); // Para ingreso manual en trabajo fuerte [cite: 144]
        
        cbUnidadPeso = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbTipoTrabajo = new JComboBox<>(new String[]{
            "Mantenimiento", "Trabajo moderado", "Trabajo medio", "Trabajo fuerte", "Trabajo muy fuerte"
        });
    }

    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BorderLayout(10, 10));

        // --- PARTE SUPERIOR: BASE DE DATOS (Tabla integrada)  ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        String[] colDB = {"Alimento", "ED (Mcal)", "Prot (g)", "Ca (g)", "P (g)", "Mg (g)", "Na (g)", "Cl (g)", "K (g)"};
        Object[][] dataDB = {
            {"Heno", 2.10, 8.4, 7.2, 0.22, 0.17, 0.04, 0.19, 0.2},
            {"Avena", 2.77, 8.6, 1.1, 0.31, 0.13, 0.05, 0.04, 0.3},
            {"Maíz", 3.27, 6.7, 0.4, 0.29, 0.09, 0.03, 0.06, 0.3}
        };
        JTable tablaDB = new JTable(new DefaultTableModel(dataDB, colDB));
        tablaDB.setEnabled(false); // Solo lectura
        JScrollPane scrollDB = new JScrollPane(tablaDB);
        scrollDB.setPreferredSize(new Dimension(850, 100));
        
        panelSuperior.add(new JLabel("  Base de Datos de Referencia (Aportes por Kg):"), BorderLayout.NORTH);
        panelSuperior.add(scrollDB, BorderLayout.CENTER);

        // --- PARTE CENTRAL: ENTRADAS DE DATOS ---
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        
        panelCentral.add(crearFila("Peso vivo del caballo:", txtPesoVivo, cbUnidadPeso));
        panelCentral.add(crearFila("Nivel de Actividad Física:", new JLabel("Tipo:"), cbTipoTrabajo));
        
        JPanel dietaPanel = new JPanel(new GridLayout(3, 2, 5, 2));
        dietaPanel.setBorder(BorderFactory.createTitledBorder("Distribución de la Ración (%)"));
        dietaPanel.add(new JLabel("% Heno:")); dietaPanel.add(txtHenoPct);
        dietaPanel.add(new JLabel("% Avena:")); dietaPanel.add(txtAvenaPct);
        dietaPanel.add(new JLabel("% Maíz:")); dietaPanel.add(txtMaizPct);
        panelCentral.add(dietaPanel);

        panelCentral.add(crearFila("Sodio Manual (Solo Trabajo Fuerte):", txtNaFuerte, new JLabel("gr")));

        // --- PARTE INFERIOR: RESULTADOS DETALLADOS ---
        lblResultadoEquino = new JLabel("<html><body><font color='blue'>Complete los campos y presione 'Realizar Cálculo'</font></body></html>");
        JScrollPane scrollRes = new JScrollPane(lblResultadoEquino);
        scrollRes.setPreferredSize(new Dimension(400, 250));
        scrollRes.setBorder(BorderFactory.createTitledBorder("Análisis de Requerimientos y Ración"));

        panelContenedor.add(panelSuperior, BorderLayout.NORTH);
        panelContenedor.add(panelCentral, BorderLayout.CENTER);
        panelContenedor.add(scrollRes, BorderLayout.SOUTH);

        return panelContenedor;
    }

    private JPanel crearFila(String titulo, JComponent campo, JComponent dimensional) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 0.3; p.add(new JLabel(titulo), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; p.add(campo, gbc);
        gbc.gridx = 2; gbc.weightx = 0.3; p.add(dimensional, gbc);
        return p;
    }

    private double aKg(double v) {
        // Conversión según documento 1kg = 2.2 lb [cite: 170]
        return cbUnidadPeso.getSelectedItem().equals("Lb") ? v / 2.2 : v;
    }

    @Override
    public double calcularPesoMetabolico() {
        try {
            double kg = aKg(Double.parseDouble(txtPesoVivo.getText().trim().replace(",", ".")));
            return Math.pow(kg, 0.75); // Fórmula: Peso^0.75 [cite: 115]
        } catch (Exception e) { return 0; }
    }

    @Override
    public double calcularRequerimientoEnergia() {
        try {
            double pesoKg = aKg(Double.parseDouble(txtPesoVivo.getText().trim().replace(",", ".")));
            double wkg = calcularPesoMetabolico();
            String trabajo = (String) cbTipoTrabajo.getSelectedItem();

            // 1. CÁLCULO DE ENERGÍA DIGESTIBLE (ED)
            // ED Mantenimiento: Wkg * 0.155 [cite: 118]
            double edMantMcal = wkg * 0.155;
            double edMantMJ = wkg * 0.6; // [cite: 117]

            // ED Ejercicio: Ajuste según tabla de actividad [cite: 134]
            double factor = 1.0;
            if (trabajo.equals("Trabajo moderado")) factor = 1.2;
            else if (trabajo.equals("Trabajo medio")) factor = 1.4;
            else if (trabajo.equals("Trabajo fuerte")) factor = 1.6;
            else if (trabajo.equals("Trabajo muy fuerte")) factor = 1.9;

            double edTotalMcal = edMantMcal * factor;
            double edTotalMJ = edTotalMcal * 4.19; // Conversión: 1 Mcal = 4.19 MJ [cite: 139]

            // 2. REQUERIMIENTO DE PROTEÍNA (CHON) [cite: 140]
            // Fórmula: Energía total Mcal * 19.4
            double proteinaGramos = edTotalMcal * 19.4;

            // 3. REQUERIMIENTO DE MINERALES (Regla de 3 por cada 100Kg de peso) 
            double f100 = pesoKg / 100.0;
            double rCa, rP, rMg, rNa, rK, rCl;

            if (trabajo.equals("Mantenimiento")) {
                rCa = 5 * f100; rP = 3 * f100; rMg = 2 * f100; rNa = 2 * f100; rK = 5 * f100; rCl = 2 * f100;
            } else if (trabajo.equals("Trabajo medio")) {
                rCa = 5.7 * f100; rP = 3.8 * f100; rMg = 2.7 * f100; rNa = 10 * f100; rK = 8.8 * f100; rCl = 14 * f100;
            } else if (trabajo.contains("fuerte")) {
                rCa = 6.7 * f100; rP = 4.9 * f100; rMg = 3.7 * f100;
                rNa = txtNaFuerte.getText().isEmpty() ? 25 * f100 : Double.parseDouble(txtNaFuerte.getText());
                rK = 15 * f100; rCl = 35 * f100;
            } else { // Trabajo moderado/leve
                rCa = 5.2 * f100; rP = 3.2 * f100; rMg = 2.2 * f100; rNa = 3.9 * f100; rK = 5.9 * f100; rCl = 5.1 * f100;
            }

            // 4. CÁLCULO DE LA RACIÓN PROPUESTA [cite: 147-150]
            double hPct = Double.parseDouble(txtHenoPct.getText()) / 100.0;
            double aPct = Double.parseDouble(txtAvenaPct.getText()) / 100.0;
            double mPct = Double.parseDouble(txtMaizPct.getText()) / 100.0;

            double kgHeno = (edTotalMcal * hPct) / ED_HENO;
            double kgAvena = (edTotalMcal * aPct) / ED_AVENA;
            double kgMaiz = (edTotalMcal * mPct) / ED_MAIZ;
            double totalAlimentoKg = kgHeno + kgAvena + kgMaiz;

            // 5. VERIFICACIÓN DE REGLAS CRÍTICAS [cite: 155-163]
            double minHeno = 0.5 * f100;
            double maxConc = 0.4 * f100;
            double msLimite = pesoKg * 0.02;

            // GENERACIÓN DEL REPORTE DETALLADO (HTML)
            StringBuilder res = new StringBuilder("<html><body style='width:350px'>");
            res.append("<b>1. PESO METABÓLICO:</b> ").append(String.format("%.2f", wkg)).append(" Wkg<sup>0.75</sup><br><br>");
            res.append("<b>2. REQUERIMIENTOS TOTALES:</b><br>");
            res.append("- Energía (ED): ").append(String.format("%.2f Mcal (%.2f MJ)", edTotalMcal, edTotalMJ)).append("<br>");
            res.append("- Proteína (CHON): ").append(String.format("%.2f g", proteinaGramos)).append("<br>");
            res.append("- Minerales (g/día):<br>");
            res.append("&nbsp;&nbsp;Ca: ").append(String.format("%.1f", rCa)).append(" | P: ").append(String.format("%.1f", rP))
               .append(" | Mg: ").append(String.format("%.1f", rMg)).append("<br>");
            res.append("&nbsp;&nbsp;Na: ").append(String.format("%.1f", rNa)).append(" | K: ").append(String.format("%.1f", rK))
               .append(" | Cl: ").append(String.format("%.1f", rCl)).append("<br><br>");
            
            res.append("<b>3. RACIÓN PROPUESTA:</b><br>");
            res.append("- Heno: ").append(String.format("%.2f Kg", kgHeno)).append("<br>");
            res.append("- Avena: ").append(String.format("%.2f Kg", kgAvena)).append("<br>");
            res.append("- Maíz: ").append(String.format("%.2f Kg", kgMaiz)).append("<br><br>");

            res.append("<b>4. VERIFICACIÓN DE RACIÓN:</b><br>");
            res.append("- Mín. Heno (").append(String.format("%.1f", minHeno)).append(" Kg): ").append(kgHeno >= minHeno ? "ADECUADA" : "NO ADECUADA").append("<br>");
            res.append("- Máx. Concentrado (").append(String.format("%.1f", maxConc)).append(" Kg): ").append((kgAvena+kgMaiz) <= maxConc ? "ADECUADA" : "NO ADECUADA").append("<br>");
            res.append("- Límite Consumo MS (").append(String.format("%.1f", msLimite)).append(" Kg): ").append(totalAlimentoKg <= msLimite ? "ADECUADA" : "NO ADECUADA");
            res.append("</body></html>");

            lblResultadoEquino.setText(res.toString());
            return edTotalMcal;

        } catch (Exception e) {
            lblResultadoEquino.setText("<html><font color='red'>Error: Verifique que los campos numéricos no estén vacíos.</font></html>");
            return 0;
        }
    }
}