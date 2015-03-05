/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.collaud.fablab.gcodesender.gcode;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gaetan
 */
public class InkscapeEggbotConverterTest {

	/**
	 * Test of lineConvertLine method, of class InkscapeEggbotConverter.
	 */
	@Test
	public void testLineConvertLine() {
		Map<String, String> map = new HashMap<>();
		map.put("G00 Z3.000000", "M300S30");
		map.put("G00 Z-0.25", "M300S0");

		for (Map.Entry<String, String> entry : map.entrySet()) {
			GcodeCommand cmd = GcodeCommand.parse(entry.getKey()).get();
			cmd = GcodeConverter.inkscapeZToServo(cmd);
			assertEquals(entry.getValue(), cmd.toString());
		}

	}

}
