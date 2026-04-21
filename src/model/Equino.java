package model;

import java.awt.*;
import javax.swing.*;

public class Equino extends Animal {

    public Equino(double peso, String unidad) {
        super("Equino", peso, unidad);
    }

	@Override
	public double calcularRequerimientoEnergia() {
		return 0.0;
	}

	@Override
	public JPanel obtenerPanelPrincipal() {
		return new JPanel(new BorderLayout());
	}
}