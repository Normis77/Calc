package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Clase encargada de generar la ventana principal del programa.
 * Organiza la navegación por medio de pestañas.
 */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        // Configuración básica de la ventana
        setTitle("Sistema de Formulación Nutricional Veterinaria");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Creación del panel de pestañas
        JTabbedPane pestañas = new JTabbedPane();

        // Inicialización de secciones
        pestañas.addTab("Base de Datos Ingredientes", crearPanelIngredientes());
        pestañas.addTab("Definición de Requerimientos", crearPanelRequerimientos());
        pestañas.addTab("Cálculo de Ración", crearPanelCalculo());

        add(pestañas);
    }


    private JPanel crearPanelRequerimientos() {
        JPanel panel = new JPanel(new GridLayout(10, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Especie:"));
        String[] especies = {"Equinos", "Bovinos", "Caninos"};
        panel.add(new JComboBox<>(especies));

        panel.add(new JLabel("Peso:"));
        panel.add(new JTextField());

        panel.add(new JLabel("Unidad:"));
        panel.add(new JComboBox<>(new String[]{"kg", "lb"}));

        return panel;
    }

    private JPanel crearPanelCalculo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Área de resultados y reportes", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel crearPanelIngredientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {
            "Ingrediente", 
            "MS (%)", 
            "Prot. Cruda (%)", 
            "Energía (Mcal)", 
            "Fibra (%)", 
            "Calcio (%)", 
            "Fósforo (%)"
        };

        // Modelo de la tabla 
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        JTable tablaIngredientes = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);

        // Panel de botones para interactuar con la tabla
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar Fila");
        JButton btnEliminar = new JButton("Eliminar Fila");

        btnAgregar.addActionListener(e -> {
            modeloTabla.addRow(new Object[]{"Nuevo...", 0, 0, 0, 0, 0, 0});
        });

        btnEliminar.addActionListener(e -> {
            int filaSeleccionada = tablaIngredientes.getSelectedRow();
            if (filaSeleccionada != -1) {
                modeloTabla.removeRow(filaSeleccionada);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.");
            }
        });

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);

        // Organización del panel
        panel.add(new JLabel("Composición Nutricional de Ingredientes", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

}