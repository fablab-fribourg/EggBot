package net.collaud.fablab.gcodesender.serial;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jssc.SerialPortList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Service
@Slf4j
public class SerialService {

	public List<SerialPortDefinition> getListPorts() {
		return Arrays.stream(SerialPortList.getPortNames())
				.map(n -> new SerialPortDefinition(n))
				.collect(Collectors.toList());
	}
	
}
