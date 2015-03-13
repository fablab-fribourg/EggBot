package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.config.Config;
import net.collaud.fablab.gcodesender.config.ConfigKey;
import net.collaud.fablab.gcodesender.controller.model.LimitsProperty;
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
	
	@Autowired
	private Config config;

	@FXML
	private LinearControlController servoController;

	@Getter
	@FXML
	private LinearControlController xController;

	@Getter
	@FXML
	private LinearControlController yController;

	private final Semaphore semNewValue = new Semaphore(0);
	private final Map<Motor, Double> changedValue = new ConcurrentHashMap<>();

	private ChangeThread changeThread;
	
	private LimitsProperty limits;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		changeThread = new ChangeThread();
		changeThread.start();
		
		servoController.init("Servo", config.getDoubleProperty(ConfigKey.LAST_SERVO_MIN), config.getDoubleProperty(ConfigKey.LAST_SERVO_MAX));
		xController.init("X", config.getDoubleProperty(ConfigKey.LAST_X_MIN), config.getDoubleProperty(ConfigKey.LAST_X_MAX));
		yController.init("Y", config.getDoubleProperty(ConfigKey.LAST_Y_MIN), config.getDoubleProperty(ConfigKey.LAST_Y_MAX));

		servoController.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.PEN, newValue.doubleValue());
			semNewValue.release();
		});

		xController.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.X, newValue.doubleValue());
			semNewValue.release();
		});

		yController.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			changedValue.put(Motor.Y, newValue.doubleValue());
			semNewValue.release();
		});
		
		limits = new LimitsProperty(xController.getMin(), xController.getMax(), yController.getMin(), yController.getMax());

		//Config bind
		linkConfigSave(ConfigKey.LAST_SERVO_MIN, servoController.getMin());
		linkConfigSave(ConfigKey.LAST_SERVO_MAX, servoController.getMax());
		linkConfigSave(ConfigKey.LAST_X_MIN, xController.getMin());
		linkConfigSave(ConfigKey.LAST_X_MAX, xController.getMax());
		linkConfigSave(ConfigKey.LAST_Y_MIN, yController.getMin());
		linkConfigSave(ConfigKey.LAST_Y_MAX, yController.getMax());
		
		//realtime binding
		gcodeService.getCurrentPositionServo().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			servoController.setValue(newValue.doubleValue());
		});
		gcodeService.getCurrentPositionX().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			xController.setValue(newValue.doubleValue());
		});
		gcodeService.getCurrentPositionY().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			yController.setValue(newValue.doubleValue());
		});
	}
	
	private void linkConfigSave(ConfigKey key, DoubleProperty property){
		property.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			config.setProperty(key, newValue);
		});
	}

	@FXML
	private void actionRelease() {
		new Thread(gcodeService::release).start();
	}

	@FXML
	private void actionGoHome() {
		new Thread(() -> gcodeService.goHome(servoController.getMax().get())).start();
	}

	@FXML
	private void actionSetHome() {
		new Thread(gcodeService::setHome).start();
	}

	public class ChangeThread extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					semNewValue.acquire();
					while (!changedValue.isEmpty()) {
						for (Motor m : Motor.values()) {
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
	
	public LimitsProperty getLimits(){
		return limits;
	}

}
