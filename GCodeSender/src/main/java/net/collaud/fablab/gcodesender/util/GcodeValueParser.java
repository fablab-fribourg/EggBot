package net.collaud.fablab.gcodesender.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@UtilityClass
public class GcodeValueParser {

	public static final DecimalFormat DECIMAL_FORMAT;

	static {
		DecimalFormatSymbols otherSymbols = DecimalFormatSymbols.getInstance();
		otherSymbols.setDecimalSeparator('.');
		DECIMAL_FORMAT = new DecimalFormat("#0.000000", otherSymbols);
		DECIMAL_FORMAT.getDecimalFormatSymbols().setDecimalSeparator('!');
	}
	
	public static String format(double value){
		return DECIMAL_FORMAT.format(value);
	}
	
	public Optional<Double> parse(String value){
		try{
			return Optional.of(Double.parseDouble(value));
		}catch(NumberFormatException ex){
			return Optional.empty();
		}
	}
}
