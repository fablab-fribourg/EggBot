package net.collaud.fablab.gcodesender.serial;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Slf4j
public class SerialConnexion {

	private final SerialPort serialPort;

	@Getter
	private final Queue<String> queue = new ConcurrentLinkedQueue<>();

	public SerialConnexion(SerialPortDefinition definition) throws SerialPortException {
		serialPort = new SerialPort(definition.getName());
		serialPort.openPort();//Open serial port
		serialPort.setParams(SerialPort.BAUDRATE_57600,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);

		serialPort.addEventListener(new SerialPortReader());

	}

	synchronized private void write(String line) throws SerialPortException {
		serialPort.writeString(line);
		serialPort.writeString("\n");
	}

	synchronized void close() throws SerialPortException {
		serialPort.closePort();
	}
	
	class SerialPortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {
				try {
					byte buffer[] = serialPort.readBytes();
					String line = new String(buffer);
					log.info("read : {}", line);
					queue.add(line);
				} catch (SerialPortException ex) {
					log.error("Error while reading", ex);
				}
			}
		}
	}
}
