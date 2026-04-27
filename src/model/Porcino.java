package model;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
public class Porcino extends Animal {

    // ─── Componentes Swing (inicializados UNA VEZ en constructor) ─
    private JTextField txtNumeroCerdos;
    private JTextField txtTiempoEngorde;
    private JTextField txtConsumoDiario;
    private JTextField txtPesoIngreso;
    private JTextField txtPesoEgreso;

    private JComboBox<String> cbUnidadTiempo;
    private JComboBox<String> cbUnidadConsumo;
    private JComboBox<String> cbUnidadIngreso;
    private JComboBox<String> cbUnidadEgreso;

    private JLabel lblResultadoCA;

    // ─── Resultado interno ────────────────────────────────────────
    private double resultadoCA = 0;

    // ─────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────
    public Porcino(double peso, String unidad) {
        super("Porcino", peso, unidad);

        // Todos los componentes se crean UNA SOLA VEZ aquí
        txtNumeroCerdos  = new JTextField(6);
        txtTiempoEngorde = new JTextField(6);
        txtConsumoDiario = new JTextField(6);
        txtPesoIngreso   = new JTextField(6);
        txtPesoEgreso    = new JTextField(6);

        cbUnidadTiempo  = new JComboBox<>(new String[]{"Días", "Meses"});
        cbUnidadConsumo = new JComboBox<>(new String[]{"Kg/día", "Lb/día"});
        cbUnidadIngreso = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbUnidadEgreso  = new JComboBox<>(new String[]{"Kg", "Lb"});
    }

    // ─────────────────────────────────────────────────────────────
    //  PANEL VISUAL
    // ─────────────────────────────────────────────────────────────
    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBorder(BorderFactory.createTitledBorder(
            "Cálculo de Conversión Alimenticia – Porcinos"));

        contenedor.add(crearFila("Número de cerdos:",      txtNumeroCerdos,  new JLabel("Animales")));
        contenedor.add(crearFila("Tiempo de engorde:",     txtTiempoEngorde, cbUnidadTiempo));
        contenedor.add(crearFila("Consumo diario/animal:", txtConsumoDiario, cbUnidadConsumo));
        contenedor.add(crearFila("Peso ingreso total:",    txtPesoIngreso,   cbUnidadIngreso));
        contenedor.add(crearFila("Peso egreso total:",     txtPesoEgreso,    cbUnidadEgreso));

        // Nota opcional
        JLabel nota = new JLabel("  * Si no existe peso de ingreso, ingresar 0.");
        nota.setFont(new Font("SansSerif", Font.ITALIC, 11));
        nota.setForeground(Color.GRAY);
        contenedor.add(nota);

        // Resultado en pantalla
        JPanel panelRes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelRes.add(new JLabel("RESULTADO  CA:"));
        lblResultadoCA = new JLabel("Pendiente...");
        lblResultadoCA.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResultadoCA.setForeground(new Color(0, 100, 200));
        panelRes.add(lblResultadoCA);
        contenedor.add(panelRes);

        return contenedor;
    }

    // ─────────────────────────────────────────────────────────────
    //  Fila de formulario reutilizable
    // ─────────────────────────────────────────────────────────────
    private JPanel crearFila(String titulo, JTextField campo, JComponent unidad) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.weightx = 0.45; p.add(new JLabel(titulo), g);
        g.gridx = 1; g.weightx = 0.35; campo.setPreferredSize(new Dimension(100, 25)); p.add(campo, g);
        g.gridx = 2; g.weightx = 0.20; unidad.setPreferredSize(new Dimension(90, 25));  p.add(unidad, g);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  CONVERSIONES INTERNAS
    // ─────────────────────────────────────────────────────────────

    /** Convierte peso a Kg según la unidad del ComboBox. */
    private double aKg(double valor, JComboBox<String> cb) {
        String u = (String) cb.getSelectedItem();
        // Cualquier unidad que empiece por "Lb" se divide entre 2.2
        return (u != null && u.startsWith("Lb")) ? valor / 2.2 : valor;
    }

    /** Convierte tiempo a Días. */
    private double aDias(double valor, JComboBox<String> cb) {
        String u = (String) cb.getSelectedItem();
        return (u != null && u.equalsIgnoreCase("Meses")) ? valor * 30 : valor;
    }

    // ─────────────────────────────────────────────────────────────
    //  CÁLCULO PRINCIPAL
    // ─────────────────────────────────────────────────────────────
    @Override
    public double calcularRequerimientoEnergia() {
        try {
            // ── Lectura y conversión de datos de entrada ──────────
            int    nCerdos   = Integer.parseInt(txtNumeroCerdos.getText().trim());
            double dias      = aDias(
                Double.parseDouble(txtTiempoEngorde.getText().trim().replace(",", ".")),
                cbUnidadTiempo);
            double consumoKg = aKg(
                Double.parseDouble(txtConsumoDiario.getText().trim().replace(",", ".")),
                cbUnidadConsumo);

            // Pesos de egreso e ingreso en Kg
            double pesoEgresoKg  = aKg(
                Double.parseDouble(txtPesoEgreso.getText().trim().replace(",", ".")),
                cbUnidadEgreso);
            double pesoIngresoKg = aKg(
                Double.parseDouble(txtPesoIngreso.getText().trim().replace(",", ".")),
                cbUnidadIngreso);

            // ── Alimento total entregado por animal ────────────────
            // días × consumo diario por animal
            double alimentoTotalIndiv = dias * consumoKg;

            // ── Ganancia de peso individual ───────────────────────
            double gananciaIndiv;
            if (pesoIngresoKg <= 0) {
                // Sin peso de ingreso → se usa solo el egreso total / N°
                gananciaIndiv = pesoEgresoKg / nCerdos;
            } else {
                // Con peso de ingreso → (egreso − ingreso) total / N°
                double gananciaTotalKg = pesoEgresoKg - pesoIngresoKg;
                gananciaIndiv = gananciaTotalKg / nCerdos;
            }

            if (gananciaIndiv <= 0) {
                lblResultadoCA.setText("Error: Ganancia ≤ 0, revise los datos.");
                return 0;
            }

            // ── Conversión Alimenticia ────────────────────────────
            resultadoCA = alimentoTotalIndiv / gananciaIndiv;
            lblResultadoCA.setText(String.format(
                "%.4f  kg alimento / kg de ganancia de peso", resultadoCA));

            return resultadoCA;

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null,
                "Error: Verifique que todos los campos numéricos estén completos\n" +
                "y usen punto o coma como separador decimal.");
            lblResultadoCA.setText("Datos incompletos");
            return 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            lblResultadoCA.setText("Error de cálculo");
            return 0;
        }
    }

    // Porcino no calcula peso metabólico
    @Override
    public double calcularPesoMetabolico() { return 0; }

    // ─────────────────────────────────────────────────────────────
    //  Resultados para tabla (Porcino muestra solo CA)
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, String> getResultadosParaTabla() {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("Conversión Alimenticia (CA)",
                 String.format("%.4f  kg alimento / kg ganancia de peso", resultadoCA));
        return mapa;
    }
}