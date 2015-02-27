package net.collaud.fablab.gcodesender.serial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerialPort {

	private String name;
	
	@Override
	public String toString(){
		return name;
	}
}
