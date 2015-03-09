package net.collaud.fablab.gcodesender.gcode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Setter
@Getter
public class GcodeFile {
	private final File file;
	private List<GcodeCommand> commands;

	public GcodeFile(File file) {
		this.file = file;
		commands = new ArrayList<>();
	}
	
	
}
