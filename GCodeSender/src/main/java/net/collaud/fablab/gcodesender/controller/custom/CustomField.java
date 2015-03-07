package net.collaud.fablab.gcodesender.controller.custom;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
public class CustomField {

	public static void numberField(DoubleProperty property, TextField field) {
		field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			if (!newValue.equals("-") && !newValue.equals(".")) {
				try {
					final double value = Double.parseDouble(newValue);
					if (property.get() != value) {
						property.set(value);
					}
				} catch (NumberFormatException ex) {
					field.setText(oldValue);
				}
			}
		});
		property.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			field.setText(newValue.toString());
		});
		field.setText(String.valueOf(property.get()));
	}
}
