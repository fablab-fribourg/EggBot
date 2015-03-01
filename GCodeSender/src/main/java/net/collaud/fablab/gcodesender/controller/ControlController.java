package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FXML Controller class
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
public class ControlController implements Initializable {
	
	@Autowired
	private GcodeService GcodeService;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}	
	
}
