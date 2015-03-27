package net.collaud.fablab.gcodesender.gcode;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.annotation.PostConstruct;
import jssc.SerialPortException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.Constants;
import net.collaud.fablab.gcodesender.config.Config;
import net.collaud.fablab.gcodesender.config.ConfigKey;
import net.collaud.fablab.gcodesender.serial.SerialService;
import net.collaud.fablab.gcodesender.tools.Observable;
import net.collaud.fablab.gcodesender.util.FXUtils;
import net.collaud.fablab.gcodesender.util.GcodeValueParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud
 */
@Service
@Slf4j
public class GcodeService extends Observable<GcodeNotifyMessage> implements Constants {

	@Autowired
	private GcodeConverterService gcodeConverterService;

	@Autowired
	private SerialService serialService;

	@Autowired
	private GcodeFileService fileService;

	@Autowired
	private Config config;

	private final GcodeSenderThread senderThread;

	@Getter
	private final DoubleProperty currentPositionX = new SimpleDoubleProperty(0);

	@Getter
	private final DoubleProperty currentPositionY = new SimpleDoubleProperty(0);

	@Getter
	private final DoubleProperty currentPositionServo = new SimpleDoubleProperty(0);

	private int mCommandWait;
	private boolean wasPrinting = false;

	public GcodeService() {
		senderThread = new GcodeSenderThread();
		senderThread.start();
		mCommandWait = 0;
	}

	public void print(double scale) {
		wasPrinting = true;
		mCommandWait = config.getIntProperty(ConfigKey.M_COMMAND_WAIT);
		fileService.getGcodeFile().get().getCommands()
				.forEach(c -> {
					c = gcodeConverterService.inkscapeZToServo(c);
					c = c.scale(scale);
					senderThread.addCommand(c);
				});
	}

	synchronized public void sendGCodeLine(String line) {
		Optional<GcodeCommand> cmd = GcodeCommand.parse(line);
		if (cmd.isPresent()) {
			senderThread.addCommand(cmd.get());
		} else {
			notifyError("Cannot parse gcode : " + line);
		}
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
		senderThread.cancelAllPendingCommands();
		notifyError("Print cancelled !");
	}

	public void release() {
		senderThread.addCommand(GcodeCommand.parse("M18").get());
	}

	public void setHome() {
		senderThread.addCommand(GcodeCommand.parse("M19").get());
	}

	public void goHome(double servoPos) {
		String servoStr = GcodeValueParser.format(servoPos);
		senderThread.addCommand(GcodeCommand.parse("M300S" + servoStr).get());
		senderThread.addCommand(GcodeCommand.parse("G00 X0.0000 Y0.0000").get());
	}

	public void move(Motor motor, double position) {
		String posStr = GcodeValueParser.format(position);
		switch (motor) {
			case PEN:
				senderThread.addCommand(GcodeCommand.parse("M300S" + posStr).get());
				break;
			case X:
				senderThread.addCommand(GcodeCommand.parse("G00 X" + posStr).get());
				break;
			case Y:
				senderThread.addCommand(GcodeCommand.parse("G00 Y" + posStr).get());
				break;
		}
	}

	private void updatePropertiesFromCommand(GcodeCommand cmd) {
		cmd.x.ifPresent(v -> FXUtils.setInFXThread(currentPositionX, v));
		cmd.y.ifPresent(v -> FXUtils.setInFXThread(currentPositionY, v));
		cmd.servo.ifPresent(v -> FXUtils.setInFXThread(currentPositionServo, v));
	}

	public class GcodeSenderThread extends Thread {

		private final BlockingQueue<GcodeCommand> queue = new LinkedBlockingQueue<>();
		private boolean running = true;

		public GcodeSenderThread() {
			super("GcodeSender");
		}

		synchronized public void cancelAllPendingCommands() {
			queue.clear();
		}

		synchronized public void addCommand(GcodeCommand cmd) {
			queue.add(cmd);
		}

		synchronized public void addCommands(Collection<GcodeCommand> cmds) {
			queue.addAll(cmds);
		}

		@Override
		public void run() {
			while (running) {
				try {
					if (queue.isEmpty() && wasPrinting) {
						GcodeNotifyMessage msg = new GcodeNotifyMessage(GcodeNotifyMessage.Type.INFO, "End of pring");
						msg.setEndOfPrint(true);
						wasPrinting = false;
						mCommandWait = 0;
						notifyObservers(msg);
					}

					final GcodeCommand next = queue.take();
					writeLineAndWaitOk(next);
					if (next.getType() == GcodeCommand.Type.M && next.getCode() == 300) {
						//pen cmd
						try {
							Thread.sleep(mCommandWait);
						} catch (InterruptedException ex) {
						}
					}
				} catch (InterruptedException ex) {
					log.error("Gcode Sender Thread interupted", ex);
					running = false;
				}
			}
		}

		private boolean writeLineAndWaitOk(GcodeCommand line) {
			updatePropertiesFromCommand(line);
			notifyInfo(line.toString());
			try {
				String rep;
				if (serialService.write(line.toString())) {
					do {
						rep = serialService.getReadingQueue().take();
						if (rep != null) {
							if (!rep.startsWith("ok")) {
								notifyError("Wrong message received : " + rep);
							}
						}
					} while (rep != null && !rep.startsWith("ok"));
				}
			} catch (SerialPortException | InterruptedException ex) {
				notifyError("Error while writing gcode line : " + line, ex);
				return false;
			}
			return true;
		}
	}
}
