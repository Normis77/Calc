package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Equino extends Animal {

    private JTextField txtPesoVivo;
    private JTextField txtHenoPct;
    private JTextField txtAvenaPct;
    private JTextField txtMaizPct;
    private JTextField txtNaFuerte;   
    private JComboBox<String> cbUnidadPeso;
    private JComboBox<String> cbTipoTrabajo;

    // ─── BASE DE DATOS NUTRICIONAL ───
    // Columnas: { Grasa(g), ED(Mcal), Prot(g), Ca(g), P(g), Mg(g), Na(g), Cl(g), K(g) }
    private static final double[] DB_HENO  = {2.70, 2.10, 8.4, 7.20, 0.22, 0.17, 0.04, 0.19, 0.20};
    private static final double[] DB_AVENA = {4.70, 2.77, 8.6, 1.10, 0.31, 0.13, 0.05, 0.04, 0.30};
    private static final double[] DB_MAIZ  = {4.11, 3.27, 6.7, 0.40, 0.29, 0.09, 0.03, 0.06, 0.30};

    private static final int I_GRASA = 0, I_ED = 1, I_PROT = 2,
                             I_CA = 3, I_P  = 4, I_MG  = 5,
                             I_NA = 6, I_CL = 7, I_K   = 8;

    // ─── Mapa de resultados─────────────
    private Map<String, String> ultimosResultados = new LinkedHashMap<>();

    // ─────────────────────────────────────────────────────────────
    //  CONSTRUCTOR – Componentes
    // ─────────────────────────────────────────────────────────────
    public Equino(double peso, String unidad) {
        super("Equino", peso, unidad);

        // Campos de texto
        txtPesoVivo = new JTextField(8);
        txtHenoPct  = new JTextField("70", 5);
        txtAvenaPct = new JTextField("15", 5);
        txtMaizPct  = new JTextField("15", 5);
        txtNaFuerte = new JTextField(6);

        // Combos
        cbUnidadPeso  = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbTipoTrabajo = new JComboBox<>(new String[]{
            "Mantenimiento", "Trabajo moderado", "Trabajo medio",
            "Trabajo fuerte", "Trabajo muy fuerte"
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  PANEL VISUAL DE ENTRADA DE DATOS
    // ─────────────────────────────────────────────────────────────
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel(new BorderLayout(8, 8));
        contenedor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // ── Tabla de Base de Datos Nutricional ────────────────────
        String[] colDB = {"Alimento","Grasa(g)","ED(Mcal)","Prot(g)",
                          "Ca(g)","P(g)","Mg(g)","Na(g)","Cl(g)","K(g)"};
        Object[][] dataDB = {
            {"Heno",  2.70, 2.10, 8.4, 7.20, 0.22, 0.17, 0.04, 0.19, 0.20},
            {"Avena", 4.70, 2.77, 8.6, 1.10, 0.31, 0.13, 0.05, 0.04, 0.30},
            {"Maíz",  4.11, 3.27, 6.7, 0.40, 0.29, 0.09, 0.03, 0.06, 0.30}
        };
        DefaultTableModel modeloDB = new DefaultTableModel(dataDB, colDB);
        JTable tablaDB = new JTable(modeloDB);
        tablaDB.setEnabled(false);   // Solo lectura
        tablaDB.setRowHeight(22);
        tablaDB.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollDB = new JScrollPane(tablaDB);
        scrollDB.setPreferredSize(new Dimension(860, 88));
        scrollDB.setBorder(BorderFactory.createTitledBorder(
            "Base de Datos de Referencia – Aportes por Kg de Alimento"));

        // ── Panel de entradas del caballo ────────────────────────
        JPanel panelEntradas = new JPanel();
        panelEntradas.setLayout(new BoxLayout(panelEntradas, BoxLayout.Y_AXIS));
        panelEntradas.setBorder(BorderFactory.createTitledBorder("Datos del Caballo"));

        // Peso vivo + unidad
        panelEntradas.add(crearFila("Peso vivo:", txtPesoVivo, cbUnidadPeso));

        //  Tipo de actividad
        panelEntradas.add(crearFila("Tipo de actividad:", new JLabel("Nivel:"), cbTipoTrabajo));

        // Sub-panel de distribución de la ración
        JPanel panelRacion = new JPanel(new GridLayout(4, 3, 5, 3));
        panelRacion.setBorder(BorderFactory.createTitledBorder(
            "Distribución de la Ración (%)  – debe sumar 100%"));
        panelRacion.add(new JLabel("% Heno:"));
        panelRacion.add(txtHenoPct);
        panelRacion.add(new JLabel("(forraje principal)"));
        panelRacion.add(new JLabel("% Avena:"));
        panelRacion.add(txtAvenaPct);
        panelRacion.add(new JLabel("(concentrado)"));
        panelRacion.add(new JLabel("% Maíz:"));
        panelRacion.add(txtMaizPct);
        panelRacion.add(new JLabel("(concentrado)"));
        panelRacion.add(new JLabel("Na manual (solo Trabajo fuerte):"));
        panelRacion.add(txtNaFuerte);
        panelRacion.add(new JLabel("g/día  (opcional)"));
        panelEntradas.add(panelRacion);

        // Nota informativa
        JLabel nota = new JLabel("  * En Trabajo fuerte/muy fuerte el Na puede ser hasta 25 g/100 kg; déjelo vacío para usar ese máximo.");
        nota.setFont(new Font("SansSerif", Font.ITALIC, 11));
        nota.setForeground(Color.GRAY);
        panelEntradas.add(nota);

        contenedor.add(scrollDB,      BorderLayout.NORTH);
        contenedor.add(panelEntradas, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearFila(String titulo, JComponent campo, JComponent unidad) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 5, 3, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.weightx = 0.35; p.add(new JLabel(titulo), g);
        g.gridx = 1; g.weightx = 0.40; p.add(campo, g);
        g.gridx = 2; g.weightx = 0.25; p.add(unidad, g);
        return p;
    }

    //-----------------------------------------------------CÁLCULOS------------------------------------------------
    // ─────────────────────────────────────────────────────────────
    //  Conversión de peso a Kg según combo seleccionado
    // ─────────────────────────────────────────────────────────────
    private double aKg(double valor) {
        return "Lb".equals(cbUnidadPeso.getSelectedItem()) ? valor / 2.2 : valor;
    }

    // ─────────────────────────────────────────────────────────────
    //  Peso metabólico: PesoVivo^0.75
    // ─────────────────────────────────────────────────────────────
    @Override
    public double calcularPesoMetabolico() {
        try {
            //LECTURA DE DATO
            double kg = aKg(Double.parseDouble(txtPesoVivo.getText().trim().replace(",", ".")));
            // CÁLCULO
            return Math.pow(kg, 0.75);
        } catch (Exception e) {
            return 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CÁLCULO PRINCIPAL – retorna ED total en Mcal
    // ─────────────────────────────────────────────────────────────
    @Override
    public double calcularRequerimientoEnergia() {
        ultimosResultados.clear();
        try {
            // ── PASO 1: Datos base ────────────────────────────────
            double pesoKg = aKg(Double.parseDouble(
                    txtPesoVivo.getText().trim().replace(",", ".")));
            if (pesoKg <= 0) {
                JOptionPane.showMessageDialog(null, "El peso debe ser mayor a 0.");
                return 0;
            }
            // Peso metabólico (Wkg)
            double wkg    = Math.pow(pesoKg, 0.75);
            String trabajo = (String) cbTipoTrabajo.getSelectedItem();

            // ── Energía de Mantenimiento ─────────────────
            double edMantMJ   = wkg * 0.6;     // en MegaJoules
            double edMantMcal = wkg * 0.155;   // en Megacalorías

            // ── Factor de ejercicio y ED Total ────────────
            double factor;
            switch (trabajo) {
                case "Trabajo moderado":   factor = 1.2; break;
                case "Trabajo medio":      factor = 1.4; break;
                case "Trabajo fuerte":     factor = 1.6; break;
                case "Trabajo muy fuerte": factor = 1.9; break;
                default:                   factor = 1.0; break; // Mantenimiento
            }
            // ED total = ED mantenimiento × factor de actividad
            double edTotalMcal = edMantMcal * factor;
            // Convertir Mcal → MJ  (1 Mcal = 4.19 MJ)
            double edTotalMJ   = edTotalMcal * 4.19;

            // ── Proteína (CHON) ───────────────────────────
            // Fórmula: ED total (Mcal) × 19.4 = gramos CHON/día
            double chonReqG = edTotalMcal * 19.4;

            // ── Minerales por regla de 3 (base 100 kg PV) ─
            // f100 = factor de escala respecto a 100 kg
            double f100 = pesoKg / 100.0;
            double rCa, rP, rMg, rNa, rK, rCl;

            switch (trabajo) {
                case "Trabajo moderado":
                    rCa = 5.2 * f100; rP  = 3.2 * f100; rMg = 2.2 * f100;
                    rNa = 3.9 * f100; rK  = 5.9 * f100; rCl = 5.1 * f100;
                    break;
                case "Trabajo medio":
                    rCa = 5.7 * f100; rP  = 3.8 * f100; rMg = 2.7 * f100;
                    rNa = 10.0* f100; rK  = 8.8 * f100; rCl = 14.0* f100;
                    break;
                case "Trabajo fuerte":
                case "Trabajo muy fuerte":
                    rCa = 6.7 * f100; rP  = 4.9 * f100; rMg = 3.7 * f100;
                    // Na: el usuario puede ingresar valor manual; si no, usar 25 g/100 kg
                    String naText = txtNaFuerte.getText().trim().replace(",", ".");
                    rNa = naText.isEmpty() ? 25.0 * f100
                                           : Double.parseDouble(naText);
                    rK  = 15.0 * f100; rCl = 35.0 * f100;
                    break;
                default: // Mantenimiento
                    rCa = 5.0 * f100; rP  = 3.0 * f100; rMg = 2.0 * f100;
                    rNa = 2.0 * f100; rK  = 5.0 * f100; rCl = 2.0 * f100;
                    break;
            }

            // ──  Ración – porcentajes de cada ingrediente ──
            double hPct = parsePct(txtHenoPct)  / 100.0;
            double aPct = parsePct(txtAvenaPct) / 100.0;
            double mPct = parsePct(txtMaizPct)  / 100.0;

            // Validar que los porcentajes sumen 100 %
            double sumPct = hPct + aPct + mPct;
            if (Math.abs(sumPct - 1.0) > 0.01) {
                JOptionPane.showMessageDialog(null,
                    "Los porcentajes de la ración deben sumar 100% (actual: "
                    + String.format("%.1f", sumPct * 100) + "%)");
                return 0;
            }

            // ED que debe aportar cada ingrediente según su porcentaje:
            double edHeno  = edTotalMcal * hPct;
            double edAvena = edTotalMcal * aPct;
            double edMaiz  = edTotalMcal * mPct;

            // Kg necesarios de cada ingrediente:
            //   Si 1 Kg de heno aporta DB_HENO[I_ED] Mcal → necesito edHeno/DB_HENO[I_ED] Kg
            double kgHeno  = edHeno  / DB_HENO [I_ED];
            double kgAvena = edAvena / DB_AVENA[I_ED];
            double kgMaiz  = edMaiz  / DB_MAIZ [I_ED];
            double kgTotal = kgHeno + kgAvena + kgMaiz;

            // ── Aporte nutricional de la ración propuesta ─
            // Proteína: regla de 3 con la base de datos (g/kg)
            double aporteProtH = kgHeno  * DB_HENO [I_PROT];
            double aporteProtA = kgAvena * DB_AVENA[I_PROT];
            double aporteProtM = kgMaiz  * DB_MAIZ [I_PROT];
            double aporteProt  = aporteProtH + aporteProtA + aporteProtM;

            // Minerales: misma lógica para cada mineral
            double aCa = kg(kgHeno,kgAvena,kgMaiz, I_CA);
            double aP  = kg(kgHeno,kgAvena,kgMaiz, I_P);
            double aMg = kg(kgHeno,kgAvena,kgMaiz, I_MG);
            double aNa = kg(kgHeno,kgAvena,kgMaiz, I_NA);
            double aCl = kg(kgHeno,kgAvena,kgMaiz, I_CL);
            double aK  = kg(kgHeno,kgAvena,kgMaiz, I_K);

            // ── Verificación de reglas críticas ───────────
            // Regla 1: mínimo 0.5 kg de heno por cada 100 kg PV
            double minHeno  = 0.5  * f100;
            // Regla 2: máximo 0.4 kg de granos (avena+maíz) por cada 100 kg PV
            double maxConc  = 0.4  * f100;
            // Regla 3: consumo máximo = 2% del peso vivo (Materia Seca)
            double msLimite = pesoKg * 0.02;
            double concTotal = kgAvena + kgMaiz;

            String vHeno = kgHeno   >= minHeno  ? "ADECUADA " : "NO ADECUADA ";
            String vConc = concTotal <= maxConc  ? "ADECUADA " : "NO ADECUADA ";
            String vMS   = kgTotal  <= msLimite  ? "ADECUADA " : "NO ADECUADA ";

            // ── Construir mapa de resultados para la JTable ─
            ultimosResultados.put("Peso Vivo",                    String.format("%.2f Kg", pesoKg));
            ultimosResultados.put("Peso Metabólico (Wkg^0.75)",   String.format("%.4f Wkg", wkg));
            ultimosResultados.put("ENERGÍA",                    "────────────────────");
            ultimosResultados.put("ED Mantenimiento",             String.format("%.4f Mcal  /  %.4f MJ", edMantMcal, edMantMJ));
            ultimosResultados.put("Factor actividad (" + trabajo + ")", "× " + factor);
            ultimosResultados.put("ED Total",                     String.format("%.4f Mcal  /  %.4f MJ", edTotalMcal, edTotalMJ));
            ultimosResultados.put("PROTEÍNA",                   "────────────────────");
            ultimosResultados.put("Req. CHON (g/día)",            String.format("%.2f g", chonReqG));
            ultimosResultados.put("Aporte CHON de la ración",     String.format("%.2f g", aporteProt));
            ultimosResultados.put("Diferencia CHON",              String.format("%.2f g  %s", aporteProt - chonReqG,
                                                                    (aporteProt >= chonReqG) ? "✔" : "✘"));
            ultimosResultados.put("MINERALES – Requerido / Aportado", "────────────────────");
            ultimosResultados.put("Calcio (Ca)  g/día",           String.format("%.2f / %.2f  %s", rCa, aCa, check(aCa, rCa)));
            ultimosResultados.put("Fósforo (P)  g/día",           String.format("%.2f / %.2f  %s", rP,  aP,  check(aP,  rP)));
            ultimosResultados.put("Magnesio (Mg) g/día",          String.format("%.2f / %.2f  %s", rMg, aMg, check(aMg, rMg)));
            ultimosResultados.put("Sodio (Na)   g/día",           String.format("%.2f / %.2f  %s", rNa, aNa, check(aNa, rNa)));
            ultimosResultados.put("Cloro (Cl)   g/día",           String.format("%.2f / %.2f  %s", rCl, aCl, check(aCl, rCl)));
            ultimosResultados.put("Potasio (K)  g/día",           String.format("%.2f / %.2f  %s", rK,  aK,  check(aK,  rK)));
            ultimosResultados.put("RACIÓN PROPUESTA",           "────────────────────");
            ultimosResultados.put("Heno  (" + pct(hPct) + ")",   String.format("%.4f Kg", kgHeno));
            ultimosResultados.put("Avena (" + pct(aPct) + ")",   String.format("%.4f Kg", kgAvena));
            ultimosResultados.put("Maíz  (" + pct(mPct) + ")",   String.format("%.4f Kg", kgMaiz));
            ultimosResultados.put("Total alimento",               String.format("%.4f Kg", kgTotal));
            ultimosResultados.put("VERIFICACIÓN DE LA RACIÓN",  "────────────────────");
            ultimosResultados.put("Mín. Heno  (≥ " + String.format("%.2f", minHeno)  + " Kg)", vHeno);
            ultimosResultados.put("Máx. Conc. (≤ " + String.format("%.2f", maxConc)  + " Kg)", vConc);
            ultimosResultados.put("Límite MS   (≤ " + String.format("%.2f", msLimite) + " Kg)", vMS);

            return edTotalMcal;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numéricos estén completos\n" +
                "y usen punto o coma como separador decimal.");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + e.getMessage());
            return 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers internos
    // ─────────────────────────────────────────────────────────────

    /** Suma el aporte de un mineral para heno+avena+maíz dado su índice. */
    private double kg(double kH, double kA, double kM, int idx) {
        return kH * DB_HENO[idx] + kA * DB_AVENA[idx] + kM * DB_MAIZ[idx];
    }

    /** Parsea el texto de un JTextField como porcentaje. */
    private double parsePct(JTextField tf) {
        return Double.parseDouble(tf.getText().trim().replace(",", "."));
    }

    /** Formatea un factor como porcentaje legible. */
    private String pct(double f) {
        return String.format("%.0f%%", f * 100);
    }

    /** Devuelve ✔ si el aporte cubre el requerimiento, ✘ si no. */
    private String check(double aporte, double req) {
        return aporte >= req ? "✔" : "✘";
    }

    // ─────────────────────────────────────────────────────────────
    //  Devuelve el mapa de resultados 
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, String> getResultadosParaTabla() {
        return ultimosResultados;
    }
}