package net.collaud.fablab.gcodesender.gcode;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Data
@AllArgsConstructor
public class NotifyMessage {
	public enum Type{
		INFO,
		ERROR
	};
	
	private Type type;
	private String message;
}
