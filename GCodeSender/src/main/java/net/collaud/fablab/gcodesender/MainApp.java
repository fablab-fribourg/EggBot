package net.collaud.fablab.gcodesender;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.collaud.fablab.gcodesender.controller.MainController;


public class MainApp extends Application {
	
	private static final SpringFxmlLoader loader = new SpringFxmlLoader();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = (Parent) loader.load("/fxml/mainScene.fxml");
		((MainController)loader.getController()).setStage(stage);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
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
