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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author gaetan
 */
public class InkscapeEggbotConverterTest {

	private GcodeConverterService gcodeConverterService = new GcodeConverterService();

	/**
	 * Test of lineConvertLine method, of class InkscapeEggbotConverter.
	 */
	@Test
	public void testLineConvertLine() {
		gcodeConverterService.setServoMin(10);
		gcodeConverterService.setServoMax(70);

		Map<String, String> map = new HashMap<>();
		map.put("G00 Z3.000000", "M300S70");
		map.put("G00 Z0.1", "M300S70");
		map.put("G00 Z0", "M300S10");
		map.put("G00 Z-0.25", "M300S10");

		map.entrySet().stream().forEach((entry) -> {
			GcodeCommand cmd = GcodeCommand.parse(entry.getKey()).get();
			cmd = gcodeConverterService.inkscapeZToServo(cmd);
			assertEquals(entry.getValue(), cmd.toString());
		});

	}

}
