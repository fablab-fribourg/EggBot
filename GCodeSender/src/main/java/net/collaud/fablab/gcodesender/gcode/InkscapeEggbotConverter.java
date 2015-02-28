package net.collaud.fablab.gcodesender.gcode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gaetan
 */
public class InkscapeEggbotConverter {

	public static String lineConvertLine(String line) {
		Pattern pattern = Pattern.compile(".*G([0-9]+) Z([0-9\\.\\-]+).*");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			float value = Float.parseFloat(matcher.group(2));
			value *= 10;
			if (value < 0) {
				value = 0.0f;
			}
			return "M300S" + value;
		}
		return line;
	}
}
