package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;

public class VentanaPrincipal extends JFrame {
    private JTabbedPane pestañas;
    private JPanel panelParticularidadesContenedor;
    private JComboBox<String> comboEspecies;
    private Animal animalActual;
    private DefaultTableModel modeloAnalisis; 

    public VentanaPrincipal() {
        setTitle("Sistema de Formulación Nutricional Veterinaria");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pestañas = new JTabbedPane();

        
        pestañas.addTab("Definición de Requerimientos", crearPanelRequerimientos());
        

        add(pestañas);
    }

    private JPanel crearPanelRequerimientos() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        
        // Formulario superior reducido (solo especie)
        JPanel formularioEspecie = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formularioEspecie.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        formularioEspecie.add(new JLabel("Seleccione Especie:"));
        
        String[] especies = {"Seleccione...", "Equino", "Porcino"};
        comboEspecies = new JComboBox<>(especies);
        formularioEspecie.add(comboEspecies);

        panelParticularidadesContenedor = new JPanel(new BorderLayout());
        panelParticularidadesContenedor.setBorder(BorderFactory.createTitledBorder("Datos Específicos"));

        comboEspecies.addActionListener(e -> actualizarParticularidades());

        // Botón inferior
        JButton btnCalcular = new JButton("Realizar Cálculo");
        btnCalcular.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCalcular.addActionListener(e -> {
            if (animalActual != null) {
                double resultado = animalActual.calcularRequerimientoEnergia();

                if (resultado > 0) {
                    actualizarTablaResultados(resultado);

                    if (!(animalActual instanceof Porcino)) {
                        pestañas.setSelectedIndex(2); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Cálculo finalizado exitosamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error en los datos ingresados.");
                }
                }
          });

        principal.add(formularioEspecie, BorderLayout.NORTH);
        principal.add(panelParticularidadesContenedor, BorderLayout.CENTER); 
        principal.add(btnCalcular, BorderLayout.SOUTH);
        
        return principal;
    }

    private void actualizarParticularidades() {
        panelParticularidadesContenedor.removeAll();
        String seleccion = (String) comboEspecies.getSelectedItem();

        if (seleccion.equals("Equino")) {
            // animalActual = new Equino(0, "Kg", "Adulto"); 
            // panelParticularidadesContenedor.add(animalActual.obtenerPanelPrincipal());
        } else if (seleccion.equals("Porcino")) {
            animalActual = new Porcino(0, "Kg"); 
            panelParticularidadesContenedor.add(animalActual.obtenerPanelPrincipal(), BorderLayout.NORTH);
        }

        panelParticularidadesContenedor.revalidate();
        panelParticularidadesContenedor.repaint();
    }

    private void actualizarTablaResultados(double valorCalculado) {
        if (modeloAnalisis != null) {
            modeloAnalisis.setRowCount(0); 
            String resultadoFormateado = String.format("%.2f", valorCalculado);
            modeloAnalisis.addRow(new Object[]{
                "Conversión Alimenticia (CA)", "Eficiencia", resultadoFormateado, "Relación", "Calculado"
            });
        }
    }

    private JPanel crearPanelCalculo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnas = {"Nutriente", "Requerimiento", "Aporte Mezcla", "Diferencia", "% Cumplimiento"};
        modeloAnalisis = new DefaultTableModel(columnas, 0);
        JTable tablaAnalisis = new JTable(modeloAnalisis);
        
        panel.add(new JScrollPane(tablaAnalisis), BorderLayout.CENTER);
        return panel;
    }   
    
    private JPanel crearPanelIngredientes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Panel de Ingredientes"));
        return panel; 

    }
}