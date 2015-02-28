package net.collaud.fablab.gcodesender.gcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Optional;
import java.util.Queue;
import jssc.SerialPortException;
import lombok.extern.slf4j.Slf4j;
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
public class GcodeService extends Observable {

	@Autowired
	private SerialService serialService;

	private Queue<String> lines;

	private GcodeThread thread;

	public void sendFile(File file, SerialPortDefinition port) {
		Optional.ofNullable(thread).ifPresent(t -> t.interrupt());
		thread = new GcodeThread(file, port);
		thread.start();
	}

	protected void notifyInfo(String msg) {
		log.info("notify : " + msg);
		notify(new NotifyMessage(NotifyMessage.Type.INFO, msg));
	}

	protected void notifyError(String msg) {
		notifyError(msg, null);
	}

	protected void notifyError(String msg, Exception ex) {
		log.error(msg, ex);
		if (ex != null) {
			notify(new NotifyMessage(NotifyMessage.Type.ERROR, msg + " : " + ex.getMessage()));
		} else {
			notify(new NotifyMessage(NotifyMessage.Type.ERROR, msg));
		}
	}

	protected void notify(NotifyMessage msg) {
		setChanged();
		notifyObservers(msg);
	}

	class GcodeThread extends Thread {

		private final File file;
		private final SerialPortDefinition port;

		public GcodeThread(File file, SerialPortDefinition port) {
			super("Gcode thread : " + file.getAbsolutePath());
			this.file = file;
			this.port = port;
		}

		@Override
		public void run() {
			notifyInfo("Loading GCode from " + file.getAbsolutePath());
			int nbLines = readLines(file);
			notifyInfo("Gcode loaded, " + nbLines + " lines read");
			notifyInfo("Opening port " + port);
			try {
				serialService.getReadingQueue().clear();
				serialService.openPort(port);
				sleep(5000);

				notifyInfo("Start sending GCode");

				for (String line : lines) {
					notifyInfo("Sending : " + line);
					serialService.write(line);
					String rep;
					do {
						rep = serialService.getReadingQueue().take();
						if(rep != null && !rep.startsWith("ok")){
							notifyError("Wrong message received : "+rep);
						}
					} while (rep != null && !rep.startsWith("ok"));
				}

				serialService.closePort();
			} catch (SerialPortException ex) {
				notifyError("Problem with serial", ex);
			} catch (InterruptedException ex) {
				//nothing to do here
			}
			notifyInfo("End of print");
		}

		private int readLines(File file) {
			int count = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				lines = new LinkedList<>();
				String line;
				while ((line = br.readLine()) != null) {
					//FIXME remove comment
					if (isGcodeLine(line)) {
						lines.add(line);
						count++;
					}
				}
			} catch (FileNotFoundException ex) {
				notifyError("File not found", ex);
			} catch (IOException ex) {
				notifyError("Cannot read file", ex);
			}
			return count;
		}

		private boolean isGcodeLine(String line) {
			return line.startsWith("G") || line.startsWith("M");
		}
	}
}
