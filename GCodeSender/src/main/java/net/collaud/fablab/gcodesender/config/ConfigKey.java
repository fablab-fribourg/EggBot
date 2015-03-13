package net.collaud.fablab.gcodesender.config;

import lombok.Getter;

/**
 *
 * @author Gaetan Collaud
 */
public enum ConfigKey {

	ARDUINO_READY_TIMEOUT_MS("arduinoReadyTimeout", "5000"),
	M_COMMAND_WAIT("mCommandWait", "300"),
	LAST_PORT("last.port", null),
	LAST_FILE("last.file", null),
	LAST_SERVO_MIN("last.servoMin", "0"),
	LAST_SERVO_MAX("last.servoMax", "90"),
	LAST_X_MIN("last.xMin", "-40.0"),
	LAST_X_MAX("last.xMax", "40.0"),
	LAST_Y_MIN("last.yMin", "-100.0"),
	LAST_Y_MAX("last.yMax", "100.0");

	@Getter
	private final String name;

	@Getter
	private final String def;

	private ConfigKey(String name, String def) {
		this.name = name;
		this.def = def;
	}

}
