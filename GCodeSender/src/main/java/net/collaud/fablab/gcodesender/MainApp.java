package net.collaud.fablab.gcodesender;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.collaud.fablab.gcodesender.controller.MainController;

public class MainApp extends Application {

	private static final SpringFxmlLoader loader = new SpringFxmlLoader();

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = (Parent) loader.load("/mainScene.fxml");
		((MainController) loader.getController()).setStage(stage);

		Scene scene = new Scene(root);
		scene.getStylesheets().add("/styles/Styles.css");

		stage.setOnCloseRequest((WindowEvent t) -> {
			//FIXME stop gcode
			//FIXME close serial
			Platform.exit();
			System.exit(0);
		});

		stage.setTitle("Fablab-Fribourg Eggbot GcodeSender");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be
	 * launched through deployment artifacts, e.g., in IDEs with limited FX
	 * support. NetBeans ignores main().
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
