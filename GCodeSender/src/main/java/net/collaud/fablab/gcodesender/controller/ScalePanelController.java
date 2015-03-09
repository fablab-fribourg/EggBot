package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.controller.custom.CustomField;
import net.collaud.fablab.gcodesender.controller.model.Limits;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author Gaetan Collaud <gaetancollaud@gmail.com>
 */
@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScalePanelController implements Initializable {

	@FXML
	private Label xMinUser;

	@FXML
	private Label xMaxUser;

	@FXML
	private Label yMinUser;

	@FXML
	private Label yMaxUser;

	@FXML
	private Label xMinFile;

	@FXML
	private Label xMaxFile;

	@FXML
	private Label yMinFile;

	@FXML
	private Label yMaxFile;

	@FXML
	private Label xMinScale;

	@FXML
	private Label xMaxScale;

	@FXML
	private Label yMinScale;

	@FXML
	private Label yMaxScale;

	@FXML
	private TextField textScale;

	@Getter
	private final DoubleProperty scalevalue = new SimpleDoubleProperty();

	@Getter
	private final ObjectProperty<Limits> limitUser = new SimpleObjectProperty<>();
	
	@Getter
	private final ObjectProperty<Limits> limitFile = new SimpleObjectProperty<>();
	
	@Getter
	private final ObjectProperty<Limits> limitScale = new SimpleObjectProperty<>();

	public void init(DoubleProperty xMin, DoubleProperty xMax, DoubleProperty yMin, DoubleProperty yMax) {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		CustomField.numberField(scalevalue, textScale);
	}

}
