package net.collaud.fablab.gcodesender.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class SerialService {

	private SerialPort openPort;

	@Getter
	private final BlockingQueue<String> readingQueue = new LinkedBlockingQueue<String>();

	public List<SerialPortDefinition> getListPorts() {
		return Arrays.stream(SerialPortList.getPortNames())
				.map(n -> new SerialPortDefinition(n))
				.collect(Collectors.toList());
	}

	synchronized public void openPort(SerialPortDefinition def) throws SerialPortException {
		openPort = new SerialPort(def.getName());
		openPort.openPort();
		openPort.setParams(SerialPort.BAUDRATE_115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		openPort.addEventListener(new SerialPortReader());
	}

	synchronized public void write(String line) throws SerialPortException {
		openPort.writeString(line + "\n");
	}

	public void closePort() throws SerialPortException {
		openPort.closePort();
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
			boolean lineFound;
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

//				StringBuilder sb = new StringBuilder();
//				for (int i = 0; i < buffer.size(); i++) {
//					byte b = buffer.get(i);
//					if (b == '\n') {
//						lineFound = true;
//						buffer = buffer.subList(i+1, buffer.size() - 1);
//						String line = sb.toString();
//						log.info("new line : {}", line);
//						readingQueue.add(sb.toString());
//						break;
//					} else {
//						sb.append((char)b);
//					}
//				}
		}
	}
}
