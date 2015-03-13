package net.collaud.fablab.gcodesender.controller;

import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.controller.custom.CustomField;
import net.collaud.fablab.gcodesender.controller.model.LimitsProperty;
import net.collaud.fablab.gcodesender.util.DoubleParserUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 *
 * @author Gaetan Collaud
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
	
	@FXML
	private Button buttonAutoScale;

	@Getter
	private final DoubleProperty scaleValue = new SimpleDoubleProperty(0);
	
	private LimitsProperty limitsUser;
	private LimitsProperty limitsFile;

	public void init(final LimitsProperty user, final LimitsProperty file) {
		limitsUser = user;
		limitsFile = file;
		
		linkLabelToDoubleProperty(xMinUser, user.getXMin());
		linkLabelToDoubleProperty(xMaxUser, user.getXMax());
		linkLabelToDoubleProperty(yMinUser, user.getYMin());
		linkLabelToDoubleProperty(yMaxUser, user.getYMax());
		linkLabelToDoubleProperty(xMinFile, file.getXMin());
		linkLabelToDoubleProperty(xMaxFile, file.getXMax());
		linkLabelToDoubleProperty(yMinFile, file.getYMin());
		linkLabelToDoubleProperty(yMaxFile, file.getYMax());
		
		LimitsProperty scale = new LimitsProperty();
		linkLabelToDoubleProperty(xMinScale, scale.getXMin());
		linkLabelToDoubleProperty(xMaxScale, scale.getXMax());
		linkLabelToDoubleProperty(yMinScale, scale.getYMin());
		linkLabelToDoubleProperty(yMaxScale, scale.getYMax());

		ChangeListener<Number> scaleChangeList = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				scale.getXMin().set(scaleValue.get() * file.getXMin().get());
				scale.getXMax().set(scaleValue.get() * file.getXMax().get());
				scale.getYMin().set(scaleValue.get() * file.getYMin().get());
				scale.getYMax().set(scaleValue.get() * file.getYMax().get());
			}
		};
		
		xMinScale.textFillProperty().bind(Bindings.when(user.getXMin().greaterThan(scale.getXMin())).then(Color.RED).otherwise(Color.GREEN));
		xMaxScale.textFillProperty().bind(Bindings.when(user.getXMax().lessThan(scale.getXMax())).then(Color.RED).otherwise(Color.GREEN));
		yMinScale.textFillProperty().bind(Bindings.when(user.getYMin().greaterThan(scale.getYMin())).then(Color.RED).otherwise(Color.GREEN));
		yMaxScale.textFillProperty().bind(Bindings.when(user.getYMax().lessThan(scale.getYMax())).then(Color.RED).otherwise(Color.GREEN));
				
		
		scaleValue.addListener(scaleChangeList);
		file.addListener(scaleChangeList);

		scaleValue.setValue(1.0);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		CustomField.numberField(scaleValue, textScale);
	}
	
	@FXML
	private void resetScale(){
		scaleValue.set(1.0);
	}
	
	@FXML
	private void autoScale(){
		List<Map.Entry<Double, Double>> list = new ArrayList<>();
		list.add(new AbstractMap.SimpleEntry(limitsUser.getXMin().get(), limitsFile.getXMin().get()));
		list.add(new AbstractMap.SimpleEntry(limitsUser.getXMax().get(), limitsFile.getXMax().get()));
		list.add(new AbstractMap.SimpleEntry(limitsUser.getYMin().get(), limitsFile.getYMin().get()));
		list.add(new AbstractMap.SimpleEntry(limitsUser.getYMax().get(), limitsFile.getYMax().get()));
		
		double min = Double.MAX_VALUE;
		for(Map.Entry<Double, Double> entry : list){
			double s = entry.getKey()/entry.getValue();
			if(s<min){
				min = s;
			}
		}
		scaleValue.set(min);
	}
	
	private void linkLabelToDoubleProperty(Label label, DoubleProperty prop) {
		prop.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			setLabelDouble(label, newValue.doubleValue());
		});
		setLabelDouble(label, prop.get());
	}

	private void setLabelDouble(Label label, Double value) {
		if (value == null || value.isNaN()) {
			label.setText("-");
		} else {
			label.setText(DoubleParserUtil.format(value));
		}
	}

}
