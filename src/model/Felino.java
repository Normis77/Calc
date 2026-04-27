package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * ============================================================
 *  CÁlculo de Energia y Ración
 * ============================================================
 *
 * ENERGIA DE MANTENIMIENTO (EM) segun nivel de actividad :
 *   Sedentario:           PesoKg * 60  Kcal
 *   Moderadamente activo: PesoKg * 70  Kcal
 *   Muy activo:           PesoKg * 80  Kcal
 *
 * CRITERIOS ESPECIALES :
 *   Marcados en ROJO (ya dan la energia total; NO se suma a EM):
 *     Posdestete:    250 * PesoKg   [Kcal]
 *     > 20 semanas:  130 * PesoKg   [Kcal]
 *     > 30 semanas:  100 * PesoKg   [Kcal]
 *   Marcados en AZUL (se calcula y se SUMA a la EM base):
 *     Gestacion:     EM + (1.25 * EM)
 *     Lactacion:     EM + (3.0  * EM)   
 *
 * CALCULO DE RACION :
 *   Paso 1: DE = [(Prot%*3.5) + (Lip%*8.5) + (Carb%*3.5)] * 10  [Kcal/kg]
 *   Paso 2: Kg alimento = Energia total / DE
 *   Paso 3: Gramos = Kg * 1000
 *   Paso 4: Tasas  = Gramos / 100
 *   Si el problema NO especifica alimento, se omite el calculo de racion.
 *
 * CONVERSION: Kg <-> Lb (1 kg = 2.2 lb)
 *
 * 
 * ============================================================
 */
public class Felino extends Animal {

    // ----------------------------------------------------------
    // Componentes Swing - inicializados UNA VEZ en constructor
    // ----------------------------------------------------------
    private JTextField        txtPeso;
    private JComboBox<String> cbUnidadPeso;
    private JComboBox<String> cbNivelActividad;
    private JComboBox<String> cbCriterioEspecial;
    private JComboBox<String> cbAlimento;
    private JCheckBox         chkSinAlimento;

    // Etiquetas de resultado
    private JLabel lblResultadoEM;
    private JLabel lblResultadoTotal;
    private JLabel lblResultadoKg;
    private JLabel lblResultadoTasas;

    // ----------------------------------------------------------
    // BASE DE DATOS de concentrados felinos
    // { Proteina%, Lipidos%, Carbohidratos% }
    // ----------------------------------------------------------
    private static final String[] NOMBRES_ALIM = {
        "Rufo (baja calidad)",
        "Hills (alta calidad)",
        "Purina (mediana calidad)"
    };
    private static final double[][] DB_ALIM = {
        {30.0, 10.0, 40.0},   // Rufo
        {33.0, 20.6, 39.0},   // Hills
        {31.0, 11.0, 45.0}    // Purina
    };

    // Niveles de actividad y sus constantes
    private static final String[] ACTIVIDADES = {
        "Sedentario (60 Kcal/kg)",
        "Moderadamente activo (70 Kcal/kg)",
        "Muy activo (80 Kcal/kg)"
    };
    private static final double[] KCAL_ACT = {60.0, 70.0, 80.0};

    // Criterios especiales
    private static final String[] CRITERIOS = {
        "Ninguno (solo EM base)",
        "[ROJO] Posdestete   - 250 Kcal/kg",
        "[ROJO] > 20 semanas - 130 Kcal/kg",
        "[ROJO] > 30 semanas - 100 Kcal/kg",
        "[AZUL] Gestacion    - EM + (1.25 * EM)",
        "[AZUL] Lactacion    - EM + (3.0 * EM)"
    };

    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================
    public Felino(double peso, String unidad) {
        super("Felino", peso, unidad);

        txtPeso             = new JTextField(8);
        cbUnidadPeso        = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbNivelActividad    = new JComboBox<>(ACTIVIDADES);
        cbCriterioEspecial  = new JComboBox<>(CRITERIOS);
        cbAlimento          = new JComboBox<>(NOMBRES_ALIM);
        chkSinAlimento      = new JCheckBox("El problema no especifica el alimento");

        lblResultadoEM     = new JLabel("-");
        lblResultadoTotal  = new JLabel("-");
        lblResultadoKg     = new JLabel("-");
        lblResultadoTasas  = new JLabel("-");

        chkSinAlimento.addActionListener(e ->
            cbAlimento.setEnabled(!chkSinAlimento.isSelected()));
    }

    // ==========================================================
    // PANEL VISUAL
    // ==========================================================
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tabla base de datos (referencia)
        String[] colDB = {"Alimento", "Proteina (%)", "Lipidos (%)", "Carb. (%)", "DE Kcal/kg"};
        Object[][] dataDB = {
            {"Rufo (baja calidad)",    30.0, 10.0, 40.0, String.format("%.1f", calcDE(0))},
            {"Hills (alta calidad)",   33.0, 20.6, 39.0, String.format("%.1f", calcDE(1))},
            {"Purina (media calidad)", 31.0, 11.0, 45.0, String.format("%.1f", calcDE(2))}
        };
        JTable tablaDB = new JTable(new javax.swing.table.DefaultTableModel(dataDB, colDB));
        tablaDB.setEnabled(false);
        tablaDB.setRowHeight(22);
        JScrollPane scrollDB = new JScrollPane(tablaDB);
        scrollDB.setPreferredSize(new Dimension(720, 88));
        scrollDB.setBorder(BorderFactory.createTitledBorder(
            "Base de Datos Felinos  |  DE = (Prot*3.5 + Lip*8.5 + Carb*3.5) * 10  [Kcal/kg]"));
        panel.add(scrollDB);

        // Entradas del animal
        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder("Datos del Gato"));
        entradas.add(crearFila("Peso del animal:",       txtPeso,              cbUnidadPeso));
        entradas.add(crearFila("Nivel de actividad:",    new JLabel("Nivel:"), cbNivelActividad));
        entradas.add(crearFila("Criterio especial:",     new JLabel("Estado:"),cbCriterioEspecial));
        entradas.add(crearFila("Alimento seleccionado:", new JLabel("Marca:"), cbAlimento));

        JPanel panelCheck = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        panelCheck.add(chkSinAlimento);
        JLabel notaCheck = new JLabel("  (si marcado: solo se reporta energia, sin calcular racion)");
        notaCheck.setFont(new Font("SansSerif", Font.ITALIC, 11));
        notaCheck.setForeground(Color.GRAY);
        panelCheck.add(notaCheck);
        entradas.add(panelCheck);

        JLabel leyenda = new JLabel(
            "  [ROJO] = ya da la energia total (no se suma a EM).  [AZUL] = se suma a la EM base.");
        leyenda.setFont(new Font("SansSerif", Font.ITALIC, 11));
        leyenda.setForeground(new Color(120, 0, 0));
        entradas.add(leyenda);
        panel.add(entradas);

        // Resultados rapidos
        JPanel res = new JPanel(new GridLayout(4, 2, 6, 4));
        res.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        res.add(new JLabel("Energia de mantenimiento (EM):"));
        estilizar(lblResultadoEM, new Color(0, 100, 200));
        res.add(lblResultadoEM);
        res.add(new JLabel("Energia total requerida:"));
        estilizar(lblResultadoTotal, new Color(150, 0, 150));
        res.add(lblResultadoTotal);
        res.add(new JLabel("Kg de alimento por dia:"));
        estilizar(lblResultadoKg, new Color(0, 120, 0));
        res.add(lblResultadoKg);
        res.add(new JLabel("Tasas/porciones por dia:"));
        estilizar(lblResultadoTasas, new Color(160, 80, 0));
        res.add(lblResultadoTasas);
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

    private double aKg(double valor) {
        String u = (String) cbUnidadPeso.getSelectedItem();
        return (u != null && u.equalsIgnoreCase("Lb")) ? valor / 2.2 : valor;
    }

    /** DE = [(Prot%*3.5) + (Lip%*8.5) + (Carb%*3.5)] * 10 */
    private double calcDE(int idx) {
        double[] a = DB_ALIM[idx];
        return ((a[0] * 3.5) + (a[1] * 8.5) + (a[2] * 3.5)) * 10.0;
    }

    @Override public double calcularPesoMetabolico() { return 0; }

    // ==========================================================
    //  CALCULO PRINCIPAL
    // ==========================================================
    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();
        try {
            // --------------------------------------------------
            // PASO 1: Peso en Kg
            //   Si lo dieron en Lb -> dividir entre 2.2
            // --------------------------------------------------
            double pesoKg = aKg(Double.parseDouble(
                txtPeso.getText().trim().replace(",", ".")));
            if (pesoKg <= 0) {
                JOptionPane.showMessageDialog(null, "El peso debe ser mayor a 0.");
                return 0;
            }

            // --------------------------------------------------
            // PASO 2: Energia de Mantenimiento (EM)
            //   EM = PesoKg * constante_de_actividad  [Kcal/dia]
            //   Sedentario=60, Mod. activo=70, Muy activo=80
            // --------------------------------------------------
            int    actIdx   = cbNivelActividad.getSelectedIndex();
            double kcalFact = KCAL_ACT[actIdx];
            double em       = pesoKg * kcalFact;

            // --------------------------------------------------
            // PASO 3: Ajuste por criterio especial
            //   ROJO: reemplaza la EM (ya es la energia total)
            //   AZUL: se suma a la EM
            // --------------------------------------------------
            int    critIdx = cbCriterioEspecial.getSelectedIndex();
            String critNom = CRITERIOS[critIdx];
            double energiaTotal;
            String detalleCriterio;

            switch (critIdx) {
                case 0: // Ninguno
                    energiaTotal    = em;
                    detalleCriterio = "Solo EM base";
                    break;
                case 1: // ROJO Posdestete
                    energiaTotal    = 250.0 * pesoKg;
                    detalleCriterio = "250 * " + String.format("%.2f", pesoKg)
                        + " = " + String.format("%.2f", energiaTotal) + " Kcal";
                    break;
                case 2: // ROJO > 20 semanas
                    energiaTotal    = 130.0 * pesoKg;
                    detalleCriterio = "130 * " + String.format("%.2f", pesoKg)
                        + " = " + String.format("%.2f", energiaTotal) + " Kcal";
                    break;
                case 3: // ROJO > 30 semanas
                    energiaTotal    = 100.0 * pesoKg;
                    detalleCriterio = "100 * " + String.format("%.2f", pesoKg)
                        + " = " + String.format("%.2f", energiaTotal) + " Kcal";
                    break;
                case 4: // AZUL Gestacion: EM + (1.25 * EM)
                    energiaTotal    = em + (1.25 * em);
                    detalleCriterio = String.format("%.2f + (1.25 * %.2f) = %.2f Kcal", em, em, energiaTotal);
                    break;
                case 5: // AZUL Lactacion: EM + (3.0 * EM)
                    // Factor 3 segun Perros.pdf: "se multiplica 3 * energia de mantenimiento"
                    energiaTotal    = em + (3.0 * em);
                    detalleCriterio = String.format("%.2f + (3.0 * %.2f) = %.2f Kcal", em, em, energiaTotal);
                    break;
                default:
                    energiaTotal    = em;
                    detalleCriterio = "-";
                    break;
            }

            lblResultadoEM.setText(String.format("%.2f Kcal/dia", em));
            lblResultadoTotal.setText(String.format("%.2f Kcal/dia", energiaTotal));

            // Llenar mapa
            ultimosResultados.put("Peso Vivo",                             String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("ENERGIA DE MANTENIMIENTO",              "");
            ultimosResultados.put("  Nivel de actividad",                  ACTIVIDADES[actIdx]);
            ultimosResultados.put("  Paso 1 - EM = PesoKg * constante",   String.format("%.2f * %.0f = %.2f Kcal/dia", pesoKg, kcalFact, em));
            ultimosResultados.put("AJUSTE POR CRITERIO ESPECIAL",          "");
            ultimosResultados.put("  Criterio aplicado",                   critNom);
            ultimosResultados.put("  Paso 2 - Energia total",              detalleCriterio);
            ultimosResultados.put("  Energia total requerida",             String.format("%.2f Kcal/dia", energiaTotal));

            // --------------------------------------------------
            // PASO 4: Calculo de racion 
            // --------------------------------------------------
            if (chkSinAlimento.isSelected()) {
                lblResultadoKg.setText("No especificado");
                lblResultadoTasas.setText("No especificado");
                ultimosResultados.put("RACION",     "");
                ultimosResultados.put("  Alimento", "No especificado en el problema");
            } else {
                int    aliIdx   = cbAlimento.getSelectedIndex();
                String nomAli   = NOMBRES_ALIM[aliIdx];
                double deKcalKg = calcDE(aliIdx);

                // Kg = Energia total / DE
                double kgAli     = energiaTotal / deKcalKg;
                // Gramos = Kg * 1000
                double gramosAli = kgAli * 1000.0;
                // Tasas = Gramos / 100
                double tasas     = gramosAli / 100.0;

                lblResultadoKg.setText(String.format("%.4f Kg  (%.2f g)", kgAli, gramosAli));
                lblResultadoTasas.setText(String.format("%.2f tasas/dia", tasas));

                ultimosResultados.put("RACION",                                "");
                ultimosResultados.put("  Alimento",                            nomAli);
                ultimosResultados.put("  Composicion  Prot / Lip / Carb",      String.format("%.1f%% / %.1f%% / %.1f%%",
                    DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2]));
                ultimosResultados.put("  Paso 3 - DE del alimento",            String.format(
                    "(%.1f*3.5 + %.1f*8.5 + %.1f*3.5)*10 = %.2f Kcal/Kg",
                    DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2], deKcalKg));
                ultimosResultados.put("  Paso 4 - Kg = Energia / DE",          String.format(
                    "%.2f / %.2f = %.4f Kg", energiaTotal, deKcalKg, kgAli));
                ultimosResultados.put("  Paso 5 - Gramos = Kg * 1000",         String.format(
                    "%.4f * 1000 = %.2f g", kgAli, gramosAli));
                ultimosResultados.put("  Paso 6 - Tasas = Gramos / 100",       String.format(
                    "%.2f / 100 = %.2f tasas/dia", gramosAli, tasas));
            }

            return energiaTotal;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que el campo peso tenga un valor numerico valido.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}