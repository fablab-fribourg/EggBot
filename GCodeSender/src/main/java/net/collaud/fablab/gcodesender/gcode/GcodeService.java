package net.collaud.fablab.gcodesender.gcode;

import java.io.File;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.serial.SerialConnexion;
import net.collaud.fablab.gcodesender.serial.SerialPortDefinition;
import net.collaud.fablab.gcodesender.serial.SerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class GcodeService extends Observable{
	@Autowired
	private SerialService serialService;
	
	public void sendFile(File file, SerialPortDefinition port){
		notifyObservers("Opening port "+port);
		try {
			SerialConnexion con = new SerialConnexion(port);
		} catch (SerialPortException ex) {
			log.error("Problem with serial", ex);
		}
	}
}
