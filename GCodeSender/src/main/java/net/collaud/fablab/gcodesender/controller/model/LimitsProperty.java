package net.collaud.fablab.gcodesender.controller.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.collaud.fablab.gcodesender.util.FXUtils;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Getter
@AllArgsConstructor
public class LimitsProperty {

	private final DoubleProperty xMin;
	private final DoubleProperty xMax;
	private final DoubleProperty yMin;
	private final DoubleProperty yMax;

	public LimitsProperty() {
		xMin = new SimpleDoubleProperty(Double.NaN);
		xMax = new SimpleDoubleProperty(Double.NaN);
		yMin = new SimpleDoubleProperty(Double.NaN);
		yMax = new SimpleDoubleProperty(Double.NaN);
	}

	public void setAll(Double xMin, Double xMax, Double yMin, Double yMax) {
		this.xMin.set(xMin);
		this.xMax.set(xMax);
		this.yMin.set(yMin);
		this.yMax.set(yMax);
	}

	public void setAllInFxThread(Double xMin, Double xMax, Double yMin, Double yMax) {
		FXUtils.runInFXThread(() -> setAll(xMin, xMax, yMin, yMax));
	}
	
	public void resetAllInFxThread(){
		setAllInFxThread(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
	}
	
	public void addListener(ChangeListener<Number> list){
		xMin.addListener(list);
		xMax.addListener(list);
		yMin.addListener(list);
		yMax.addListener(list);
	}
}
