package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import net.collaud.fablab.gcodesender.serial.SerialPort;
import net.collaud.fablab.gcodesender.serial.SerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MainController implements Initializable {
	
	@Autowired
	private SerialService serialService;
    
    @FXML
    private Label label;
	
	@FXML
	private ComboBox<SerialPort> comboPort;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboPort.setItems(FXCollections.observableArrayList(serialService.getListPorts()));
    }    
}
