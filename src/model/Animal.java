package model;

/*
Clase abstracta que define la estructura base de un animal.
Permite manejar diferentes especies mediante herencia.
 */
public abstract class Animal {
    protected String especie;
    protected double peso;
    protected String unidadPeso; // "kg" o "lb"
    protected String etapaProductiva;

    public Animal(String especie, double peso, String unidadPeso, String etapaProductiva) {
        this.especie = especie;
        this.peso = peso;
        this.unidadPeso = unidadPeso;
        this.etapaProductiva = etapaProductiva;
    }

    // Método para normalizar el peso a kg para los cálculos internos
    public double getPesoEnKg() {
        if (unidadPeso.equalsIgnoreCase("lb")) {
            return peso * 0.453592;
        }
        return peso;
    }

    // Método abstracto que cada especie implementará según sus fórmulas
    public abstract double calcularRequerimientoEnergia();
}