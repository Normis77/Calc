package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * ============================================================
 *  Calculo de Energia y Racion
 * ============================================================
 *
 * FORMULA DE ENERGIA DE MANTENIMIENTO (EM):
 *   Segun el documento (Perros.pdf):
 *     Paso 1: PM = PesoKg ^ 0.75        (Peso Metabolico)
 *     Paso 2: EM = 132 * PM              [Kcal/dia]
 *
 * AJUSTE POR ESTADO FISIOLOGICO:
 *   Energia requerida = EM * factor fisiologico
 *   Factores:
 *     Hasta 40% del peso adulto           -> 2.0
 *     Entre 40% y 80% del peso adulto     -> 1.5
 *     Entre 80% y peso adulto completo    -> 1.2
 *     Primeras 6 semanas de gestacion     -> 1.0 a 1.2  
 *     Tres ultimas semanas de gestacion   -> 1.2 a 1.5  
 *     Lactacion                           -> 2 a 4      
 *     Medianamente activo                 -> 1.2
 *     Muy activo                          -> 1.5
 *     Sumamente activo                    -> 2 a 3      
 *     En geriatria                        -> 0.8
 *     Sobrepeso hasta 20%                 -> 0.8
 *     Obeso o superior al 20%             -> 0.7
 *
 *
 * CALCULO DE RACION (solo si se especifica el alimento):
 *   Paso 1: DE = [(Prot%*3.5) + (Lip%*8.5) + (Carb%*3.5)] * 10  [Kcal/kg]
 *   Paso 2: Kg alimento = Energia requerida / DE
 *   Paso 3: Gramos = Kg * 1000
 *   Paso 4: Tasas  = Gramos / 100
 *   Si el problema NO especifica el alimento, solo se reporta la energia.
 *
 * DENSIDAD ENERGETICA PERSONALIZADA (sub-pestana separada):
 *   Permite calcular la DE de cualquier concentrado ingresando su composicion.
 *
 * CONVERSION: Kg <-> Lb (1 kg = 2.2 lb)
 *
 * ============================================================
 */
public class Canino extends Animal {

    // ----------------------------------------------------------
    // Componentes Swing - todos inicializados UNA VEZ en constructor
    // ----------------------------------------------------------
    private JTextField        txtPeso;
    private JTextField        txtPesoAdulto;       // Opcional: para determinar % del peso adulto
    private JComboBox<String> cbUnidadPeso;
    private JComboBox<String> cbEstadoFisiologico;
    private JComboBox<String> cbAlimento;
    private JCheckBox         chkSinAlimento;       // El problema no especifica el alimento
    private JLabel            lblPesoAdultoLbl;     // Etiqueta que se muestra/oculta

    // Sub-panel densidad personalizada
    private JTextField txtProtPct;
    private JTextField txtLipPct;
    private JTextField txtCarbPct;
    private JTextField txtHumedadPct;   // Solo informativo
    private JTextField txtCenizaPct;    // Solo informativo

    // Etiquetas de resultado
    private JLabel lblResultadoPM;
    private JLabel lblResultadoEnergia;
    private JLabel lblResultadoKg;
    private JLabel lblResultadoTasas;
    private JLabel lblResultadoDensidad;

    // ----------------------------------------------------------
    // BASE DE DATOS de concentrados caninos
    // { Proteina%, Lipidos%, Carbohidratos% }
    // Fuente: Perros.pdf y CALCULO_DE_RACIONES_1_.pdf
    // ----------------------------------------------------------
    private static final String[] NOMBRES_ALIM = {
        "Rufo (baja calidad)",
        "Hills (alta calidad)",
        "Monello (mediana calidad)"
    };
    private static final double[][] DB_ALIM = {
        {18.0,  9.0,  60.0},   // Rufo
        {25.0, 14.3,  52.1},   // Hills
        {23.0, 11.0,  52.5}    // Monello
    };

    // ----------------------------------------------------------
    // Estados fisiologicos con sus factores (tabla Perros.pdf)
    // Para rangos se usa el valor promedio del intervalo
    // ----------------------------------------------------------
    private static final String[] ESTADOS = {
        "Hasta 40% del peso adulto",
        "Entre 40% y 80% del peso adulto",
        "Entre 80% y peso adulto completo",
        "Primeras 6 semanas de gestacion",
        "Tres ultimas semanas de gestacion",
        "Lactacion",
        "Medianamente activo",
        "Muy activo",
        "Sumamente activo",
        "En geriatria",
        "Sobrepeso hasta 20%",
        "Obeso o superior al 20%"
    };
    private static final double[] FACTORES = {
        2.0,    // Hasta 40%
        1.5,    // Entre 40-80%
        1.2,    // Entre 80%-adulto
        1.1,    // Primeras 6 sem gestacion (promedio 1.0-1.2)
        1.35,   // Tres ultimas sem gestacion (promedio 1.2-1.5)
        3.0,    // Lactacion (promedio 2-4)
        1.2,    // Medianamente activo
        1.5,    // Muy activo
        2.5,    // Sumamente activo (promedio 2-3)
        0.8,    // En geriatria
        0.8,    // Sobrepeso hasta 20%
        0.7     // Obeso o superior al 20%
    };
    // Solo los 3 primeros estados pueden usar el peso adulto para
    // determinar el porcentaje exacto
    private static final boolean[] ESTADO_USA_PESO_ADULTO = {
        true, true, true,
        false, false, false, false, false, false, false, false, false
    };

    // Mapa de resultados para poblar la JTable
    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================
    public Canino(double peso, String unidad) {
        super("Canino", peso, unidad);

        txtPeso             = new JTextField(8);
        txtPesoAdulto       = new JTextField(8);
        cbUnidadPeso        = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbEstadoFisiologico = new JComboBox<>(ESTADOS);
        cbAlimento          = new JComboBox<>(NOMBRES_ALIM);
        chkSinAlimento      = new JCheckBox("El problema no especifica el alimento");
        lblPesoAdultoLbl    = new JLabel("Peso adulto esperado (opcional):");

        txtProtPct    = new JTextField(6);
        txtLipPct     = new JTextField(6);
        txtCarbPct    = new JTextField(6);
        txtHumedadPct = new JTextField(6);
        txtCenizaPct  = new JTextField(6);

        lblResultadoPM      = new JLabel("-");
        lblResultadoEnergia = new JLabel("-");
        lblResultadoKg      = new JLabel("-");
        lblResultadoTasas   = new JLabel("-");
        lblResultadoDensidad= new JLabel("-");

        // Mostrar/ocultar campo de peso adulto segun estado seleccionado
        cbEstadoFisiologico.addActionListener(e -> {
            boolean usa = ESTADO_USA_PESO_ADULTO[cbEstadoFisiologico.getSelectedIndex()];
            lblPesoAdultoLbl.setVisible(usa);
            txtPesoAdulto.setVisible(usa);
        });
        lblPesoAdultoLbl.setVisible(ESTADO_USA_PESO_ADULTO[0]);
        txtPesoAdulto.setVisible(ESTADO_USA_PESO_ADULTO[0]);

        // Habilitar/deshabilitar combo de alimento segun checkbox
        chkSinAlimento.addActionListener(e ->
            cbAlimento.setEnabled(!chkSinAlimento.isSelected()));
    }

    // ==========================================================
    // PANEL VISUAL
    // ==========================================================
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));

        JTabbedPane sub = new JTabbedPane();
        sub.addTab("Energia y Racion",        crearSubPanelEnergia());
        sub.addTab("Densidad de Concentrado", crearSubPanelDensidad());

        contenedor.add(sub);
        return contenedor;
    }

    // Energia y Racion
    private JPanel crearSubPanelEnergia() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tabla de referencia de la base de datos
        String[] colDB = {"Alimento", "Proteina (%)", "Lipidos (%)", "Carb. (%)", "DE Kcal/kg"};
        Object[][] dataDB = {
            {"Rufo (baja calidad)",    18.0,  9.0,  60.0, String.format("%.1f", calcDE(0))},
            {"Hills (alta calidad)",   25.0, 14.3,  52.1, String.format("%.1f", calcDE(1))},
            {"Monello (media calidad)",23.0, 11.0,  52.5, String.format("%.1f", calcDE(2))}
        };
        JTable tablaDB = new JTable(new javax.swing.table.DefaultTableModel(dataDB, colDB));
        tablaDB.setEnabled(false);
        tablaDB.setRowHeight(22);
        JScrollPane scrollDB = new JScrollPane(tablaDB);
        scrollDB.setPreferredSize(new Dimension(720, 88));
        scrollDB.setBorder(BorderFactory.createTitledBorder(
            "Base de Datos Caninos  |  DE = (Prot*3.5 + Lip*8.5 + Carb*3.5) * 10  [Kcal/kg]"));
        panel.add(scrollDB);

        // Entradas del animal
        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder("Datos del Perro"));
        entradas.add(crearFila("Peso del animal:",           txtPeso,       cbUnidadPeso));
        entradas.add(crearFila("Estado fisiologico:",        new JLabel("Estado:"), cbEstadoFisiologico));
        entradas.add(crearFilaComp(lblPesoAdultoLbl,         txtPesoAdulto, new JLabel("Kg (deje vacio si no aplica)")));
        entradas.add(crearFila("Alimento seleccionado:",     new JLabel("Marca:"),  cbAlimento));

        JPanel panelCheck = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        panelCheck.add(chkSinAlimento);
        JLabel notaCheck = new JLabel("  (si marcado: solo se reporta energia, sin calcular racion)");
        notaCheck.setFont(new Font("SansSerif", Font.ITALIC, 11));
        notaCheck.setForeground(Color.GRAY);
        panelCheck.add(notaCheck);
        entradas.add(panelCheck);
        panel.add(entradas);

        // Resultados rapidos
        JPanel res = new JPanel(new GridLayout(4, 2, 6, 4));
        res.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        res.add(new JLabel("Peso metabolico (PM = PV^0.75):"));
        estilizar(lblResultadoPM, new Color(100, 0, 150));
        res.add(lblResultadoPM);
        res.add(new JLabel("Energia requerida (EM * factor):"));
        estilizar(lblResultadoEnergia, new Color(0, 100, 200));
        res.add(lblResultadoEnergia);
        res.add(new JLabel("Kg de alimento por dia:"));
        estilizar(lblResultadoKg, new Color(0, 120, 0));
        res.add(lblResultadoKg);
        res.add(new JLabel("Tasas/porciones por dia:"));
        estilizar(lblResultadoTasas, new Color(160, 80, 0));
        res.add(lblResultadoTasas);
        panel.add(res);
        return panel;
    }

    // Densidad energetica personalizada
    private JPanel crearSubPanelDensidad() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder(
            "Densidad Energetica de Concentrado Personalizado"));

        JLabel formula = new JLabel(
            "<html><b>DE = [(Prot% x 3.5) + (Lip% x 8.5) + (Carb% x 3.5)] x 10</b><br>" +
            "Humedad y ceniza son informativos y no entran al calculo de DE.</html>");
        formula.setFont(new Font("SansSerif", Font.PLAIN, 11));
        formula.setForeground(new Color(40, 40, 120));
        entradas.add(formula);
        entradas.add(Box.createVerticalStrut(6));

        entradas.add(crearFila("% Proteina:",      txtProtPct,    new JLabel("%")));
        entradas.add(crearFila("% Lipidos:",       txtLipPct,     new JLabel("%")));
        entradas.add(crearFila("% Carbohidratos:", txtCarbPct,    new JLabel("%")));
        entradas.add(crearFila("% Humedad:",        txtHumedadPct, new JLabel("% (informativo)")));
        entradas.add(crearFila("% Ceniza:",         txtCenizaPct,  new JLabel("% (informativo)")));
        panel.add(entradas);

        JPanel res = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        res.setBorder(BorderFactory.createTitledBorder("Resultado"));
        res.add(new JLabel("DE del concentrado:"));
        estilizar(lblResultadoDensidad, new Color(0, 100, 200));
        res.add(lblResultadoDensidad);
        panel.add(res);
        return panel;
    }

    // Layout helpers
    private JPanel crearFila(String titulo, JComponent campo, JComponent extra) {
        return crearFilaComp(new JLabel(titulo), campo, extra);
    }

    private JPanel crearFilaComp(JComponent etiqueta, JComponent campo, JComponent extra) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0.40; p.add(etiqueta, g);
        g.gridx = 1; g.weightx = 0.35; p.add(campo, g);
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

    /**
     * Calcula la Densidad Energetica de un alimento por su indice en la base de datos.
     * DE = [(Prot% * 3.5) + (Lip% * 8.5) + (Carb% * 3.5)] * 10  [Kcal/kg]
     */
    private double calcDE(int idx) {
        double[] a = DB_ALIM[idx];
        return ((a[0] * 3.5) + (a[1] * 8.5) + (a[2] * 3.5)) * 10.0;
    }

    @Override
    public double calcularPesoMetabolico() {
        try {
            double kg = aKg(Double.parseDouble(
                txtPeso.getText().trim().replace(",", ".")), cbUnidadPeso);
            return Math.pow(kg, 0.75);
        } catch (Exception e) { return 0; }
    }

    // ==========================================================
    //  CALCULO PRINCIPAL
    // ==========================================================
    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();

        // Primero calcular densidad de concentrado personalizado si hay datos en ese sub-panel
        calcularDensidadPersonalizada();

        try {
            // --------------------------------------------------
            // PASO 1: Peso en Kg
            //   Si lo dieron en Lb -> dividir entre 2.2
            // --------------------------------------------------
            double pesoKg = aKg(
                Double.parseDouble(txtPeso.getText().trim().replace(",", ".")),
                cbUnidadPeso);
            if (pesoKg <= 0) {
                JOptionPane.showMessageDialog(null, "El peso debe ser mayor a 0.");
                return 0;
            }

            // --------------------------------------------------
            // PASO 2: Peso Metabolico
            //   PM = PesoKg ^ 0.75
            // --------------------------------------------------
            double pm = Math.pow(pesoKg, 0.75);

            // --------------------------------------------------
            // PASO 3: Energia de Mantenimiento
            //   EM = 132 * PM   [Kcal/dia]
            //   Fuente: Perros.pdf, paso 3 del calculo de energia
            // --------------------------------------------------
            double em = 132.0 * pm;

            // --------------------------------------------------
            // PASO 4: Factor fisiologico
            //   Si es estado de crecimiento y se dio peso adulto:
            //     -> calcular % y elegir factor automaticamente
            //   Si no se dio peso adulto o no es estado de crecimiento:
            //     -> usar el factor del combo directamente
            // --------------------------------------------------
            int    estadoIdx = cbEstadoFisiologico.getSelectedIndex();
            String estadoNom = ESTADOS[estadoIdx];
            double factor    = FACTORES[estadoIdx];

            if (ESTADO_USA_PESO_ADULTO[estadoIdx]) {
                String txPA = txtPesoAdulto.getText().trim().replace(",", ".");
                if (!txPA.isEmpty()) {
                    double pesoAdulto = Double.parseDouble(txPA);
                    if (pesoAdulto > 0) {
                        double pct = (pesoKg / pesoAdulto) * 100.0;
                        if (pct <= 40.0) {
                            factor    = 2.0;
                            estadoNom = "Hasta 40% del peso adulto (actual: "
                                + String.format("%.1f", pct) + "%)";
                        } else if (pct <= 80.0) {
                            factor    = 1.5;
                            estadoNom = "Entre 40-80% del peso adulto (actual: "
                                + String.format("%.1f", pct) + "%)";
                        } else {
                            factor    = 1.2;
                            estadoNom = "Entre 80% y peso adulto (actual: "
                                + String.format("%.1f", pct) + "%)";
                        }
                    }
                }
                // Si campo vacio: usar factor del combo sin cambio
            }

            // --------------------------------------------------
            // PASO 5: Energia ajustada = EM * factor  [Kcal/dia]
            // --------------------------------------------------
            double energiaAdj = em * factor;

            lblResultadoPM.setText(String.format("%.4f", pm));
            lblResultadoEnergia.setText(String.format("%.2f Kcal/dia", energiaAdj));

            // Llenar mapa de resultados
            ultimosResultados.put("Peso Vivo",                          String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("ENERGIA DE MANTENIMIENTO",           "");
            ultimosResultados.put("  Paso 1 - Peso Metabolico",         String.format("PM = %.2f ^ 0.75 = %.4f", pesoKg, pm));
            ultimosResultados.put("  Paso 2 - EM = 132 * PM",           String.format("132 * %.4f = %.2f Kcal/dia", pm, em));
            ultimosResultados.put("AJUSTE FISIOLOGICO",                 "");
            ultimosResultados.put("  Estado fisiologico",               estadoNom);
            ultimosResultados.put("  Factor de ajuste",                 String.format("x %.2f", factor));
            ultimosResultados.put("  Paso 3 - Energia ajustada",        String.format("%.2f * %.2f = %.2f Kcal/dia", em, factor, energiaAdj));

            // --------------------------------------------------
            // PASO 6: Calculo de racion (solo si hay alimento)
            // --------------------------------------------------
            if (chkSinAlimento.isSelected()) {
                lblResultadoKg.setText("No especificado");
                lblResultadoTasas.setText("No especificado");
                ultimosResultados.put("RACION",           "");
                ultimosResultados.put("  Alimento",       "No especificado en el problema");
                ultimosResultados.put("  Nota",           "Solo se reporta la energia requerida.");
            } else {
                int    aliIdx   = cbAlimento.getSelectedIndex();
                String nomAli   = NOMBRES_ALIM[aliIdx];
                double deKcalKg = calcDE(aliIdx);

                // Paso 6a: Kg = Energia / DE
                double kgAli = energiaAdj / deKcalKg;
                // Paso 6b: Gramos = Kg * 1000
                double gramosAli = kgAli * 1000.0;
                // Paso 6c: Tasas = Gramos / 100
                double tasas = gramosAli / 100.0;

                lblResultadoKg.setText(String.format("%.4f Kg  (%.2f g)", kgAli, gramosAli));
                lblResultadoTasas.setText(String.format("%.2f tasas/dia", tasas));

                ultimosResultados.put("RACION",                                "");
                ultimosResultados.put("  Alimento",                            nomAli);
                ultimosResultados.put("  Composicion  Prot / Lip / Carb",      String.format("%.1f%% / %.1f%% / %.1f%%",
                    DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2]));
                ultimosResultados.put("  Paso 4 - DE del alimento",            String.format(
                    "(%.1f*3.5 + %.1f*8.5 + %.1f*3.5)*10 = %.2f Kcal/Kg",
                    DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2], deKcalKg));
                ultimosResultados.put("  Paso 5 - Kg alimento = Energia / DE", String.format(
                    "%.2f / %.2f = %.4f Kg", energiaAdj, deKcalKg, kgAli));
                ultimosResultados.put("  Paso 6 - Gramos = Kg * 1000",         String.format(
                    "%.4f * 1000 = %.2f g", kgAli, gramosAli));
                ultimosResultados.put("  Paso 7 - Tasas = Gramos / 100",       String.format(
                    "%.2f / 100 = %.2f tasas/dia", gramosAli, tasas));
            }

            return energiaAdj;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que el campo peso tenga un valor numerico valido.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Calcula la DE de un concentrado personalizado si los campos estan llenos.
     * DE = [(Prot%*3.5) + (Lip%*8.5) + (Carb%*3.5)] * 10
     */
    private void calcularDensidadPersonalizada() {
        try {
            String tP = txtProtPct.getText().trim().replace(",", ".");
            String tL = txtLipPct.getText().trim().replace(",", ".");
            String tC = txtCarbPct.getText().trim().replace(",", ".");
            if (tP.isEmpty() || tL.isEmpty() || tC.isEmpty()) return;

            double prot = Double.parseDouble(tP);
            double lip  = Double.parseDouble(tL);
            double carb = Double.parseDouble(tC);
            double de   = ((prot * 3.5) + (lip * 8.5) + (carb * 3.5)) * 10.0;

            lblResultadoDensidad.setText(String.format("%.2f Kcal/kg", de));

            ultimosResultados.put("DENSIDAD ENERGETICA (concentrado personalizado)", "");
            ultimosResultados.put("  Composicion Prot / Lip / Carb",
                String.format("%.1f%% / %.1f%% / %.1f%%", prot, lip, carb));
            ultimosResultados.put("  DE = (Prot*3.5 + Lip*8.5 + Carb*3.5) * 10",
                String.format("(%.1f*3.5 + %.1f*8.5 + %.1f*3.5)*10 = %.2f Kcal/kg",
                    prot, lip, carb, de));
        } catch (Exception ignored) { /* campos vacios: omitir */ }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}