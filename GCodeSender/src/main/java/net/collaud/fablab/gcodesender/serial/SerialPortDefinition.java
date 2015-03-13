package net.collaud.fablab.gcodesender.serial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gaetan Collaud
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerialPortDefinition {

	private String name;
	
	@Override
	public String toString(){
		return name;
	}
}
