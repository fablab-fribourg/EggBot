package net.collaud.fablab.gcodesender.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.Constants;
import org.springframework.stereotype.Component;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Component
@Slf4j
public class Config implements Constants {
	
	private final Properties prop;
	
	private Config() {
		InputStream is;
		prop = new Properties();
		File f = new File(CONFIG_FILE);
		try {
			is = new FileInputStream(f);
			prop.load(is);
		} catch (IOException ex) {
			log.warn("Cannot read file {} because {}", f.getAbsolutePath(), ex.getMessage());
		}
	}
	
	public Optional<String> getOptionalProperty(ConfigKey key){
		return Optional.ofNullable(getProperty(key));
	}
	
	public String getProperty(ConfigKey key) {
		return prop.getProperty(key.getName(), key.getDef());
	}
	
	public int getIntProperty(ConfigKey key) {
		String v = getProperty(key);
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException ex) {
			log.error("Cannot parse config key " + key.getName() + " with value " + v + " to integer", ex);
		}
		return Integer.parseInt(key.getDef());
	}
	
	public double getDoubleProperty(ConfigKey key) {
		String v = getProperty(key);
		try {
			return Double.parseDouble(v);
		} catch (NumberFormatException ex) {
			log.error("Cannot parse config key " + key.getName() + " with value " + v + " to double", ex);
		}
		return Double.parseDouble(key.getDef());
	}
	
	public void setProperty(ConfigKey key, Object value) {
		if (value == null) {
			prop.remove(key.getName());
		} else {
			prop.setProperty(key.getName(), value.toString());
		}
		writeProperties();
	}
	
	private void writeProperties() {
		try (OutputStream os = new FileOutputStream(new File(CONFIG_FILE))) {
			prop.store(os, null);
			
		} catch (IOException ex) {
			log.error("Cannot write config file", ex);
		}
	}
}
