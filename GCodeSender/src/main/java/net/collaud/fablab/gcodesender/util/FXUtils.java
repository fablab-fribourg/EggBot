package net.collaud.fablab.gcodesender.util;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Gaetan Collaud
 */
@UtilityClass
public class FXUtils {
	
	/**
	 * Set the property value inside the java FX UI thread
	 * @param <T>
	 * @param property
	 * @param value
	 */
	public <T> void setInFXThread(Property<T> property, T value) {
		Platform.runLater(() -> {
			property.setValue(value);
		});
	}
	
	public void runInFXThread(Runnable runable){
		Platform.runLater(runable);
	}
	
//	public void setInFXThread(DoubleProperty property, double value){
//		Platform.runLater(() -> {
//			property.set(value);
//		});
//	}
}
