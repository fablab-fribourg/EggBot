package net.collaud.fablab.gcodesender.serial;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.Constants;
import net.collaud.fablab.gcodesender.util.FXUtils;
import net.collaud.fablab.gcodesender.config.Config;
import net.collaud.fablab.gcodesender.config.ConfigKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class SerialService implements Constants {
	
	@Autowired
	private Config config;

	private SerialPort openPort;

	@Getter
	private final ObjectProperty<PortStatus> portStatus = new SimpleObjectProperty<>(PortStatus.CLOSED);

	@Getter
	private final BlockingQueue<String> readingQueue = new LinkedBlockingQueue<>();

	public List<SerialPortDefinition> getListPorts() {
		return Arrays.stream(SerialPortList.getPortNames())
				.map(n -> new SerialPortDefinition(n))
				.collect(Collectors.toList());
	}

	synchronized public void openPort(SerialPortDefinition def) {
		portStatus.set(PortStatus.OPENNING);
		new PortOpenenr(def.getName()).start();
	}

	synchronized public void write(String line) throws SerialPortException {
		openPort.writeString(line + "\n");
	}

	public void closePort() {
		try {
			FXUtils.setInFXThread(portStatus, PortStatus.CLOSING);
			openPort.closePort();
			FXUtils.setInFXThread(portStatus, PortStatus.CLOSED);
		} catch (SerialPortException ex) {
			Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	class PortOpenenr extends Thread {

		private final String portName;
		private final int arduinoReadyTimeout;

		public PortOpenenr(String portName) {
			super("Serial port opener : " + portName);
			this.portName = portName;
			arduinoReadyTimeout = config.getIntProperty(ConfigKey.ARDUINO_READY_TIMEOUT_MS);
		}

		@Override
		public void run() {
			try {
				openPort = new SerialPort(portName);
				openPort.openPort();
				openPort.setParams(SerialPort.BAUDRATE_115200,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				openPort.addEventListener(new SerialPortReader());

				FXUtils.setInFXThread(portStatus, PortStatus.WAITING_FOR_ARDUINO);

				String rep;
				do {
					rep = readingQueue.poll(arduinoReadyTimeout, TimeUnit.MILLISECONDS);
				} while (rep != null && !rep.startsWith("ready"));
				if (rep == null) {
					FXUtils.setInFXThread(portStatus, PortStatus.NOT_RESPONDING);
				} else {
					FXUtils.setInFXThread(portStatus, PortStatus.OPEN);
				}

			} catch (InterruptedException | SerialPortException ex) {
				log.error("Cannot open port", ex);
				FXUtils.setInFXThread(portStatus, PortStatus.ERROR);
			}
		}
	}

	class SerialPortReader implements SerialPortEventListener {

		private StringBuilder buffer = new StringBuilder();

		@Override
		synchronized public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {
				try {
					//FIXME read untile new line
					byte buff[] = openPort.readBytes();
					if (buff != null) {
						buffer.append(new String(buff));
						checkForNewLine();
					}

				} catch (SerialPortException ex) {
					log.error("Error while reading", ex);
				}
			}
		}

		public void checkForNewLine() {
			String buffStr = buffer.toString();

			int lastIndex = 0;
			int index;
			while ((index = buffStr.indexOf("\n", lastIndex)) > 0) {
				String line = buffStr.substring(lastIndex, index);
				log.info("new line : {}", line);
				readingQueue.add(line);
				lastIndex = index + 1;
			}
			buffer = new StringBuilder(buffStr.substring(lastIndex));
		}
	}
}
