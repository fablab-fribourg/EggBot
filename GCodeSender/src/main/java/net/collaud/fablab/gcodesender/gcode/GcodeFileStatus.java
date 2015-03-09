package net.collaud.fablab.gcodesender.gcode;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
public enum GcodeFileStatus {

	NO_FILE_SELECTED("No file selected"),
	READING("Reading"),
	COMPLETE("Complete"),
	ERROR("Error");
	
	private final String name;

	private GcodeFileStatus(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
