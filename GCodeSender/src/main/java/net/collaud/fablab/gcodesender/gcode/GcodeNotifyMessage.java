package net.collaud.fablab.gcodesender.gcode;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Gaetan Collaud
 */
@Data
@AllArgsConstructor
public class GcodeNotifyMessage {

	public enum Type {
		INFO,
		ERROR
	};
	private Type type;
	private String message;
	private boolean endOfPrint = false;

	public GcodeNotifyMessage(Type type, String message) {
		this.type = type;
		this.message = message;
	}
}
