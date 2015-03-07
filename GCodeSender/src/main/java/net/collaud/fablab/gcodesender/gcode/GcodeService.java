package net.collaud.fablab.gcodesender.gcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jssc.SerialPortException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.Constants;
import net.collaud.fablab.gcodesender.config.Config;
import net.collaud.fablab.gcodesender.config.ConfigKey;
import net.collaud.fablab.gcodesender.serial.SerialPortDefinition;
import net.collaud.fablab.gcodesender.serial.SerialService;
import net.collaud.fablab.gcodesender.tools.Observable;
import net.collaud.fablab.gcodesender.util.FXUtils;
import net.collaud.fablab.gcodesender.util.GcodeValueParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class GcodeService extends Observable<GcodeNotifyMessage> implements Constants {

	@Autowired
	private SerialService serialService;

	@Autowired
	private Config config;

	private Queue<GcodeCommand> commandQueue;

	private GcodeThread thread;

	@Getter
	private final DoubleProperty currentPositionX = new SimpleDoubleProperty(0);

	@Getter
	private final DoubleProperty currentPositionY = new SimpleDoubleProperty(0);

	@Getter
	private final DoubleProperty currentPositionServo = new SimpleDoubleProperty(0);

	public void print(File file, SerialPortDefinition port) {
		Optional.ofNullable(thread).ifPresent(t -> t.interrupt());
		thread = new GcodeThread(file, port);
		thread.start();
	}

	protected void notifyInfo(String msg) {
		log.info("notify : " + msg);
		notifyObservers(new GcodeNotifyMessage(GcodeNotifyMessage.Type.INFO, msg));
	}

	protected void notifyError(String msg) {
		notifyError(msg, null);
	}

	protected void notifyError(String msg, Exception ex) {
		log.error(msg, ex);
		if (ex != null) {
			notifyObservers(new GcodeNotifyMessage(GcodeNotifyMessage.Type.ERROR, msg + " : " + ex.getMessage()));
		} else {
			notifyObservers(new GcodeNotifyMessage(GcodeNotifyMessage.Type.ERROR, msg));
		}
	}

	public void stopPrint() {
		Optional.ofNullable(thread).ifPresent(t -> t.interrupt());
		notifyError("Print cancelled !");
	}

	public void release() {
		writeLineAndWaitOk(GcodeCommand.parse("M18").get());
	}

	public void setHome() {
		writeLineAndWaitOk(GcodeCommand.parse("M19").get());
	}

	public void goHome(double servoPos) {
		String servoStr = GcodeValueParser.format(servoPos);
		writeLineAndWaitOk(GcodeCommand.parse("M300S" + servoStr).get());
		writeLineAndWaitOk(GcodeCommand.parse("G00 X0.0000 Y0.0000").get());
	}

	public void move(Motor motor, double position) {
		String posStr = GcodeValueParser.format(position);
		switch (motor) {
			case PEN:
				writeLineAndWaitOk(GcodeCommand.parse("M300S" + posStr).get());
				break;
			case X:
				writeLineAndWaitOk(GcodeCommand.parse("G00 X" + posStr).get());
				break;
			case Y:
				writeLineAndWaitOk(GcodeCommand.parse("G00 Y" + posStr).get());
				break;
		}
	}

	private void updatePropertiesFromCommand(GcodeCommand cmd) {
		cmd.x.ifPresent(v -> FXUtils.setInFXThread(currentPositionX, v));
		cmd.y.ifPresent(v -> FXUtils.setInFXThread(currentPositionY, v));
		cmd.servo.ifPresent(v -> FXUtils.setInFXThread(currentPositionServo, v));
	}

	private boolean writeLineAndWaitOk(GcodeCommand line) {
		updatePropertiesFromCommand(line);
		notifyInfo(line.toString());
		try {
			String rep;
			serialService.write(line.toString());
			do {
				rep = serialService.getReadingQueue().take();
				if (rep != null && !rep.startsWith("ok")) {
					notifyError("Wrong message received : " + rep);
				}
			} while (rep != null && !rep.startsWith("ok"));
		} catch (SerialPortException | InterruptedException ex) {
			notifyError("Error while writing gcode line : " + line, ex);
			return false;
		}
		return true;
	}

	class GcodeThread extends Thread {

		private final File file;
		private final SerialPortDefinition port;
		private final int mCommandWait;

		public GcodeThread(File file, SerialPortDefinition port) {
			super("Gcode thread : " + file.getAbsolutePath());
			this.file = file;
			this.port = port;
			mCommandWait = config.getIntProperty(ConfigKey.M_COMMAND_WAIT);
		}

		@Override
		public void run() {
			notifyInfo("Loading GCode from " + file.getAbsolutePath());
			int nbLines = readLines(file);
			notifyInfo("Gcode loaded, " + nbLines + " lines read");
			notifyInfo("Opening port " + port);
			for (GcodeCommand cmd : commandQueue) {
				if (!writeLineAndWaitOk(cmd)) {
					//Something when wrong !
					break;
				}
				if (cmd.getType() == GcodeCommand.Type.M && cmd.getCode() == 300) {
					//pen cmd
					try {
						Thread.sleep(mCommandWait);
					} catch (InterruptedException ex) {
					}
				}
			}
			notifyObservers(new GcodeNotifyMessage(GcodeNotifyMessage.Type.INFO, "% End of print", true));
		}

		private int readLines(File file) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				commandQueue = new LinkedList<>();
				String line;
				while ((line = br.readLine()) != null) {
					GcodeCommand.parse(line).ifPresent(cmd -> {
						cmd = GcodeConverter.inkscapeZToServo(cmd);
						cmd = GcodeConverter.scale(cmd, 1.0);
						commandQueue.add(cmd);
					});
				}
			} catch (FileNotFoundException ex) {
				notifyError("File not found", ex);
			} catch (IOException ex) {
				notifyError("Cannot read file", ex);
			}
			return commandQueue.size();
		}

		private boolean isGcodeLine(String line) {
			return line.startsWith("G") || line.startsWith("M");
		}
	}
}
