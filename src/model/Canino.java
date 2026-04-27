package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class Canino extends Animal {

    // ---- Pestana 1: Calculo de energía y ración ----
    private JTextField txtPeso;
    private JTextField txtPesoAdulto;     // para estados fisiológicos de crecimiento
    private JComboBox<String> cbUnidadPeso;
    private JComboBox<String> cbFormulaEM;
    private JComboBox<String> cbEstadoFisiologico;
    private JComboBox<String> cbAlimento;
    private JLabel lblPesoAdultoLbl;

    // ---- Pestana 2: Pérdida de peso ----
    private JTextField txtPesoActual;
    private JTextField txtPesoIdeal;
    private JTextField txtPorcentajeSemanal;
    private JComboBox<String> cbUnidadPesoActual;
    private JComboBox<String> cbUnidadPesoIdeal;

    // ---- Pestana 3: Densidad energetica de alimento personalizado ----
    private JTextField txtProtPct;
    private JTextField txtLipPct;
    private JTextField txtCarbPct;
    private JTextField txtHumedadPct;
    private JTextField txtCenizaPct;

    private JLabel lblResultadoEnergia;
    private JLabel lblResultadoKg;
    private JLabel lblResultadoTasas;
    private JLabel lblResultadoPerdida;
    private JLabel lblResultadoDensidad;

    // Base de datos: { Proteina%, Lipidos%, Carbohidratos% }
    private static final String[] NOMBRES_ALIM = {
        "Rufo (baja calidad)", "Hills (alta calidad)", "Monello (mediana calidad)"
    };
    private static final double[][] DB_ALIM = {
        {18.0,  9.0,  60.0},
        {25.0, 14.3,  52.1},
        {23.0, 11.0,  52.5}
    };

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
        "Gerontologia (geriatria)",
        "Sobrepeso hasta 20%",
        "Obeso (superior al 20%)"
    };
    private static final double[] FACTORES = {
        2.0, 1.5, 1.2, 1.1, 1.35, 3.0, 1.2, 1.5, 2.5, 0.8, 0.8, 0.7
    };
    // Estados que requieren conocer el peso adulto para determinar el estado
    private static final boolean[] REQUIERE_PESO_ADULTO = {
        true, true, true, false, false, false, false, false, false, false, false, false
    };

    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    public Canino(double peso, String unidad) {
        super("Canino", peso, unidad);

        txtPeso             = new JTextField(8);
        txtPesoAdulto       = new JTextField(8);
        cbUnidadPeso        = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbFormulaEM         = new JComboBox<>(new String[]{
            "132 x PM^0.75  (NRC estandar)",
            "100 x PM^0.88",
            "145 x PM^0.67  (resistencia/carreras)"
        });
        cbEstadoFisiologico = new JComboBox<>(ESTADOS);
        cbAlimento          = new JComboBox<>(NOMBRES_ALIM);

        txtPesoActual        = new JTextField(8);
        txtPesoIdeal         = new JTextField(8);
        txtPorcentajeSemanal = new JTextField("1.5", 5);
        cbUnidadPesoActual   = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbUnidadPesoIdeal    = new JComboBox<>(new String[]{"Kg", "Lb"});

        txtProtPct   = new JTextField(6);
        txtLipPct    = new JTextField(6);
        txtCarbPct   = new JTextField(6);
        txtHumedadPct= new JTextField(6);
        txtCenizaPct = new JTextField(6);

        lblPesoAdultoLbl    = new JLabel("Peso adulto esperado:");
        lblResultadoEnergia = new JLabel("-");
        lblResultadoKg      = new JLabel("-");
        lblResultadoTasas   = new JLabel("-");
        lblResultadoPerdida = new JLabel("-");
        lblResultadoDensidad= new JLabel("-");

        // Mostrar/ocultar campo peso adulto segun estado elegido
        cbEstadoFisiologico.addActionListener(e -> {
            boolean req = REQUIERE_PESO_ADULTO[cbEstadoFisiologico.getSelectedIndex()];
            txtPesoAdulto.setVisible(req);
            lblPesoAdultoLbl.setVisible(req);
        });
        // Valor inicial
        boolean reqInicial = REQUIERE_PESO_ADULTO[0];
        txtPesoAdulto.setVisible(reqInicial);
        lblPesoAdultoLbl.setVisible(reqInicial);
    }

    // -------------------------------------------------------------------------
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));

        // Sub-pestanas dentro del panel
        JTabbedPane subPestanas = new JTabbedPane();
        subPestanas.addTab("Energia y Racion",      crearSubPanelEnergia());
        subPestanas.addTab("Perdida de Peso",        crearSubPanelPerdida());
        subPestanas.addTab("Densidad de Concentrado",crearSubPanelDensidad());

        contenedor.add(subPestanas);
        return contenedor;
    }

    // ----  Energía y ración ----
    private JPanel crearSubPanelEnergia() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tabla base de datos
        String[] colDB = {"Alimento", "Proteina (%)", "Lipidos (%)", "Carb. (%)", "Kcal/kg"};
        Object[][] dataDB = {
            {"Rufo (baja)",    18.0,  9.0,  60.0, String.format("%.1f", calcDE(0))},
            {"Hills (alta)",   25.0, 14.3,  52.1, String.format("%.1f", calcDE(1))},
            {"Monello (media)",23.0, 11.0,  52.5, String.format("%.1f", calcDE(2))}
        };
        JTable tablaDB = new JTable(new javax.swing.table.DefaultTableModel(dataDB, colDB));
        tablaDB.setEnabled(false);
        tablaDB.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tablaDB);
        scroll.setPreferredSize(new Dimension(700, 88));
        scroll.setBorder(BorderFactory.createTitledBorder("Base de Datos - Concentrados Caninos"));
        panel.add(scroll);

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder("Datos del Perro"));
        entradas.add(crearFila("Peso del animal:",         txtPeso,      cbUnidadPeso));
        entradas.add(crearFila("Formula de EM:",           new JLabel("Formula:"), cbFormulaEM));
        entradas.add(crearFila("Estado fisiologico:",      new JLabel("Estado:"),  cbEstadoFisiologico));
        entradas.add(crearFilaOpcional(lblPesoAdultoLbl,   txtPesoAdulto, new JLabel("Kg (peso adulto esperado)")));
        entradas.add(crearFila("Alimento seleccionado:",   new JLabel("Marca:"),   cbAlimento));
        panel.add(entradas);

        // Resultados rapidos
        JPanel res = new JPanel(new GridLayout(3, 2, 6, 4));
        res.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        res.add(new JLabel("Energia requerida:"));
        estilizar(lblResultadoEnergia, new Color(0, 100, 200));
        res.add(lblResultadoEnergia);
        res.add(new JLabel("Alimento necesario:"));
        estilizar(lblResultadoKg, new Color(0, 120, 0));
        res.add(lblResultadoKg);
        res.add(new JLabel("Tasas/porciones por dia:"));
        estilizar(lblResultadoTasas, new Color(160, 80, 0));
        res.add(lblResultadoTasas);
        panel.add(res);
        return panel;
    }

    // ---- Perdida de peso ----
    private JPanel crearSubPanelPerdida() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder(
            "Calculo de Semanas para Perdida de Peso"));
        entradas.add(crearFila("Peso actual del animal:", txtPesoActual, cbUnidadPesoActual));
        entradas.add(crearFila("Peso ideal del animal:",  txtPesoIdeal,  cbUnidadPesoIdeal));
        entradas.add(crearFila("% de perdida semanal:",   txtPorcentajeSemanal, new JLabel("%  (rango: 0.5 - 2%)")));

        JLabel nota = new JLabel("  Rango recomendado de perdida semanal: 0.5% a 2% del peso actual.");
        nota.setFont(new Font("SansSerif", Font.ITALIC, 11));
        nota.setForeground(Color.GRAY);
        entradas.add(nota);
        panel.add(entradas);

        JPanel res = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        res.setBorder(BorderFactory.createTitledBorder("Resultado de Perdida de Peso"));
        res.add(new JLabel("Semanas estimadas para alcanzar peso ideal:"));
        estilizar(lblResultadoPerdida, new Color(150, 0, 150));
        res.add(lblResultadoPerdida);
        panel.add(res);
        return panel;
    }

    // ----  Densidad energetica de concentrado ----
    private JPanel crearSubPanelDensidad() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel entradas = new JPanel();
        entradas.setLayout(new BoxLayout(entradas, BoxLayout.Y_AXIS));
        entradas.setBorder(BorderFactory.createTitledBorder(
            "Densidad Energetica de un Concentrado Personalizado"));

        JLabel explicacion = new JLabel(
            "<html>DE = [ (Proteina% x 3.5) + (Lipidos% x 8.5) + (Carbohidratos% x 3.5) ] x 10<br>" +
            "Los campos de humedad, fibra y ceniza son informativos; los carbohidratos se ingresan directamente.</html>");
        explicacion.setFont(new Font("SansSerif", Font.ITALIC, 11));
        explicacion.setForeground(new Color(60, 60, 120));
        entradas.add(explicacion);
        entradas.add(Box.createVerticalStrut(6));

        entradas.add(crearFila("% Proteina:",      txtProtPct,    new JLabel("%")));
        entradas.add(crearFila("% Lipidos/Grasa:", txtLipPct,     new JLabel("%")));
        entradas.add(crearFila("% Carbohidratos:", txtCarbPct,    new JLabel("%")));
        entradas.add(crearFila("% Humedad:",        txtHumedadPct, new JLabel("% (informativo)")));
        entradas.add(crearFila("% Ceniza:",         txtCenizaPct,  new JLabel("% (informativo)")));
        panel.add(entradas);

        JPanel res = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        res.setBorder(BorderFactory.createTitledBorder("Resultado"));
        res.add(new JLabel("Densidad Energetica:"));
        estilizar(lblResultadoDensidad, new Color(0, 100, 200));
        res.add(lblResultadoDensidad);
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

    private JPanel crearFilaOpcional(JLabel lbl, JComponent campo, JComponent extra) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0.38; p.add(lbl, g);
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

    private double aKgPeso(double valor) {
        return aKg(valor, cbUnidadPeso);
    }

    /** Calcula la densidad energetica (Kcal/kg) para un indice de la base de datos. */
    private double calcDE(int idx) {
        double[] ali = DB_ALIM[idx];
        return ((ali[0] * 3.5) + (ali[1] * 8.5) + (ali[2] * 3.5)) * 10.0;
    }

    @Override
    public double calcularPesoMetabolico() {
        try {
            double kg = aKgPeso(Double.parseDouble(txtPeso.getText().trim().replace(",", ".")));
            return Math.pow(kg, 0.75);
        } catch (Exception e) { return 0; }
    }

    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();

        // Calcular densidad energética del concentrado personalizado si tiene datos
        calcularDensidadPersonalizada();

        // Calcular pérdida de peso si tiene datos
        calcularPerdidaDePeso();

        // Cálculo principal: energia y racion
        try {
            double pesoKg = aKgPeso(Double.parseDouble(txtPeso.getText().trim().replace(",", ".")));
            if (pesoKg <= 0) {
                JOptionPane.showMessageDialog(null, "El peso del animal debe ser mayor a 0.");
                return 0;
            }

            // Formula de EM seleccionada
            int formulaIdx = cbFormulaEM.getSelectedIndex();
            double em;
            String formulaNom;
            switch (formulaIdx) {
                case 1:
                    em = 100.0 * Math.pow(pesoKg, 0.88);
                    formulaNom = "100 x PM^0.88";
                    break;
                case 2:
                    em = 145.0 * Math.pow(pesoKg, 0.67);
                    formulaNom = "145 x PM^0.67";
                    break;
                default:
                    em = 132.0 * Math.pow(pesoKg, 0.75);
                    formulaNom = "132 x PM^0.75";
                    break;
            }

            // Factor fisiológico
            int estadoIdx    = cbEstadoFisiologico.getSelectedIndex();
            String estadoNom = ESTADOS[estadoIdx];
            double factor    = FACTORES[estadoIdx];

            // Si el estado requiere peso adulto, verificar si el factor es correcto
            // segun el porcentaje del peso actual respecto al peso adulto
            if (REQUIERE_PESO_ADULTO[estadoIdx]) {
                String txtPA = txtPesoAdulto.getText().trim().replace(",", ".");
                if (!txtPA.isEmpty()) {
                    double pesoAdulto = Double.parseDouble(txtPA);
                    if (pesoAdulto > 0) {
                        double porcentaje = (pesoKg / pesoAdulto) * 100.0;
                        // Recalcular factor automaticamente segun el porcentaje
                        if (porcentaje <= 40.0) {
                            factor = 2.0; estadoNom = "Hasta 40% del peso adulto (" + String.format("%.1f%%", porcentaje) + ")";
                        } else if (porcentaje <= 80.0) {
                            factor = 1.5; estadoNom = "Entre 40-80% del peso adulto (" + String.format("%.1f%%", porcentaje) + ")";
                        } else {
                            factor = 1.2; estadoNom = "Entre 80%-adulto (" + String.format("%.1f%%", porcentaje) + ")";
                        }
                    }
                }
            }

            double energiaAdj = em * factor;

            // Alimento y racion
            int    aliIdx  = cbAlimento.getSelectedIndex();
            String nomAli  = NOMBRES_ALIM[aliIdx];
            double deKcalKg = calcDE(aliIdx);
            double kgAli   = energiaAdj / deKcalKg;
            double gramosAli = kgAli * 1000.0;
            double tasas   = gramosAli / 100.0;

            lblResultadoEnergia.setText(String.format("%.2f Kcal/dia", energiaAdj));
            lblResultadoKg.setText(String.format("%.4f Kg  (%.1f g)", kgAli, gramosAli));
            lblResultadoTasas.setText(String.format("%.2f tasas/dia", tasas));

            ultimosResultados.put("Peso Vivo",                      String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("Formula EM utilizada",           formulaNom);
            ultimosResultados.put("Peso metabolico",                String.format("%.4f", calcularPesoMetabolico()));
            ultimosResultados.put("ENERGIA",                        "");
            ultimosResultados.put("EM calculada (sin factor)",      String.format("%.2f Kcal/dia", em));
            ultimosResultados.put("Estado fisiologico",             estadoNom);
            ultimosResultados.put("Factor de ajuste",               String.format("x %.2f", factor));
            ultimosResultados.put("Energia requerida (ajustada)",   String.format("%.2f Kcal/dia", energiaAdj));
            ultimosResultados.put("RACION",                         "");
            ultimosResultados.put("Alimento seleccionado",          nomAli);
            ultimosResultados.put("Composicion Prot/Lip/Carb",      String.format("%.1f%% / %.1f%% / %.1f%%",
                DB_ALIM[aliIdx][0], DB_ALIM[aliIdx][1], DB_ALIM[aliIdx][2]));
            ultimosResultados.put("Densidad energetica alimento",   String.format("%.2f Kcal/Kg", deKcalKg));
            ultimosResultados.put("Kg de alimento/dia",             String.format("%.4f Kg", kgAli));
            ultimosResultados.put("Gramos de alimento/dia",         String.format("%.2f g", gramosAli));
            ultimosResultados.put("Tasas (porciones)/dia",          String.format("%.2f tasas", tasas));

            return energiaAdj;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numericos esten completos.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Calcula las semanas necesarias para alcanzar el peso ideal.
     * Semanas = (Peso actual - Peso ideal) / (Peso actual * % semanal / 100)
     * Si hay datos completos, actualiza la etiqueta y agrega al mapa.
     */
    private void calcularPerdidaDePeso() {
        try {
            String tPA = txtPesoActual.getText().trim().replace(",", ".");
            String tPI = txtPesoIdeal.getText().trim().replace(",", ".");
            String tPS = txtPorcentajeSemanal.getText().trim().replace(",", ".");
            if (tPA.isEmpty() || tPI.isEmpty() || tPS.isEmpty()) return;

            double pActual = aKg(Double.parseDouble(tPA), cbUnidadPesoActual);
            double pIdeal  = aKg(Double.parseDouble(tPI), cbUnidadPesoIdeal);
            double pctSem  = Double.parseDouble(tPS);

            if (pActual <= 0 || pIdeal <= 0 || pctSem <= 0) return;

            double perdidaTotal = pActual - pIdeal;
            if (perdidaTotal <= 0) {
                lblResultadoPerdida.setText("El animal ya esta en su peso ideal o por debajo.");
                ultimosResultados.put("PERDIDA DE PESO", "");
                ultimosResultados.put("Peso actual / Peso ideal", String.format("%.2f Kg / %.2f Kg", pActual, pIdeal));
                ultimosResultados.put("Estado", "Ya esta en peso ideal o por debajo");
                return;
            }

            double perdidaSemanal = pActual * (pctSem / 100.0);
            double semanas        = perdidaTotal / perdidaSemanal;

            lblResultadoPerdida.setText(String.format("%.1f semanas  (perdida de %.3f Kg/semana)", semanas, perdidaSemanal));

            ultimosResultados.put("PERDIDA DE PESO", "");
            ultimosResultados.put("Peso actual",               String.format("%.2f Kg", pActual));
            ultimosResultados.put("Peso ideal",                String.format("%.2f Kg", pIdeal));
            ultimosResultados.put("Perdida total necesaria",   String.format("%.2f Kg", perdidaTotal));
            ultimosResultados.put("Porcentaje semanal",        String.format("%.1f%%", pctSem));
            ultimosResultados.put("Perdida por semana",        String.format("%.3f Kg/semana", perdidaSemanal));
            ultimosResultados.put("Semanas estimadas",         String.format("%.1f semanas", semanas));

        } catch (Exception ignored) { /* campos vacios, no calcular */ }
    }

    /**
     * Calcula la densidad energetica de un concentrado con composicion personalizada.
     * DE = [ (Prot% * 3.5) + (Lip% * 8.5) + (Carb% * 3.5) ] * 10
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
            ultimosResultados.put("Composicion Proteina/Lipidos/Carb",
                String.format("%.1f%% / %.1f%% / %.1f%%", prot, lip, carb));
            ultimosResultados.put("DE = (Prot*3.5 + Lip*8.5 + Carb*3.5) * 10",
                String.format("%.2f Kcal/kg", de));

        } catch (Exception ignored) { /* campos vacios, no calcular */ }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}