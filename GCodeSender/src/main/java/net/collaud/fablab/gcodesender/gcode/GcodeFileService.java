package net.collaud.fablab.gcodesender.gcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.util.FXUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class GcodeFileService {

	@Getter
	private final ObjectProperty<GcodeFileStatus> fileStatus;

	@Getter
	private final ObjectProperty<GcodeFile> gcodeFile;

	private GcodeFileAnalyserThread thread;

	public GcodeFileService() {
		fileStatus = new SimpleObjectProperty<>(GcodeFileStatus.NO_FILE_SELECTED);
		gcodeFile = new SimpleObjectProperty<>();
	}

	public void analyseFile(File file) {
		if (thread != null) {
			thread.interrupt();
		}
		thread = new GcodeFileAnalyserThread(file);
		thread.start();
	}

	public class GcodeFileAnalyserThread extends Thread {

		private final File file;

		public GcodeFileAnalyserThread(File file) {
			super("GcodeFileService Analyser : " + file.getAbsolutePath());
			this.file = file;
		}

		@Override
		public void run() {
			FXUtils.setInFXThread(fileStatus, GcodeFileStatus.READING);
			List<GcodeCommand> commands = new ArrayList<>();
			GcodeFile gf = new GcodeFile(file);
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					GcodeCommand.parse(line).ifPresent(cmd -> {
						cmd = GcodeConverter.inkscapeZToServo(cmd);
						commands.add(cmd);
					});
				}
				gf.setCommands(commands);
				
				//compute min/max
				
				
				gcodeFile.set(gf);
				//done
				FXUtils.setInFXThread(fileStatus, GcodeFileStatus.COMPLETE);
			} catch (FileNotFoundException ex) {
				log.error("File not found", ex);
				FXUtils.setInFXThread(fileStatus, GcodeFileStatus.ERROR);
			} catch (IOException ex) {
				log.error("Cannot read file", ex);
				FXUtils.setInFXThread(fileStatus, GcodeFileStatus.ERROR);
			}
		}
	}
}
