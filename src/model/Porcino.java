package model;

import java.awt.*;
import javax.swing.*;

//---------------------------------------------------------INTERFAZ PARA PORCINO---------------------------------------------------------
public class Porcino extends Animal {
    private JTextField txtNumeroCerdos, txtDiasEngorde, txtCantDiaria, txtPesoIngreso, txtPesoEgreso;
    private JComboBox<String> cbUnidadCantDiaria, cbUnidadPesoIngreso, cbUnidadPesoEgreso, cbUnidadTiempo;
    private JLabel lblResultadoCA;

    private double resultadoConversionAlimenticia;

    public Porcino(double peso, String unidad) {
        super("Porcino", peso, unidad);
        // Inicialización de campos 
        txtNumeroCerdos = new JTextField();
        txtDiasEngorde = new JTextField();
        txtCantDiaria = new JTextField();
        txtPesoIngreso = new JTextField();
        txtPesoEgreso = new JTextField();

        cbUnidadTiempo = new JComboBox<>(new String[]{"Días", "Meses"});
        cbUnidadCantDiaria = new JComboBox<>(new String[]{"Kg/día", "Lb/día"});
        cbUnidadPesoIngreso = new JComboBox<>(new String[]{"Kg", "Lb"});
        cbUnidadPesoEgreso = new JComboBox<>(new String[]{"Kg", "Lb"});
    }

    @Override
    public JPanel obtenerPanelPrincipal() {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setBorder(BorderFactory.createTitledBorder("Cálculo de Conversión Alimenticia"));
        
        panelContenedor.add(crearFila("Número de cerdos:", txtNumeroCerdos, new JLabel("Animales")));
        panelContenedor.add(crearFila("Tiempo de engorde:", txtDiasEngorde, cbUnidadTiempo));
        panelContenedor.add(crearFila("Consumo diario:", txtCantDiaria, cbUnidadCantDiaria));
        panelContenedor.add(crearFila("Peso ingreso total:", txtPesoIngreso, cbUnidadPesoIngreso));
        panelContenedor.add(crearFila("Peso egreso total:", txtPesoEgreso, cbUnidadPesoEgreso));
        
        // Panel de Resultado
        JPanel panelRes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelRes.add(new JLabel("RESULTADO: "));
        lblResultadoCA = new JLabel("Pendiente...");
        lblResultadoCA.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResultadoCA.setForeground(new Color(0, 102, 204));
        panelRes.add(lblResultadoCA);
        
        panelContenedor.add(panelRes);

        return panelContenedor;
    }

    
    private JPanel crearFila(String titulo, JTextField campo, JComponent dimensional) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
 
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        p.add(new JLabel(titulo), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        campo.setPreferredSize(new Dimension(100, 25));
        p.add(campo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        dimensional.setPreferredSize(new Dimension(80, 25));
        p.add(dimensional, gbc);

        return p;
    }

    private double aKilogramos(double valor, String unidad) {
        if (unidad.startsWith("Lb")) {
            return valor / 2.20462;
        }
        return valor;
    }

    // --------------------------------------------------------CÁLCULOS--------------------------------------------------------

    @Override
    public double calcularRequerimientoEnergia() {
        try {
            //Parseo de datos ingresados
            //número de cerdos
            int nCerdos = Integer.parseInt(txtNumeroCerdos.getText().trim());
            //tiempo de engorde
            String textoTiempo = txtDiasEngorde.getText().trim().replace(",", ".");
            double tiempoIngresado = Double.parseDouble(textoTiempo);
            String unidadTiempo = (String) cbUnidadTiempo.getSelectedItem();
            double diasCalculados;

            if (unidadTiempo.equalsIgnoreCase("Meses")) {
                // Convertimos meses a días ( 30 días)
                diasCalculados = (double) (tiempoIngresado * 30);
            } else {
                diasCalculados = (double) tiempoIngresado;
            }
            // consumo diario convertido a kg
            double consumoKg = aKilogramos(Double.parseDouble(txtCantDiaria.getText().trim().replace(",", ".")), 
                                          (String) cbUnidadCantDiaria.getSelectedItem());
            // peso de ingreso y egreso convertidos a kg
            double pIngresoKg = aKilogramos(Double.parseDouble(txtPesoIngreso.getText().trim().replace(",", ".")), 
                                           (String) cbUnidadPesoIngreso.getSelectedItem());
            
            double pEgresoKg = aKilogramos(Double.parseDouble(txtPesoEgreso.getText().trim().replace(",", ".")), 
                                          (String) cbUnidadPesoEgreso.getSelectedItem());
            // Cálculo de Conversión Alimenticia
            //Tipo de variable: Double, ya que puede tener decimales y es el resultado final del cálculo.

            double alimentoTotalIndiv = diasCalculados * consumoKg;
            double gananciaIndiv = (pEgresoKg - pIngresoKg) / nCerdos;
            // Validación para evitar división por cero o resultados no válidos
            if (gananciaIndiv <= 0) {
                lblResultadoCA.setText("Error: Ganancia no válida");
                return 0;
            }
            // Cálculo final de Conversión Alimenticia
            resultadoConversionAlimenticia = alimentoTotalIndiv / gananciaIndiv;
            // label que muestra el resultado 
            lblResultadoCA.setText(String.format("%.2f kg alimento / kg peso", resultadoConversionAlimenticia));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            lblResultadoCA.setText("Datos incompletos o inválidos");
            return 0;
        }
        return resultadoConversionAlimenticia;
    }
}