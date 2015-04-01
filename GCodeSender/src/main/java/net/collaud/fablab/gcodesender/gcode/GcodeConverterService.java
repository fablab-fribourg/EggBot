package net.collaud.fablab.gcodesender.gcode;

import java.util.Optional;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;
import lombok.Setter;
import net.collaud.fablab.gcodesender.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author gaetan
 */
@Service
public class GcodeConverterService {

	@Setter
	private double servoMin = 0;

	@Setter
	private double servoMax = 90;
	
	public GcodeCommand inkscapeZToServo(GcodeCommand cmd) {
		if (cmd.getType() == GcodeCommand.Type.G
				&& cmd.getZ().isPresent()
				&& !cmd.getX().isPresent()
				&& !cmd.getY().isPresent()) {
			double z = cmd.getZ().get();
			if(z<=0){
				z = servoMin;
			}else{
				z = servoMax;
			}
			cmd = new GcodeCommand();
			cmd.setType(GcodeCommand.Type.M);
			cmd.setCode(300);
			cmd.setServo(Optional.of(z));
		}
		return cmd;
	}

}
