package net.collaud.fablab.gcodesender.config;

import lombok.Getter;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
public enum ConfigKey {

	ARDUINO_READY_TIMEOUT_MS("arduinoReadyTimeout", "5000"),
	M_COMMAND_WAIT("mCommandWait", "300"),
	LAST_PORT("last.port", null),
	LAST_FILE("last.file", null);

	@Getter
	private final String name;

	@Getter
	private final String def;

	private ConfigKey(String name, String def) {
		this.name = name;
		this.def = def;
	}

}
