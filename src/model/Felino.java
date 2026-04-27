package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class Felino extends Animal {

    // ---- Pestana 1: Energia y racion ----
    private JTextField txtPeso;
    private JComboBox<String> cbUnidadPeso;
    private JComboBox<String> cbNivelActividad;
    private JComboBox<String> cbCriterioEspecial;
    private JComboBox<String> cbAlimento;
    private JLabel lblEM;
    private JLabel lblEnergiaTotal;
    private JLabel lblKgAlimento;
    private JLabel lblTasas;

    // ---- Pestana 2: Perdida de peso ----
    private JTextField txtPesoActual;
    private JTextField txtPesoIdeal;
    private JTextField txtPorcentajeSemanal;
    private JComboBox<String> cbUnidadPesoActual;
    private JComboBox<String> cbUnidadPesoIdeal;
    private JLabel lblResultadoPerdida;

    // Base de datos felinos: { Proteina%, Lipidos%, Carbohidratos% }
    private static final String[] NOMBRES_ALIM = {
        "Rufo (baja calidad)", "Hills (alta calidad)", "Purina (mediana calidad)"
    };
    private static final double[][] DB_ALIM = {
        {30.0, 10.0, 40.0},
        {33.0, 20.6, 39.0},
        {31.0, 11.0, 45.0}
    };

    private static final String[] ACTIVIDADES = {
        "Sedentario (60 Kcal/kg)",
        "Moderadamente activo (70 Kcal/kg)",
        "Muy activo (80 Kcal/kg)"
    };
    private static final double[] KCAL_ACT = {60.0, 70.0, 80.0};

    // Criterios especiales
    private static final String[] CRITERIOS = {
        "Ninguno (solo EM base)",
        "[ROJO] Posdestete (250 Kcal/kg)",
        "[ROJO] > 20 semanas (130 Kcal/kg)",
        "[ROJO] > 30 semanas (100 Kcal/kg)",
        "[AZUL] Finales de gestacion (EM + EM*1.25)",
        "[AZUL] Lactacion (EM + EM*3.5)"
    };

    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    public Felino(double peso, String unidad) {
        super("Felino", peso, unidad);

        txtPeso              = new JTextField(8);
        cbUnidadPeso         = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbNivelActividad     = new JComboBox<>(ACTIVIDADES);
        cbCriterioEspecial   = new JComboBox<>(CRITERIOS);
        cbAlimento           = new JComboBox<>(NOMBRES_ALIM);

        lblEM              = new JLabel("-");
        lblEnergiaTotal    = new JLabel("-");
        lblKgAlimento      = new JLabel("-");
        lblTasas           = new JLabel("-");

        txtPesoActual        = new JTextField(8);
        txtPesoIdeal         = new JTextField(8);
        txtPorcentajeSemanal = new JTextField("1.0", 5);
        cbUnidadPesoActual   = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbUnidadPesoIdeal    = new JComboBox<>(new String[]{"Kg", "Lb"});
        lblResultadoPerdida  = new JLabel("-");
    }

    // -------------------------------------------------------------------------
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));

        JTabbedPane subPestanas = new JTabbedPane();
        subPestanas.addTab("Energia y Racion", crearSubPanelEnergia());
        subPestanas.addTab("Perdida de Peso",   crearSubPanelPerdida());

        contenedor.add(subPestanas);
        return contenedor;
    }

    private JPanel crearSubPanelEnergia() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tabla base de datos
        String[] colDB = {"Alimento", "Proteina (%)", "Lipidos (%)", "Carb. (%)", "Kcal/kg"};
        Object[][] dataDB = {
            {"Rufo (baja)",    30.0, 10.0, 40.0, String.format("%.1f", calcDE(0))},
            {"Hills (alta)",   33.0, 20.6, 39.0, String.format("%.1f", calcDE(1))},
            {"Purina (media)", 31.0, 11.0, 45.0, String.format("%.1f", calcDE(2))}
        };
        JTable tablaDB = new JTable(new javax.swing.table.DefaultTableModel(dataDB, colDB));
        tablaDB.setEnabled(false);
        tablaDB.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tablaDB);
        scroll.setPreferredSize(new Dimension(700, 88));
        scroll.setBorder(BorderFactory.createTitledBorder("Base de Datos - Concentrados Felinos"));
        panel.add(scroll);

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder("Datos del Gato"));
        entradas.add(crearFila("Peso del animal:",       txtPeso,             cbUnidadPeso));
        entradas.add(crearFila("Nivel de actividad:",    new JLabel("Nivel:"),cbNivelActividad));
        entradas.add(crearFila("Criterio especial:",     new JLabel("Estado:"),cbCriterioEspecial));
        entradas.add(crearFila("Alimento seleccionado:", new JLabel("Marca:"), cbAlimento));

        JLabel leyenda = new JLabel(
            "  [ROJO] = reemplaza la EM base.  [AZUL] = se suma a la EM base.");
        leyenda.setFont(new Font("SansSerif", Font.ITALIC, 11));
        leyenda.setForeground(new Color(120, 0, 0));
        entradas.add(leyenda);
        panel.add(entradas);

        JPanel res = new JPanel(new GridLayout(4, 2, 6, 4));
        res.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        res.add(new JLabel("Energia de mantenimiento (EM):"));
        estilizar(lblEM, new Color(0, 100, 200));
        res.add(lblEM);
        res.add(new JLabel("Energia total requerida:"));
        estilizar(lblEnergiaTotal, new Color(150, 0, 150));
        res.add(lblEnergiaTotal);
        res.add(new JLabel("Cantidad de alimento:"));
        estilizar(lblKgAlimento, new Color(0, 120, 0));
        res.add(lblKgAlimento);
        res.add(new JLabel("Tasas/porciones por dia:"));
        estilizar(lblTasas, new Color(160, 80, 0));
        res.add(lblTasas);
        panel.add(res);
        return panel;
    }

    private JPanel crearSubPanelPerdida() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder(
            "Calculo de Semanas para Perdida de Peso"));
        entradas.add(crearFila("Peso actual:", txtPesoActual, cbUnidadPesoActual));
        entradas.add(crearFila("Peso ideal:",  txtPesoIdeal,  cbUnidadPesoIdeal));
        entradas.add(crearFila("% perdida semanal:", txtPorcentajeSemanal, new JLabel("%  (rango: 0.5 - 2%)")));

        JLabel nota = new JLabel("  Perdida semanal recomendada: 0.5% a 2% del peso actual.");
        nota.setFont(new Font("SansSerif", Font.ITALIC, 11));
        nota.setForeground(Color.GRAY);
        entradas.add(nota);
        panel.add(entradas);

        JPanel res = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        res.setBorder(BorderFactory.createTitledBorder("Resultado de Perdida de Peso"));
        res.add(new JLabel("Semanas estimadas:"));
        estilizar(lblResultadoPerdida, new Color(150, 0, 150));
        res.add(lblResultadoPerdida);
        panel.add(res);
        return panel;
    }

    private JPanel crearFila(String titulo, JComponent campo, JComponent extra) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0.38; p.add(new JLabel(titulo), g);
        g.gridx = 1; g.weightx = 0.37; p.add(campo, g);
        g.gridx = 2; g.weightx = 0.25; p.add(extra, g);
        return p;
    }

    private void estilizar(JLabel lbl, Color c) {
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(c);
    }

    private double aKg(double valor, JComboBox<String> cb) {
        String u = (String) cb.getSelectedItem();
        return (u != null && u.equalsIgnoreCase("Lb")) ? valor / 2.2 : valor;
    }

    private double calcDE(int idx) {
        double[] ali = DB_ALIM[idx];
        return ((ali[0] * 3.5) + (ali[1] * 8.5) + (ali[2] * 3.5)) * 10.0;
    }

    @Override public double calcularPesoMetabolico() { return 0; }

    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();

        // Calcular perdida de peso si tiene datos
        calcularPerdidaDePeso();

        try {
            double pesoKg = aKg(Double.parseDouble(txtPeso.getText().trim().replace(",",".")), cbUnidadPeso);
            if (pesoKg <= 0) {
                JOptionPane.showMessageDialog(null, "El peso debe ser mayor a 0.");
                return 0;
            }

            // Energia de mantenimiento
            int    actIdx   = cbNivelActividad.getSelectedIndex();
            double kcalFact = KCAL_ACT[actIdx];
            double em       = pesoKg * kcalFact;

            // Criterio especial
            int    critIdx   = cbCriterioEspecial.getSelectedIndex();
            String critNom   = CRITERIOS[critIdx];
            double energiaTotal;
            String notaCrit;
            switch (critIdx) {
                case 0: energiaTotal = em;                  notaCrit = "Solo EM base"; break;
                case 1: energiaTotal = pesoKg * 250.0;     notaCrit = "Reemplaza EM (250 Kcal/kg)"; break;
                case 2: energiaTotal = pesoKg * 130.0;     notaCrit = "Reemplaza EM (130 Kcal/kg)"; break;
                case 3: energiaTotal = pesoKg * 100.0;     notaCrit = "Reemplaza EM (100 Kcal/kg)"; break;
                case 4: energiaTotal = em + em * 1.25;     notaCrit = "EM + EM*1.25 (se suma)"; break;
                case 5: energiaTotal = em + em * 3.5;      notaCrit = "EM + EM*3.5  (promedio 3-4, se suma)"; break;
                default: energiaTotal = em;                 notaCrit = "-"; break;
            }

            int    aliIdx    = cbAlimento.getSelectedIndex();
            String nomAli    = NOMBRES_ALIM[aliIdx];
            double deKcalKg  = calcDE(aliIdx);
            double kgAli     = energiaTotal / deKcalKg;
            double gramosAli = kgAli * 1000.0;
            double tasas     = gramosAli / 100.0;

            lblEM.setText(String.format("%.2f Kcal/dia", em));
            lblEnergiaTotal.setText(String.format("%.2f Kcal/dia", energiaTotal));
            lblKgAlimento.setText(String.format("%.4f Kg  (%.1f g)", kgAli, gramosAli));
            lblTasas.setText(String.format("%.2f tasas/dia", tasas));

            ultimosResultados.put("Peso Vivo",                         String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("Nivel de actividad",                ACTIVIDADES[actIdx]);
            ultimosResultados.put("ENERGIA",                           "");
            ultimosResultados.put("Constante de actividad",            String.format("%.0f Kcal/Kg", kcalFact));
            ultimosResultados.put("Energia de Mantenimiento (EM)",     String.format("%.2f Kcal/dia", em));
            ultimosResultados.put("Criterio especial aplicado",        critNom);
            ultimosResultados.put("Nota del criterio",                 notaCrit);
            ultimosResultados.put("Energia Total requerida",           String.format("%.2f Kcal/dia", energiaTotal));
            ultimosResultados.put("RACION",                            "");
            ultimosResultados.put("Alimento seleccionado",             nomAli);
            ultimosResultados.put("Composicion Prot/Lip/Carb",         String.format("%.1f%% / %.1f%% / %.1f%%",
                DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2]));
            ultimosResultados.put("Densidad energetica del alimento",  String.format("%.2f Kcal/Kg", deKcalKg));
            ultimosResultados.put("Kg de alimento/dia",                String.format("%.4f Kg", kgAli));
            ultimosResultados.put("Gramos de alimento/dia",            String.format("%.2f g", gramosAli));
            ultimosResultados.put("Tasas (porciones)/dia",             String.format("%.2f tasas", tasas));

            return energiaTotal;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numericos esten completos.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    private void calcularPerdidaDePeso() {
        try {
            String tPA = txtPesoActual.getText().trim().replace(",",".");
            String tPI = txtPesoIdeal.getText().trim().replace(",",".");
            String tPS = txtPorcentajeSemanal.getText().trim().replace(",",".");
            if (tPA.isEmpty() || tPI.isEmpty() || tPS.isEmpty()) return;

            double pActual = aKg(Double.parseDouble(tPA), cbUnidadPesoActual);
            double pIdeal  = aKg(Double.parseDouble(tPI), cbUnidadPesoIdeal);
            double pctSem  = Double.parseDouble(tPS);
            if (pActual <= 0 || pIdeal <= 0 || pctSem <= 0) return;

            double perdidaTotal   = pActual - pIdeal;
            if (perdidaTotal <= 0) {
                lblResultadoPerdida.setText("El animal ya esta en su peso ideal o por debajo.");
                return;
            }
            double perdidaSemanal = pActual * (pctSem / 100.0);
            double semanas        = perdidaTotal / perdidaSemanal;

            lblResultadoPerdida.setText(String.format("%.1f semanas  (%.3f Kg/semana)", semanas, perdidaSemanal));

            ultimosResultados.put("PERDIDA DE PESO", "");
            ultimosResultados.put("Peso actual",            String.format("%.2f Kg", pActual));
            ultimosResultados.put("Peso ideal",             String.format("%.2f Kg", pIdeal));
            ultimosResultados.put("Perdida total",          String.format("%.2f Kg", perdidaTotal));
            ultimosResultados.put("Perdida por semana",     String.format("%.3f Kg/semana (%.1f%%)", perdidaSemanal, pctSem));
            ultimosResultados.put("Semanas estimadas",      String.format("%.1f semanas", semanas));

        } catch (Exception ignored) { /* campos vacios */ }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}