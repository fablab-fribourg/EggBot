package net.collaud.fablab.gcodesender.gcode;

import java.util.Optional;

/**
 *
 * @author gaetan
 */
public class GcodeConverter {

	public static GcodeCommand inkscapeZToServo(GcodeCommand cmd) {
		if(cmd.getType()==GcodeCommand.Type.G 
				&& cmd.getZ().isPresent()
				&& !cmd.getX().isPresent() 
				&& !cmd.getY().isPresent()){
			double z = cmd.getZ().get()*10;
			if(z<0){
				z=0;
			}
			cmd = new GcodeCommand();
			cmd.setType(GcodeCommand.Type.M);
			cmd.setCode(300);
			cmd.setServo(Optional.of(z));
		}
		return cmd;
	}
	
	public static GcodeCommand scale(GcodeCommand cmd, double scaleFactor){
		return cmd;
	}
}
