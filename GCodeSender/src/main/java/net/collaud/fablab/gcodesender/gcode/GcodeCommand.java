package net.collaud.fablab.gcodesender.gcode;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.util.GcodeValueParser;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Getter
@Setter(AccessLevel.PROTECTED)
@Slf4j
public class GcodeCommand {

	public enum Type {

		UNKNOWN,
		M,
		G
	};

	protected Type type = Type.UNKNOWN;
	protected Integer code = -1;
	protected Optional<Double> x = Optional.empty();
	protected Optional<Double> y = Optional.empty();
	protected Optional<Double> z = Optional.empty();
	protected Optional<Double> i = Optional.empty();
	protected Optional<Double> j = Optional.empty();
	protected Optional<Double> servo = Optional.empty();
	protected Optional<Double> feed = Optional.empty();

	public static Optional<GcodeCommand> parse(String line) {
		if (line == null || line.length() < 2) {
			return Optional.empty();
		}
		Optional<GcodeCommand> cmd = new GcodeCommand()
				.parseTypeAndCode(line);
		if (cmd.isPresent()) {
			if (cmd.get().getType() == Type.M) {
				cmd = cmd.map(c -> c.parseServoValue(line));
			} else if (cmd.get().getType() == Type.G) {
				cmd = cmd.map(c -> c.parseStandardValues(line));
			}
		}
		return cmd;
	}

	protected Optional<GcodeCommand> parseTypeAndCode(String line) {
		char c = line.substring(0, 1).toLowerCase().charAt(0);
		switch (c) {
			case 'm':
				setType(Type.M);
				break;
			case 'g':
				setType(Type.G);
				break;
			default:
				return Optional.empty();
		}
		getDoubleValueFromIndex(line.toCharArray(), 1).ifPresent(v -> setCode(v.intValue()));
		return Optional.of(this);
	}

	protected GcodeCommand parseStandardValues(String line) {
		setX(parseValue(line, 'X'));
		setY(parseValue(line, 'Y'));
		setZ(parseValue(line, 'Z'));
		setI(parseValue(line, 'I'));
		setJ(parseValue(line, 'J'));
		setFeed(parseValue(line, 'F'));
		return this;
	}

	protected Optional<Double> parseValue(String line, char name) {
		int indexStart = line.indexOf(name);
		if (indexStart > 0) {
			return getDoubleValueFromIndex(line.toCharArray(), indexStart + 1);
		}
		return Optional.empty();
	}

	protected GcodeCommand parseServoValue(String line) {
		setServo(parseValue(line, 'S'));
		return this;
	}

	protected Optional<Double> getDoubleValueFromIndex(char[] line, int index) {
		StringBuilder sb = new StringBuilder();
		char c = line[index];
		while ((c >= '0' && c <= '9') || c == '.' || c == '-') {
			sb.append(c);
			if (++index >= line.length) {
				break;
			}
			c = line[index];
		}
		try {
			return Optional.of(Double.parseDouble(sb.toString()));
		} catch (NumberFormatException ex) {
			log.error("Cannot parse double value in command '" + new String(line) + "'", ex);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.toString());

		if (type == Type.M) {
			sb.append(code);
			servo.ifPresent(v -> sb.append("S").append(v.intValue()));
		} else {
			if (code < 10) {
				sb.append('0');
			}
			sb.append(code);

			Consumer<AbstractMap.SimpleEntry<String, Double>> printValue = (AbstractMap.SimpleEntry<String, Double> t)
					-> sb.append(" ").append(t.getKey()).append(GcodeValueParser.format(t.getValue()));

			x.map(v -> new AbstractMap.SimpleEntry<String, Double>("X", v)).ifPresent(printValue);
			y.map(v -> new AbstractMap.SimpleEntry<String, Double>("Y", v)).ifPresent(printValue);
			z.map(v -> new AbstractMap.SimpleEntry<String, Double>("Z", v)).ifPresent(printValue);
			i.map(v -> new AbstractMap.SimpleEntry<String, Double>("I", v)).ifPresent(printValue);
			j.map(v -> new AbstractMap.SimpleEntry<String, Double>("J", v)).ifPresent(printValue);
			feed.map(v -> new AbstractMap.SimpleEntry<String, Double>("F", v)).ifPresent(printValue);
		}
		return sb.toString();
	}

}
