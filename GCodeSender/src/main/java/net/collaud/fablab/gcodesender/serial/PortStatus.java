package net.collaud.fablab.gcodesender.serial;

import lombok.Getter;

/**
 *
 * @author Gaetan Collaud
 */
public enum PortStatus {

	CLOSED("Closed", "Port is closed"),
	OPENNING("Openning", "Port is currently openning, please wait"),
	OPEN("Open and ready", "Port is open and ready to send data"),
	WAITING_FOR_ARDUINO("Waiting for Arduino", "Port is open, waiting for arduino to be ready"),
	CLOSING("Closing", "Port is currently closing"),
	ERROR("Error", "An error occur with the port, please try to open it again"),
	NOT_RESPONDING("Not responding", "The port was open, but the arduino did not send the 'ready' signal");

	private final String name;

	@Getter
	private final String detail;

	private PortStatus(String name, String detail) {
		this.name = name;
		this.detail = detail;
	}

	@Override
	public String toString() {
		return name;
	}

}
