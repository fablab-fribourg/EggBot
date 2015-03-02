package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ControlController implements Initializable {
	
	@Autowired
	private GcodeService GcodeService;
	
	@FXML
	private LinearControlController servoController;
	
	@FXML
	private LinearControlController xController;
	
	@FXML
	private LinearControlController yController;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		servoController.init("Servo", 0, 90);
		xController.init("X", -30, 30);
		yController.init("Y", -30, 30);
	}	
	
}
