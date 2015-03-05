package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import net.collaud.fablab.gcodesender.gcode.Motor;
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
	private GcodeService gcodeService;
	
	@FXML
	private LinearControlController servoController;
	
	@FXML
	private LinearControlController xController;
	
	@FXML
	private LinearControlController yController;
	
	private final Semaphore semNewValue = new Semaphore(0);
	private final Map<Motor, Double> changedValue = new ConcurrentHashMap<>();
	
	private ChangeThread changeThread;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		changeThread = new ChangeThread();
		changeThread.start();
		
		servoController.init("Servo", 0, 90);
		xController.init("X", -40, 40);
		yController.init("Y", -100, 100);
		
		servoController.getValueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.PEN, newValue.doubleValue());
			semNewValue.release();
		});
		
		xController.getValueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.X, newValue.doubleValue());
			semNewValue.release();
		});
		
		yController.getValueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.Y, newValue.doubleValue());
			semNewValue.release();
		});
		
//		servoController.getValueProperty().bind(gcodeService.getCurrentPositionServo());
//		xController.getValueProperty().bind(gcodeService.getCurrentPositionX());
//		yController.getValueProperty().bind(gcodeService.getCurrentPositionY());
	}
	
	
	
	public class ChangeThread extends Thread{
		
		@Override
		public void run(){
			while(true){
				try {
					semNewValue.acquire();
					while(!changedValue.isEmpty()){
						for(Motor m : Motor.values()){
							Optional.ofNullable(changedValue.remove(m))
									.ifPresent(v -> gcodeService.move(m, v));
						}
					}
					
				} catch (InterruptedException ex) {
					log.error("Interrupted", ex);
				}
			}
		}
		
	}
	
}
