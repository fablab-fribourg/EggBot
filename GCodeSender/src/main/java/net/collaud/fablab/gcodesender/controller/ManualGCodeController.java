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
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import net.collaud.fablab.gcodesender.util.GcodeValueParser;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ManualGCodeController implements Initializable {

	@Autowired
	private GcodeService gcodeService;

	@FXML
	private TextField textGcode;

	@FXML
	private Button buttonSend;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		buttonSend.disableProperty().bind(textGcode.textProperty().isEmpty());
	}

	@FXML
	private void send() {
		if (!textGcode.getText().isEmpty()) {
			gcodeService.sendGCodeLine(textGcode.getText());
		}
	}

	@FXML
	private void clear() {
		textGcode.setText("");
	}

}
