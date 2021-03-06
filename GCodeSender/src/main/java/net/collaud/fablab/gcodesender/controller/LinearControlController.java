package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.controller.custom.CustomField;
import net.collaud.fablab.gcodesender.util.GcodeValueParser;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author Gaetan Collaud
 */
@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class LinearControlController implements Initializable {

	@FXML
	private Label labelTitle;

	@FXML
	private Label labelCurrent;

	@FXML
	private Slider slider;

	@FXML
	private Button buttonSetMin;

	@FXML
	private Button buttonSetMax;

	@FXML
	private TextField textMin;

	@FXML
	private TextField textMax;

	@Getter
	private final DoubleProperty min = new SimpleDoubleProperty();

	@Getter
	private final DoubleProperty max = new SimpleDoubleProperty();
	
	private double sliderMinDefault;
	private double sliderMaxDefault;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	public void init(String title, double sliderMin, double sliderMax, double sliderMinDefault, double sliderMaxDefault) {
		this.sliderMinDefault = sliderMinDefault;
		this.sliderMaxDefault = sliderMaxDefault;
		
		min.set(sliderMin);
		max.set(sliderMax);

		labelTitle.setText(title);

		slider.minProperty().bind(min);
		slider.maxProperty().bind(max);

		if (slider.getValue() < sliderMin) {
			slider.setValue(sliderMin);
		}

		CustomField.numberField(min, textMin);
		CustomField.numberField(max, textMax);
	}

	@FXML
	private void setCurrentToMin() {
		min.set(slider.getValue());
	}

	@FXML
	private void setCurrentToMax() {
		max.set(slider.getValue());
	}

	@FXML
	private void resetMin() {
		min.set(sliderMinDefault);
	}

	@FXML
	private void resetMax() {
		max.set(sliderMaxDefault);
	}

	public void setValue(double v) {
		labelCurrent.setText(GcodeValueParser.format(v));
//		if(v<min.get()){
//			v = min.get();
//		}
//		if(v>max.get()){
//			v = max.get();
//		}
//		slider.setValue(v);
	}

	public void addListener(ChangeListener<Number> list) {
		slider.valueProperty().addListener(list);
	}

}
