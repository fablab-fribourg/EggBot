package net.collaud.fablab.gcodesender;

import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Slf4j
public class SpringFxmlLoader {

	private static final ApplicationContext applicationContext = new AnnotationConfigApplicationContext("net.collaud.fablab.gcodesender");
	
	private FXMLLoader lastLoader;

	public Object load(String url) {
		try (InputStream fxmlStream = SpringFxmlLoader.class.getResourceAsStream(url)) {
			log.info("URL : " + SpringFxmlLoader.class.getResourceAsStream(url));
			lastLoader = new FXMLLoader(SpringFxmlLoader.class.getResource("/fxml/"));
			lastLoader.setControllerFactory((Class<?> clazz) -> applicationContext.getBean(clazz));
			return lastLoader.load(fxmlStream);
		} catch (IOException ioException) {
			log.error("Cannot load file " + url);
			throw new RuntimeException(ioException);
		}
	}
	
	public Object getController(){
		return lastLoader.getController();
	}
}
