package net.collaud.fablab.gcodesender.controller.model;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Getter
@Setter
public class Limits {

	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;

	public Limits applyScale(double scale) {
		Limits lim = new Limits();
		lim.xMin = this.xMin * scale;
		lim.xMax = this.xMax * scale;
		lim.yMin = this.yMin * scale;
		lim.yMax = this.yMax * scale;
		return lim;
	}
}
