package model;

import java.util.Map;
import javax.swing.JPanel;


public abstract class Animal {

    // ─── Atributos comunes a todas las especies ───────────────────
    protected String especie;
    protected double peso;
    protected String unidadPeso;

    /**
     * Constructor base.
     * @param especie     Nombre de la especie (ej. "Equino")
     * @param peso        Peso del animal en la unidad indicada
     * @param unidadPeso  "Kg" o "Lb"
     */
    public Animal(String especie, double peso, String unidadPeso) {
        this.especie    = especie;
        this.peso       = peso;
        this.unidadPeso = unidadPeso;
    }

    // ─────────────────────────────────────────────────────────────
    //  Utilidad: conversión de peso a Kg desde la unidad almacenada
    // ─────────────────────────────────────────────────────────────
    /**
     * Devuelve el peso en Kg, convirtiendo desde Lb si corresponde.
     * 1 kg = 2.2 lb  →  lb / 2.2 = kg
     */
    public double getPesoEnKg() {
        if (unidadPeso != null && unidadPeso.equalsIgnoreCase("lb")) {
            return peso / 2.2;
        }
        return peso;
    }

    // ─────────────────────────────────────────────────────────────
    //  Métodos abstractos que cada subclase DEBE implementar
    // ─────────────────────────────────────────────────────────────

    /** Devuelve el panel principal de captura de datos de la especie. */
    public abstract JPanel obtenerPanelPrincipal();

    /**
     * Ejecuta el cálculo principal de la especie y retorna un valor
     * representativo (ED total en Mcal para Equino, CA para Porcino, etc.).
     * Retorna 0 si hay error en los datos.
     */
    public abstract double calcularRequerimientoEnergia();

    /**
     * Calcula el peso metabólico (Wkg^0.75).
     * Aplica a Equino y Canino. Las demás especies devuelven 0.
     */
    public abstract double calcularPesoMetabolico();

    /**
     * Devuelve un mapa ordenado (LinkedHashMap) de resultados listo para
     * poblar la JTable de la pestaña "Cálculo de Ración".
     *   Clave   → nombre del nutriente / verificación (columna "Concepto")
     *   Valor   → resultado formateado como String (columna "Resultado")
     */
    public abstract Map<String, String> getResultadosParaTabla();
}