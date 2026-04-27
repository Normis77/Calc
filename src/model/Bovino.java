package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * ============================================================
 *  Formulación de Raciones para Leche
 * ============================================================
 *
 * PASO 1 - REQUERIMIENTOS DE MANTENIMIENTO :
 *   a. Peso Kg (si viene en Lb: dividir entre 2.2)
 *   b. Peso Metabolico = PesoKg ^ 0.75
 *   c. Energia ENL     = PM * 0.293           [MJ ENL/dia]
 *   d. Proteina mant.  = 425 g para los primeros 500 Kg
 *                      + 0.5 g por cada Kg adicional sobre 500
 *   e. Minerales (por litro de leche que produce):
 *        Ca = litros * 3.2  g
 *        P  = litros * 1.7  g
 *        Mg = litros * 0.6  g
 *        Na = litros * 0.6  g
 *
 * PASO 2 - TIPO DE LECHE (por % de grasa):
 *   3.5% -> Tipo I:   2.97 MJ ENL/litro,  80 g proteina/litro
 *   4.0% -> Tipo II:  3.17 MJ ENL/litro,  85 g proteina/litro
 *   4.5% -> Tipo III: 3.37 MJ ENL/litro,  90 g proteina/litro
 *
 * PASO 3 - ANALISIS DE LA RACION:
 *   El usuario elige cada ingrediente de la base de datos
 *   e ingresa los Kg suministrados.
 *   Proteina de cada ingrediente: PC% * 10 = g/Kg
 *   Energia de cada ingrediente:  Kg * MJ_ENL/Kg
 *   Proteina de cada ingrediente: Kg * g_proteina/Kg
 *   Se suman los aportes de todos los ingredientes.
 *
 * PASO 4 - EVALUACION DE LA RACIÓN:
 *   Energia disponible para producir  = Energia_racion  - Energia_mant
 *   Proteina disponible para producir = Proteina_racion - Proteina_mant
 *   Litros producibles con energia    = Energia_disp  / MJ_por_litro
 *   Litros producibles con proteina   = Proteina_disp / g_por_litro
 *
 * PASO 5 - AJUSTE F1/F2 (AUTOMÁTICO):
 *   Si litros_energia != litros_proteina (diferencia > 0.01):
 *     F1 = litros_proteina - litros_energia
 *     (si F1 > 0: energia es el factor limitante -> agregar alimento energetico)
 *     (si F1 < 0: proteina es el factor limitante -> agregar alimento proteico)
 *   El usuario elige el alimento de ajuste del ComboBox.
 *   F2 = (MJ_ajuste / MJ_por_litro) - (PC_ajuste / g_por_litro)
 *   Kg de ajuste = F1 / F2
 *   Se verifica recalculando la racion con el ajuste incluido.
 *
 * CONVERSION: Kg <-> Lb (1 kg = 2.2 lb)
 *
 *
 * ============================================================
 */
public class Bovino extends Animal {

    // ----------------------------------------------------------
    // Componentes Swing - inicializados UNA VEZ en constructor
    // ----------------------------------------------------------
    private JTextField        txtPeso;
    private JTextField        txtLitrosLeche;
    private JTextField        txtGrasaLeche;
    private JComboBox<String> cbUnidadPeso;

    // Hasta 4 ingredientes de la racion base: combo + Kg suministrados
    private static final int MAX_ING = 4;
    private JComboBox<String>[] cbIngrediente = new JComboBox[MAX_ING];
    private JTextField[]        txtKgIng      = new JTextField[MAX_ING];

    // Ingrediente de ajuste F1/F2
    private JComboBox<String> cbIngAjuste;

    // Etiquetas de resultado
    private JLabel lblResMantenimiento;
    private JLabel lblResRacion;
    private JLabel lblResDisponible;
    private JLabel lblResLitros;
    private JLabel lblResAjuste;
    private JLabel lblResFinal;

    // ----------------------------------------------------------
    // BASE DE DATOS BOVINOS
    // { PC%, MJ ENL/Kg }
    // PC en % -> para obtener g/Kg multiplicar por 10
    // ----------------------------------------------------------
    private static final String[] NOMBRES_ING = {
        "-- Seleccione ingrediente --",
        "Heno 1er corte   (PC 5.5%,  2.10 MJ/Kg)",
        "Heno 2do corte   (PC 11.5%, 4.40 MJ/Kg)",
        "Soya             (PC 45%,   7.10 MJ/Kg)",
        "Cebada           (PC 10.5%, 7.25 MJ/Kg)",
        "Alfalfa          (PC 3.3%,  2.20 MJ/Kg)",
        "Avena            (PC 14.8%, 7.70 MJ/Kg)",
        "Concentrado      (PC 22%,   6.50 MJ/Kg)"
    };
    // { PC%, MJ ENL/Kg } - indice 0 es "Seleccione" (valores cero)
    private static final double[][] DB_ING = {
        {  0.0,  0.00},   // placeholder "Seleccione"
        {  5.5,  2.10},   // Heno 1er corte
        { 11.5,  4.40},   // Heno 2do corte
        { 45.0,  7.10},   // Soya
        { 10.5,  7.25},   // Cebada
        {  3.3,  2.20},   // Alfalfa
        { 14.8,  7.70},   // Avena
        { 22.0,  6.50}    // Concentrado
    };

    // Tabla de tipos de leche: { % grasa limite sup, MJ/litro, g prot/litro }
    private static final double[][] TIPOS_LECHE = {
        {3.75, 2.97, 80.0},   // Tipo I   (3.5% grasa)
        {4.25, 3.17, 85.0},   // Tipo II  (4.0% grasa)
        {9.99, 3.37, 90.0}    // Tipo III (4.5% grasa)
    };

    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================
    @SuppressWarnings("unchecked")
    public Bovino(double peso, String unidad) {
        super("Bovino", peso, unidad);

        txtPeso        = new JTextField(8);
        txtLitrosLeche = new JTextField(6);
        txtGrasaLeche  = new JTextField("4.0", 5);
        cbUnidadPeso   = new JComboBox<>(new String[]{"Kg", "Lb"});

        for (int i = 0; i < MAX_ING; i++) {
            cbIngrediente[i] = new JComboBox<>(NOMBRES_ING);
            txtKgIng[i]      = new JTextField(6);
        }
        // Preselecciones comunes para los primeros dos ingredientes
        cbIngrediente[0].setSelectedIndex(1); // Heno 1er corte
        cbIngrediente[1].setSelectedIndex(4); // Cebada

        cbIngAjuste = new JComboBox<>(NOMBRES_ING);
        cbIngAjuste.setSelectedIndex(4); // Cebada por defecto (tipico ajuste energetico)

        lblResMantenimiento = new JLabel("-");
        lblResRacion        = new JLabel("-");
        lblResDisponible    = new JLabel("-");
        lblResLitros        = new JLabel("-");
        lblResAjuste        = new JLabel("-");
        lblResFinal         = new JLabel("-");
    }

    // ==========================================================
    // PANEL VISUAL
    // ==========================================================
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Tabla de tipos de leche (referencia)
        String[] colLeche = {"% Grasa", "Tipo", "MJ ENL/litro", "Proteina g/litro"};
        Object[][] dataLeche = {
            {"3.5%", "I",   "2.97", "80"},
            {"4.0%", "II",  "3.17", "85"},
            {"4.5%", "III", "3.37", "90"}
        };
        JTable tablaLeche = new JTable(
            new javax.swing.table.DefaultTableModel(dataLeche, colLeche));
        tablaLeche.setEnabled(false);
        tablaLeche.setRowHeight(22);
        JScrollPane scrollLeche = new JScrollPane(tablaLeche);
        scrollLeche.setPreferredSize(new Dimension(500, 88));
        scrollLeche.setBorder(BorderFactory.createTitledBorder(
            "Referencia - Requerimientos por Litro segun % Grasa de la Leche"));
        contenedor.add(scrollLeche);

        // Datos del animal
        JPanel panelAnimal = new JPanel();
        panelAnimal.setLayout(new BoxLayout(panelAnimal, BoxLayout.Y_AXIS));
        panelAnimal.setBorder(BorderFactory.createTitledBorder("Datos del Animal"));
        panelAnimal.add(crearFila("Peso vivo:",            txtPeso,        cbUnidadPeso));
        panelAnimal.add(crearFila("Litros de leche/dia:",  txtLitrosLeche, new JLabel("litros")));
        panelAnimal.add(crearFila("% grasa de la leche:",  txtGrasaLeche,  new JLabel("%")));
        contenedor.add(panelAnimal);

        // Racion ofrecida: ComboBox de ingredientes + Kg
        JPanel panelRacion = new JPanel(new GridBagLayout());
        panelRacion.setBorder(BorderFactory.createTitledBorder(
            "Racion Ofrecida - Elija ingredientes de la base de datos"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 5, 3, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Encabezados de columna
        gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0.70;
        panelRacion.add(new JLabel("Ingrediente (base de datos)"), gc);
        gc.gridx = 1; gc.weightx = 0.30;
        panelRacion.add(new JLabel("Kg suministrados"), gc);

        for (int i = 0; i < MAX_ING; i++) {
            gc.gridy = i + 1;
            gc.gridx = 0; gc.weightx = 0.70;
            panelRacion.add(cbIngrediente[i], gc);
            gc.gridx = 1; gc.weightx = 0.30;
            panelRacion.add(txtKgIng[i], gc);
        }
        JLabel notaRac = new JLabel(
            "  Deje en blanco (o elija '--') los ingredientes que no use.");
        notaRac.setFont(new Font("SansSerif", Font.ITALIC, 11));
        notaRac.setForeground(Color.GRAY);
        gc.gridy = MAX_ING + 1; gc.gridx = 0; gc.gridwidth = 2;
        panelRacion.add(notaRac, gc);
        contenedor.add(panelRacion);

        // Alimento de ajuste F1/F2
        JPanel panelAjuste = new JPanel();
        panelAjuste.setLayout(new BoxLayout(panelAjuste, BoxLayout.Y_AXIS));
        panelAjuste.setBorder(BorderFactory.createTitledBorder(
            "Alimento de Ajuste F1/F2 (el programa lo usa automaticamente si hay desequilibrio)"));
        panelAjuste.add(crearFila("Alimento de ajuste:", cbIngAjuste, new JLabel("")));
        JLabel notaAj = new JLabel(
            "  Si energia < proteina: use alimento energetico (ej. Cebada, Avena).  " +
            "Si proteina < energia: use alimento proteico (ej. Soya).");
        notaAj.setFont(new Font("SansSerif", Font.ITALIC, 11));
        notaAj.setForeground(Color.GRAY);
        panelAjuste.add(notaAj);
        contenedor.add(panelAjuste);

        // Resultados rapidos en pantalla
        JPanel panelRes = new JPanel(new GridLayout(6, 1, 2, 2));
        panelRes.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        Font mf = new Font("Monospaced", Font.PLAIN, 11);
        lblResMantenimiento.setFont(mf);
        lblResRacion.setFont(mf);
        lblResDisponible.setFont(mf);
        estilizar(lblResLitros,   new Color(0, 100, 200));
        estilizar(lblResAjuste,   new Color(150, 80, 0));
        estilizar(lblResFinal,    new Color(0, 120, 0));
        panelRes.add(lblResMantenimiento);
        panelRes.add(lblResRacion);
        panelRes.add(lblResDisponible);
        panelRes.add(lblResLitros);
        panelRes.add(lblResAjuste);
        panelRes.add(lblResFinal);
        contenedor.add(panelRes);

        return contenedor;
    }

    private JPanel crearFila(String titulo, JComponent campo, JComponent extra) {
        return crearFila(new JLabel(titulo), campo, extra);
    }

    private JPanel crearFila(JComponent etiqueta, JComponent campo, JComponent extra) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0.40; p.add(etiqueta, g);
        g.gridx = 1; g.weightx = 0.45; p.add(campo, g);
        g.gridx = 2; g.weightx = 0.15; p.add(extra, g);
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

    /** Convierte PC% a g/Kg: multiplicar por 10. */
    private double pcPctAGkg(double pct) { return pct * 10.0; }

    /**
     * Determina el tipo de leche segun el % de grasa.
     * Retorna { MJ ENL/litro, g proteina/litro }
     */
    private double[] tipLeche(double grasaPct) {
        for (double[] t : TIPOS_LECHE) {
            if (grasaPct <= t[0]) return new double[]{t[1], t[2]};
        }
        return new double[]{TIPOS_LECHE[2][1], TIPOS_LECHE[2][2]};
    }

    private String nomTipoLeche(double grasaPct) {
        if (grasaPct <= 3.75) return "Tipo I  (3.5% grasa): 2.97 MJ/L, 80 g prot/L";
        if (grasaPct <= 4.25) return "Tipo II (4.0% grasa): 3.17 MJ/L, 85 g prot/L";
        return                       "Tipo III(4.5% grasa): 3.37 MJ/L, 90 g prot/L";
    }

    @Override
    public double calcularPesoMetabolico() {
        try {
            return Math.pow(aKg(Double.parseDouble(
                txtPeso.getText().trim().replace(",", "."))), 0.75);
        } catch (Exception e) { return 0; }
    }

    // ==========================================================
    //  CALCULO PRINCIPAL
    // ==========================================================
    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();
        try {
            // ==================================================
            // PASO 1: DATOS DEL ANIMAL
            // ==================================================
            double pesoKg   = aKg(Double.parseDouble(
                txtPeso.getText().trim().replace(",", ".")));
            double litros   = Double.parseDouble(
                txtLitrosLeche.getText().trim().replace(",", "."));
            double grasaPct = Double.parseDouble(
                txtGrasaLeche.getText().trim().replace(",", "."));

            if (pesoKg <= 0 || litros < 0 || grasaPct <= 0) {
                JOptionPane.showMessageDialog(null,
                    "Peso, litros y % grasa deben ser valores positivos.");
                return 0;
            }

            // ==================================================
            // PASO 2: REQUERIMIENTOS DE MANTENIMIENTO
            // ==================================================

            // 2a. Peso Metabolico
            double pm = Math.pow(pesoKg, 0.75);

            // 2b. Energia de mantenimiento
            //     ENL_mant = PM * 0.293  [MJ ENL]
            double enlMant = pm * 0.293;

            // 2c. Proteina de mantenimiento
            //     425 g para los primeros 500 Kg
            //     + 0.5 g por cada Kg adicional sobre 500
            double protMant;
            if (pesoKg <= 500.0) {
                // Regla de 3 para animales menores a 500 Kg
                protMant = 425.0 * (pesoKg / 500.0);
            } else {
                protMant = 425.0 + (pesoKg - 500.0) * 0.5;
            }

            // 2d. Minerales (por litro de leche producida)
            double rCa = litros * 3.2;
            double rP  = litros * 1.7;
            double rMg = litros * 0.6;
            double rNa = litros * 0.6;

            // ==================================================
            // PASO 3: TIPO DE LECHE
            // ==================================================
            double[] tipReqs    = tipLeche(grasaPct);
            double   mjPorLitro = tipReqs[0];   // MJ ENL necesarios por litro
            double   gPorLitro  = tipReqs[1];   // g proteina necesarios por litro
            String   nomTipo    = nomTipoLeche(grasaPct);

            // ==================================================
            // PASO 4: ANALISIS DE LA RACION BASE
            // ==================================================
            double enlRacion  = 0.0;
            double protRacion = 0.0;
            int    nIngUsados = 0;

            // Recorrer los 4 ingredientes posibles
            for (int i = 0; i < MAX_ING; i++) {
                int selIdx = cbIngrediente[i].getSelectedIndex();
                // Indice 0 = "Seleccione" -> omitir
                if (selIdx == 0) continue;
                String tKg = txtKgIng[i].getText().trim().replace(",", ".");
                if (tKg.isEmpty()) continue;
                double kg = Double.parseDouble(tKg);
                if (kg <= 0) continue;

                double pcPct = DB_ING[selIdx][0];   // % proteina
                double mj    = DB_ING[selIdx][1];   // MJ ENL/Kg

                // Aporte energetico: Kg * MJ/Kg
                enlRacion  += kg * mj;
                // Aporte proteico:  Kg * (PC% * 10) = Kg * g/Kg
                protRacion += kg * pcPctAGkg(pcPct);
                nIngUsados++;
            }

            if (nIngUsados == 0) {
                JOptionPane.showMessageDialog(null,
                    "Ingrese al menos un ingrediente en la racion " +
                    "(elija del ComboBox e ingrese los Kg).");
                return 0;
            }

            // ==================================================
            // PASO 5: EVALUACION DE LA RACION
            //   Disponible para producir = Aporte_racion - Mantenimiento
            // ==================================================
            double enlDisp  = enlRacion  - enlMant;
            double protDisp = protRacion - protMant;

            // Litros producibles con cada nutriente
            double litrosEnl  = enlDisp  > 0 ? enlDisp  / mjPorLitro : 0.0;
            double litrosProt = protDisp > 0 ? protDisp / gPorLitro  : 0.0;

            // ==================================================
            // PASO 6: AJUSTE F1/F2 (AUTOMATICO)
            //   Solo se ejecuta si hay desequilibrio y el usuario
            //   selecciono un alimento de ajuste valido.
            // ==================================================
            int    ajusteIdx     = cbIngAjuste.getSelectedIndex();
            double kgAjuste      = 0.0;
            double litrosEnlFin  = litrosEnl;
            double litrosProtFin = litrosProt;
            String mensajeAjuste = "No se requiere ajuste (litros balanceados).";
            boolean ajusteHecho  = false;

            if (ajusteIdx > 0 && litrosEnl > 0 && litrosProt > 0
                    && Math.abs(litrosEnl - litrosProt) > 0.01) {

                // F1 = litrosProt - litrosEnl
                // Si F1 > 0 : energia es el factor limitante (se produce menos con energia)
                //             -> el alimento de ajuste debe ser rico en energia
                // Si F1 < 0 : proteina es el factor limitante
                //             -> el alimento de ajuste debe ser rico en proteina
                double f1 = litrosProt - litrosEnl;

                double ajPcPct = DB_ING[ajusteIdx][0];   // % PC del alimento de ajuste
                double ajMj    = DB_ING[ajusteIdx][1];   // MJ/Kg del alimento de ajuste

                // Litros que produce 1 Kg del alimento de ajuste
                double litrosPor1KgEnl  = ajMj            / mjPorLitro;
                double litrosPor1KgProt = pcPctAGkg(ajPcPct) / gPorLitro;

                // F2 = diferencia de litros entre energia y proteina para 1 Kg del ajuste
                double f2 = litrosPor1KgEnl - litrosPor1KgProt;

                if (Math.abs(f2) > 0.001) {
                    // Kg de ajuste = F1 / F2
                    kgAjuste = f1 / f2;

                    if (kgAjuste > 0) {
                        // Recalcular totales con el ajuste incluido
                        double enlTot2  = enlRacion  + kgAjuste * ajMj;
                        double protTot2 = protRacion + kgAjuste * pcPctAGkg(ajPcPct);
                        double enlD2    = enlTot2  - enlMant;
                        double protD2   = protTot2 - protMant;
                        litrosEnlFin  = enlD2  > 0 ? enlD2  / mjPorLitro : 0;
                        litrosProtFin = protD2 > 0 ? protD2 / gPorLitro  : 0;

                        mensajeAjuste = String.format(
                            "F1=%.2f  F2=%.2f  -> Agregar %.4f Kg de: %s",
                            f1, f2, kgAjuste, NOMBRES_ING[ajusteIdx].trim());
                        ajusteHecho = true;
                    } else {
                        mensajeAjuste = "Ajuste no aplicable (kgAjuste <= 0). " +
                            "Revise el tipo de alimento de ajuste.";
                    }
                } else {
                    mensajeAjuste = "F2 = 0: el alimento de ajuste no produce diferencia. " +
                        "Elija otro ingrediente.";
                }
            } else if (litrosEnl > 0 && litrosProt > 0
                    && Math.abs(litrosEnl - litrosProt) <= 0.01) {
                mensajeAjuste = "Los litros con energia y proteina estan equilibrados.";
            }

            // ==================================================
            // ACTUALIZAR ETIQUETAS EN PANTALLA
            // ==================================================
            lblResMantenimiento.setText(String.format(
                "Mant: ENL=%.2f MJ  Prot=%.0f g  |  Min: Ca=%.1f P=%.1f Mg=%.1f Na=%.1f g",
                enlMant, protMant, rCa, rP, rMg, rNa));
            lblResRacion.setText(String.format(
                "Racion: ENL=%.2f MJ  Prot=%.0f g",
                enlRacion, protRacion));
            lblResDisponible.setText(String.format(
                "Disponible producir: ENL=%.2f MJ  Prot=%.0f g",
                enlDisp, protDisp));
            lblResLitros.setText(String.format(
                "Litros: con energia=%.2f  /  con proteina=%.2f",
                litrosEnl, litrosProt));
            lblResAjuste.setText(mensajeAjuste);
            lblResFinal.setText(ajusteHecho ? String.format(
                "Tras ajuste: litros energia=%.2f  litros proteina=%.2f  (produccion estimada: %.2f L)",
                litrosEnlFin, litrosProtFin, litrosEnlFin) : "");

            // ==================================================
            // POBLAR MAPA PARA LA JTABLE
            // ==================================================
            ultimosResultados.put("Peso Vivo",                     String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("Litros de leche / dia",         String.format("%.1f litros", litros));
            ultimosResultados.put("% Grasa / Tipo de leche",       String.format("%.1f%% -> %s", grasaPct, nomTipo));

            ultimosResultados.put("PASO 2 - MANTENIMIENTO",        "");
            ultimosResultados.put("  Peso Metabolico (PV^0.75)",   String.format("%.2f ^ 0.75 = %.4f", pesoKg, pm));
            ultimosResultados.put("  Energia ENL = PM * 0.293",    String.format("%.4f * 0.293 = %.4f MJ ENL", pm, enlMant));
            if (pesoKg <= 500.0) {
                ultimosResultados.put("  Proteina mant (proporcional)", String.format("425 * (%.1f/500) = %.2f g", pesoKg, protMant));
            } else {
                ultimosResultados.put("  Proteina mant (500kg base)", String.format("425 + (%.1f-500)*0.5 = %.2f g", pesoKg, protMant));
            }
            ultimosResultados.put("  Calcio  (litros * 3.2)",     String.format("%.1f * 3.2 = %.2f g", litros, rCa));
            ultimosResultados.put("  Fosforo (litros * 1.7)",     String.format("%.1f * 1.7 = %.2f g", litros, rP));
            ultimosResultados.put("  Magnesio(litros * 0.6)",     String.format("%.1f * 0.6 = %.2f g", litros, rMg));
            ultimosResultados.put("  Sodio   (litros * 0.6)",     String.format("%.1f * 0.6 = %.2f g", litros, rNa));

            ultimosResultados.put("PASO 3 - TIPO DE LECHE",        "");
            ultimosResultados.put("  MJ ENL requeridos por litro", String.format("%.2f MJ", mjPorLitro));
            ultimosResultados.put("  Proteina requerida por litro",String.format("%.0f g", gPorLitro));

            ultimosResultados.put("PASO 4 - ANALISIS DE RACION",   "");
            for (int i = 0; i < MAX_ING; i++) {
                int selIdx = cbIngrediente[i].getSelectedIndex();
                if (selIdx == 0) continue;
                String tKg = txtKgIng[i].getText().trim().replace(",", ".");
                if (tKg.isEmpty()) continue;
                double kg = Double.parseDouble(tKg);
                if (kg <= 0) continue;
                double pcPct = DB_ING[selIdx][0];
                double mj    = DB_ING[selIdx][1];
                ultimosResultados.put("  Ing " + (i+1) + " - " + NOMBRES_ING[selIdx].trim(),
                    String.format("%.1f Kg * %.2f MJ = %.2f MJ  |  %.1f Kg * %.0f g = %.0f g",
                        kg, mj, kg*mj, kg, pcPctAGkg(pcPct), kg*pcPctAGkg(pcPct)));
            }
            ultimosResultados.put("  Total racion - Energia",     String.format("%.4f MJ ENL", enlRacion));
            ultimosResultados.put("  Total racion - Proteina",    String.format("%.2f g", protRacion));

            ultimosResultados.put("PASO 5 - EVALUACION",          "");
            ultimosResultados.put("  Disponible energia",         String.format("%.4f - %.4f = %.4f MJ", enlRacion,  enlMant,  enlDisp));
            ultimosResultados.put("  Disponible proteina",        String.format("%.2f - %.2f = %.2f g",  protRacion, protMant, protDisp));
            ultimosResultados.put("  Litros con energia",         String.format("%.4f / %.2f = %.4f litros", enlDisp,  mjPorLitro, litrosEnl));
            ultimosResultados.put("  Litros con proteina",        String.format("%.2f / %.0f = %.4f litros", protDisp, gPorLitro,  litrosProt));

            if (ajusteHecho) {
                ultimosResultados.put("PASO 6 - AJUSTE F1/F2",   "");
                ultimosResultados.put("  Alimento de ajuste",    NOMBRES_ING[ajusteIdx].trim());
                ultimosResultados.put("  F1 (dif. litros)",      String.format("%.4f - %.4f = %.4f", litrosProt, litrosEnl, litrosProt - litrosEnl));
                ultimosResultados.put("  F2 (dif. litros/kg aj.)",String.format("%.4f - %.4f = %.4f",
                    DB_ING[ajusteIdx][1]/mjPorLitro, pcPctAGkg(DB_ING[ajusteIdx][0])/gPorLitro,
                    DB_ING[ajusteIdx][1]/mjPorLitro - pcPctAGkg(DB_ING[ajusteIdx][0])/gPorLitro));
                ultimosResultados.put("  Kg a agregar = F1/F2",  String.format("%.4f Kg", kgAjuste));
                ultimosResultados.put("  Litros finales energia",String.format("%.4f litros", litrosEnlFin));
                ultimosResultados.put("  Litros finales proteina",String.format("%.4f litros", litrosProtFin));
                ultimosResultados.put("  Produccion estimada",   String.format("%.2f litros/dia (base energia)", litrosEnlFin));
            } else {
                ultimosResultados.put("PASO 6 - AJUSTE F1/F2",   mensajeAjuste);
            }

            return enlMant;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numericos esten completos.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}