package model;

import javax.swing.JPanel;

/*
Clase abstracta que define la estructura base de un animal.
Permite manejar diferentes especies mediante herencia.
 */
public abstract class Animal {
    protected String especie;
    protected double peso;
    protected String unidadPeso;

    public Animal(String especie, double peso, String unidadPeso) {
        this.especie = especie;
        this.peso = peso;
        this.unidadPeso = unidadPeso;
    }

    // Método para convertir el peso a kg si es necesario
    public double getPesoEnKg() {
        if (unidadPeso != null && unidadPeso.equalsIgnoreCase("lb")) {
            return peso / 2.20462;
        }
        return peso;
    }

    // Método abstracto que cada especie implementará según sus fórmulas
    public abstract JPanel obtenerPanelPrincipal();
    public abstract double calcularRequerimientoEnergia();
    public abstract double calcularPesoMetabolico();
}