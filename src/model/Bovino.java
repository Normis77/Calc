package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class Bovino extends Animal {

    // Datos generales del animal
    private JTextField txtPeso;
    private JTextField txtLitrosLeche;    // litros que produce (para minerales)
    private JTextField txtGrasaLeche;     // % grasa de la leche
    private JComboBox<String> cbUnidadPeso;

    // Ingredientes de la racion (hasta 3 ingredientes)
    private JTextField[] txtKgIng      = new JTextField[3];
    private JTextField[] txtPcPctIng   = new JTextField[3];   // % de proteina
    private JTextField[] txtMjIng      = new JTextField[3];   // MJ ENL/Kg

    // Alimento de ajuste F1/F2
    private JTextField txtAjustePcPct;
    private JTextField txtAjusteMj;
    private JLabel lblNombreAjuste;

    // Etiquetas de resultado
    private JLabel lblResMantenimiento;
    private JLabel lblResRacion;
    private JLabel lblResLitros;
    private JLabel lblResAjuste;

    // Tabla de tipo de leche: { % grasa, MJ ENL/litro, g proteina/litro }
    private static final double[][] TIPOS_LECHE = {
        {3.5, 2.97, 80.0},
        {4.0, 3.17, 85.0},
        {4.5, 3.37, 90.0}
    };

    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    public Bovino(double peso, String unidad) {
        super("Bovino", peso, unidad);

        txtPeso        = new JTextField(8);
        txtLitrosLeche = new JTextField(6);
        txtGrasaLeche  = new JTextField("4.0", 5);
        cbUnidadPeso   = new JComboBox<>(new String[]{"Kg", "Lb"});

        for (int i = 0; i < 3; i++) {
            txtKgIng[i]    = new JTextField(6);
            txtPcPctIng[i] = new JTextField(6);
            txtMjIng[i]    = new JTextField(6);
        }

        txtAjustePcPct   = new JTextField(6);
        txtAjusteMj      = new JTextField(6);
        lblNombreAjuste  = new JLabel("Alimento de ajuste:");

        lblResMantenimiento = new JLabel("-");
        lblResRacion        = new JLabel("-");
        lblResLitros        = new JLabel("-");
        lblResAjuste        = new JLabel("-");
    }

    // -------------------------------------------------------------------------
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Tabla de tipos de leche 
        String[] colLeche = {"% Grasa", "Tipo", "MJ ENL/litro", "Proteina g/litro"};
        Object[][] dataLeche = {
            {"3.5%", "I",   "2.97", "80"},
            {"4.0%", "II",  "3.17", "85"},
            {"4.5%", "III", "3.37", "90"}
        };
        JTable tablaLeche = new JTable(new javax.swing.table.DefaultTableModel(dataLeche, colLeche));
        tablaLeche.setEnabled(false);
        tablaLeche.setRowHeight(22);
        JScrollPane scrollLeche = new JScrollPane(tablaLeche);
        scrollLeche.setPreferredSize(new Dimension(500, 88));
        scrollLeche.setBorder(BorderFactory.createTitledBorder(
            "Referencia - Requerimientos por Litro de Leche segun % de Grasa"));
        contenedor.add(scrollLeche);

        // Datos generales del animal
        JPanel panelAnimal = new JPanel();
        panelAnimal.setLayout(new BoxLayout(panelAnimal, BoxLayout.Y_AXIS));
        panelAnimal.setBorder(BorderFactory.createTitledBorder("Datos del Animal"));
        panelAnimal.add(crearFila("Peso vivo:",         txtPeso,        cbUnidadPeso));
        panelAnimal.add(crearFila("Litros de leche/dia:",txtLitrosLeche, new JLabel("litros")));
        panelAnimal.add(crearFila("% grasa de la leche:",txtGrasaLeche, new JLabel("% (para determinar tipo de leche)")));
        contenedor.add(panelAnimal);

        // Racion ofrecida (hasta 3 ingredientes)
        JPanel panelRacion = new JPanel(new GridBagLayout());
        panelRacion.setBorder(BorderFactory.createTitledBorder(
            "Racion Ofrecida (ingrese los ingredientes disponibles)"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 5, 3, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Encabezados
        gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0.30; panelRacion.add(new JLabel("Ingrediente"), gc);
        gc.gridx = 1; gc.weightx = 0.20; panelRacion.add(new JLabel("Kg suministrados"), gc);
        gc.gridx = 2; gc.weightx = 0.20; panelRacion.add(new JLabel("% Proteina (PC)"), gc);
        gc.gridx = 3; gc.weightx = 0.30; panelRacion.add(new JLabel("MJ ENL/Kg"), gc);

        String[] nombresIng = {"Ingrediente 1 (ej. Ensilado):", "Ingrediente 2 (ej. Heno):", "Ingrediente 3 (opcional):"};
        for (int i = 0; i < 3; i++) {
            gc.gridy = i + 1;
            gc.gridx = 0; panelRacion.add(new JLabel(nombresIng[i]), gc);
            gc.gridx = 1; panelRacion.add(txtKgIng[i], gc);
            gc.gridx = 2; panelRacion.add(txtPcPctIng[i], gc);
            gc.gridx = 3; panelRacion.add(txtMjIng[i], gc);
        }
        contenedor.add(panelRacion);

        // Alimento de ajuste F1/F2
        JPanel panelAjuste = new JPanel();
        panelAjuste.setLayout(new BoxLayout(panelAjuste, BoxLayout.Y_AXIS));
        panelAjuste.setBorder(BorderFactory.createTitledBorder(
            "Alimento de Ajuste F1/F2 (ej. Cebada para energia, Soya para proteina)"));
        panelAjuste.add(crearFila("% Proteina del ajuste:", txtAjustePcPct, new JLabel("% PC")));
        panelAjuste.add(crearFila("MJ ENL/Kg del ajuste:",  txtAjusteMj,   new JLabel("MJ ENL/Kg")));
        JLabel notaAj = new JLabel("  Ejemplo cebada: 10.5% proteina, 7.25 MJ ENL.  Deje vacio si no hay ajuste.");
        notaAj.setFont(new Font("SansSerif", Font.ITALIC, 11));
        notaAj.setForeground(Color.GRAY);
        panelAjuste.add(notaAj);
        contenedor.add(panelAjuste);

        // Panel de resultados rapidos
        JPanel panelRes = new JPanel(new GridLayout(4, 1, 4, 4));
        panelRes.setBorder(BorderFactory.createTitledBorder("Resultado (presione Realizar Calculo)"));
        lblResMantenimiento.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblResRacion.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblResLitros.setFont(new Font("Monospaced", Font.BOLD, 12));
        lblResLitros.setForeground(new Color(0, 100, 200));
        lblResAjuste.setFont(new Font("Monospaced", Font.PLAIN, 11));
        panelRes.add(lblResMantenimiento);
        panelRes.add(lblResRacion);
        panelRes.add(lblResLitros);
        panelRes.add(lblResAjuste);
        contenedor.add(panelRes);

        return contenedor;
    }

    private JPanel crearFila(String titulo, JComponent campo, JComponent extra) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 0.40; p.add(new JLabel(titulo), g);
        g.gridx = 1; g.weightx = 0.35; p.add(campo, g);
        g.gridx = 2; g.weightx = 0.25; p.add(extra, g);
        return p;
    }

    private double aKg(double valor) {
        return "Lb".equals(cbUnidadPeso.getSelectedItem()) ? valor / 2.2 : valor;
    }

    /** Convierte % de proteina a g/Kg: PC% * 10 */
    private double pcPctAGramos(double pct) { return pct * 10.0; }

    /** Determina el tipo de leche segun % grasa. Retorna { MJ/litro, g prot/litro } */
    private double[] tipoLeche(double grasaPct) {
        if (grasaPct <= 3.75)       return new double[]{TIPOS_LECHE[0][1], TIPOS_LECHE[0][2]};
        else if (grasaPct <= 4.25)  return new double[]{TIPOS_LECHE[1][1], TIPOS_LECHE[1][2]};
        else                        return new double[]{TIPOS_LECHE[2][1], TIPOS_LECHE[2][2]};
    }

    private String nombreTipoLeche(double grasaPct) {
        if (grasaPct <= 3.75)       return "Tipo I  (3.5% grasa)";
        else if (grasaPct <= 4.25)  return "Tipo II (4.0% grasa)";
        else                        return "Tipo III(4.5% grasa)";
    }

    @Override public double calcularPesoMetabolico() {
        try {
            double kg = aKg(Double.parseDouble(txtPeso.getText().trim().replace(",",".")));
            return Math.pow(kg, 0.75);
        } catch (Exception e) { return 0; }
    }

    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();
        try {
            // -------  Datos del animal -----------------------------------------
            double pesoKg  = aKg(Double.parseDouble(txtPeso.getText().trim().replace(",",".")));
            double litros  = Double.parseDouble(txtLitrosLeche.getText().trim().replace(",","."));
            double grasaPct = Double.parseDouble(txtGrasaLeche.getText().trim().replace(",","."));

            if (pesoKg <= 0 || litros < 0 || grasaPct <= 0) {
                JOptionPane.showMessageDialog(null,
                    "Peso, litros y % grasa deben ser valores positivos.");
                return 0;
            }

            // ------- Requerimientos de mantenimiento -------------------------
            double pm          = Math.pow(pesoKg, 0.75);
            double enlMant     = pm * 0.293;   // MJ ENL de mantenimiento

            // Proteina de mantenimiento
            double protMant;
            if (pesoKg <= 500.0) {
                // Para animales <= 500 kg: 425 g base mas proporcion
                protMant = 425.0 * (pesoKg / 500.0);
            } else {
                // 425 g por los primeros 500 kg + 0.5 g por cada kg adicional
                protMant = 425.0 + (pesoKg - 500.0) * 0.5;
            }

            // Minerales por litros producidos
            double rCa = litros * 3.2;
            double rP  = litros * 1.7;
            double rMg = litros * 0.6;
            double rNa = litros * 0.6;

            // ------- Tipo de leche y requerimientos por litro ---------------
            double[] tipReqs = tipoLeche(grasaPct);
            double mjPorLitro   = tipReqs[0];
            double protPorLitro = tipReqs[1];
            String nomTipo      = nombreTipoLeche(grasaPct);

            // ------- PASO 4: Analisis de la racion -----------------------------------
            double enlRacion  = 0.0;
            double protRacion = 0.0;
            int nIng = 0;

            for (int i = 0; i < 3; i++) {
                String tKg = txtKgIng[i].getText().trim().replace(",",".");
                String tPc = txtPcPctIng[i].getText().trim().replace(",",".");
                String tMj = txtMjIng[i].getText().trim().replace(",",".");
                if (tKg.isEmpty() || tPc.isEmpty() || tMj.isEmpty()) continue;
                double kg  = Double.parseDouble(tKg);
                double pc  = Double.parseDouble(tPc);  // en %
                double mj  = Double.parseDouble(tMj);
                if (kg <= 0) continue;
                enlRacion  += kg * mj;
                protRacion += kg * pcPctAGramos(pc);
                nIng++;
            }

            if (nIng == 0) {
                JOptionPane.showMessageDialog(null,
                    "Ingrese al menos un ingrediente en la racion (Kg, % Proteina y MJ ENL/Kg).");
                return 0;
            }

            // -------  Evaluacion - disponible para producir -------------------
            double enlDisp  = enlRacion  - enlMant;
            double protDisp = protRacion - protMant;

            // Litros que se pueden producir
            double litrosConEnl  = enlDisp  > 0 ? enlDisp  / mjPorLitro   : 0.0;
            double litrosConProt = protDisp > 0 ? protDisp / protPorLitro  : 0.0;

            // -------  Ajuste F1/F2 --------------------------------------------
            double f1    = 0.0;
            double kgAjuste = 0.0;
            String mensajeAjuste = "No se requiere ajuste.";
            double litrosFinalEnl  = litrosConEnl;
            double litrosFinalProt = litrosConProt;
            boolean ajusteRealizado = false;

            String tAjPc = txtAjustePcPct.getText().trim().replace(",",".");
            String tAjMj = txtAjusteMj.getText().trim().replace(",",".");

            if (!tAjPc.isEmpty() && !tAjMj.isEmpty() && litrosConEnl > 0 && litrosConProt > 0) {
                double ajPc = Double.parseDouble(tAjPc);
                double ajMj = Double.parseDouble(tAjMj);

                // F1 = diferencia de litros (proteina - energia)
                f1 = litrosConProt - litrosConEnl;

                if (Math.abs(f1) > 0.01) {
                    // Litros que produce 1 Kg del alimento de ajuste
                    double litrosPor1KgEnl  = ajMj  / mjPorLitro;
                    double litrosPor1KgProt = pcPctAGramos(ajPc) / protPorLitro;
                    double f2 = litrosPor1KgEnl - litrosPor1KgProt;

                    if (Math.abs(f2) > 0.001) {
                        kgAjuste = Math.abs(f1) / Math.abs(f2);

                        // Nuevo aporte de energia y proteina con el ajuste
                        double enlAjuste  = kgAjuste * ajMj;
                        double protAjuste = kgAjuste * pcPctAGramos(ajPc);

                        double enlTotal2  = enlRacion  + enlAjuste;
                        double protTotal2 = protRacion + protAjuste;
                        double enlDisp2   = enlTotal2  - enlMant;
                        double protDisp2  = protTotal2 - protMant;

                        litrosFinalEnl  = enlDisp2  > 0 ? enlDisp2  / mjPorLitro   : 0;
                        litrosFinalProt = protDisp2 > 0 ? protDisp2 / protPorLitro  : 0;

                        mensajeAjuste = String.format(
                            "F1=%.2f  F2=%.2f  -> Agregar %.4f Kg del alimento de ajuste",
                            f1, f2, kgAjuste);
                        ajusteRealizado = true;
                    }
                } else {
                    mensajeAjuste = "Los litros con energia y proteina ya estan equilibrados.";
                }
            }

            // -------  Actualizar etiquetas en pantalla -----------------------
            lblResMantenimiento.setText(String.format(
                "Mant.: ENL=%.2f MJ  Prot=%.0f g  Minerales: Ca=%.1f P=%.1f Mg=%.1f Na=%.1f g",
                enlMant, protMant, rCa, rP, rMg, rNa));
            lblResRacion.setText(String.format(
                "Racion: ENL=%.2f MJ  Prot=%.0f g  |  Disponible: ENL=%.2f MJ  Prot=%.0f g",
                enlRacion, protRacion, enlDisp, protDisp));
            lblResLitros.setText(String.format(
                "Litros: con energia=%.2f  con proteina=%.2f  |  Final (tras ajuste): ENL=%.2f  Prot=%.2f",
                litrosConEnl, litrosConProt, litrosFinalEnl, litrosFinalProt));
            lblResAjuste.setText(mensajeAjuste);

            // ------- Mapa de resultados para la JTable ----------------------
            ultimosResultados.put("Peso Vivo",                    String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("Peso Metabolico (PV^0.75)",    String.format("%.4f", pm));
            ultimosResultados.put("Litros de leche/dia",          String.format("%.1f litros", litros));
            ultimosResultados.put("% Grasa leche / Tipo",         String.format("%.1f%% / %s", grasaPct, nomTipo));
            ultimosResultados.put("MJ ENL requeridos/litro",      String.format("%.2f MJ", mjPorLitro));
            ultimosResultados.put("Proteina requerida/litro",     String.format("%.0f g", protPorLitro));
            ultimosResultados.put("REQUERIMIENTOS DE MANTENIMIENTO", "");
            ultimosResultados.put("Energia mantenimiento",        String.format("%.4f MJ ENL", enlMant));
            ultimosResultados.put("Proteina mantenimiento",       String.format("%.2f g", protMant));
            ultimosResultados.put("Calcio (Ca) requerido",        String.format("%.2f g  (litros * 3.2)", rCa));
            ultimosResultados.put("Fosforo (P) requerido",        String.format("%.2f g  (litros * 1.7)", rP));
            ultimosResultados.put("Magnesio (Mg) requerido",      String.format("%.2f g  (litros * 0.6)", rMg));
            ultimosResultados.put("Sodio (Na) requerido",         String.format("%.2f g  (litros * 0.6)", rNa));
            ultimosResultados.put("ANALISIS DE LA RACION",        "");

            for (int i = 0; i < 3; i++) {
                String tKg = txtKgIng[i].getText().trim().replace(",",".");
                String tPc = txtPcPctIng[i].getText().trim().replace(",",".");
                String tMj = txtMjIng[i].getText().trim().replace(",",".");
                if (tKg.isEmpty() || tPc.isEmpty() || tMj.isEmpty()) continue;
                double kg  = Double.parseDouble(tKg);
                if (kg <= 0) continue;
                double pc  = Double.parseDouble(tPc);
                double mj  = Double.parseDouble(tMj);
                ultimosResultados.put("Ingrediente " + (i+1) + " (" + String.format("%.1f",kg) + " Kg)",
                    String.format("ENL=%.2f MJ  Prot=%.0f g  (%.1f%% PC, %.2f MJ/Kg)",
                        kg*mj, kg*pcPctAGramos(pc), pc, mj));
            }

            ultimosResultados.put("Total racion: Energia",       String.format("%.4f MJ ENL", enlRacion));
            ultimosResultados.put("Total racion: Proteina",       String.format("%.2f g", protRacion));
            ultimosResultados.put("EVALUACION DE LA RACION",      "");
            ultimosResultados.put("Energia disponible producir",  String.format("%.4f MJ ENL", enlDisp));
            ultimosResultados.put("Proteina disponible producir", String.format("%.2f g", protDisp));
            ultimosResultados.put("Litros producibles (energia)", String.format("%.4f litros", litrosConEnl));
            ultimosResultados.put("Litros producibles (proteina)",String.format("%.4f litros", litrosConProt));

            if (ajusteRealizado) {
                ultimosResultados.put("AJUSTE F1/F2",             "");
                ultimosResultados.put("F1 (diferencia litros)",   String.format("%.4f litros", f1));
                ultimosResultados.put("Kg de alimento a agregar", String.format("%.4f Kg", kgAjuste));
                ultimosResultados.put("Litros finales (energia)", String.format("%.4f litros", litrosFinalEnl));
                ultimosResultados.put("Litros finales (proteina)",String.format("%.4f litros", litrosFinalProt));
                ultimosResultados.put("Produccion estimada",      String.format("%.2f litros (por energia)", litrosFinalEnl));
            } else if (!tAjPc.isEmpty()) {
                ultimosResultados.put("AJUSTE F1/F2",             mensajeAjuste);
            }

            return enlMant;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numericos esten completos\n" +
                "y usen punto o coma como separador decimal.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, String> getResultadosParaTabla() { return ultimosResultados; }
}