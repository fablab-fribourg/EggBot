package net.collaud.fablab.gcodesender.gcode;

import java.io.File;
import java.io.FileReader;
import java.util.Observable;
import net.collaud.fablab.gcodesender.serial.SerialPort;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
public class GcodeService extends Observable{
	public void sendFile(File file, SerialPort port){
		
	}
}
