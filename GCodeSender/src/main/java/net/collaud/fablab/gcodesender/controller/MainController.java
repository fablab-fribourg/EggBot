package net.collaud.fablab.gcodesender.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import net.collaud.fablab.gcodesender.gcode.NotifyMessage;
import net.collaud.fablab.gcodesender.serial.PortStatus;
import net.collaud.fablab.gcodesender.serial.SerialPortDefinition;
import net.collaud.fablab.gcodesender.serial.SerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class MainController implements Initializable {
	
	@Autowired
	private SerialService serialService;
	
	@Autowired
	private GcodeService gcodeService;
	
	@Setter
	private Stage stage;
	
	@FXML
	private Label labelFile;
	
	@FXML
	private Button buttonOpenPort;
	
	@FXML
	private Label labelPortStatus;
	
	@FXML
	private Button buttonPrintStart;
	
	@FXML
	private Button buttonPrintStop;
	
	@FXML
	private ComboBox<SerialPortDefinition> comboPort;
	
	@FXML
	private WebView htmlLog;
	
	private ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
	private final List<String> logLines = new ArrayList<>();
	private ObjectProperty<SerialPortDefinition> selectedPort;
	private final BooleanProperty printing = new SimpleBooleanProperty(false);
	
	@FXML
	private void reloadPorts() {
		comboPort.setItems(FXCollections.observableArrayList(serialService.getListPorts()));
		selectedPort.setValue(null);
		bindings();
	}
	
	@FXML
	private void openPort(){
		serialService.openPort(comboPort.getValue());
	}
	
	@FXML
	private void chooseGCodeFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GCode file");
		addGCodeExtensionFilter(fileChooser);
		selectedFile.setValue(fileChooser.showOpenDialog(stage));
		log.info("File selected : " + selectedFile.getValue());
		bindings();
	}
	
	private void addGCodeExtensionFilter(FileChooser fileChooser) {
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Gcode file ", "*.gcode");
		fileChooser.getExtensionFilters().add(extFilter);
	}
	
	@FXML
	private void print() {
		log.info("Print file {} on port {}", selectedFile.get().getAbsolutePath(), selectedPort.get());
		gcodeService.print(selectedFile.get(), selectedPort.get());
		logLines.clear();
		printing.set(true);
	}
	
	@FXML
	private void stopPrint() {
		log.info("Stop print");
		gcodeService.stopPrint();
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		selectedPort = comboPort.valueProperty();
		buttonPrintStart.disableProperty().bind(selectedFile.isNull().or(selectedPort.isNull()).or(printing));
		buttonOpenPort.disableProperty().bind(selectedPort.isNull());
		buttonPrintStop.disableProperty().bind(printing.not());
		
		serialService.addObserver((PortStatus status) -> {
			labelPortStatus.setText(status.toString());
		});
		
		gcodeService.addObserver((NotifyMessage msg) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("<div style=\"color:");
			sb.append(msg.getType() == NotifyMessage.Type.ERROR ? "red" : "black");
			sb.append(";\">");
			sb.append(msg.getType().toString());
			sb.append(" ");
			sb.append(msg.getMessage());
			sb.append("</div>");
			logLines.add(sb.toString());
			Platform.runLater(() -> updateLog());
		});
		
		reloadPorts();

		//Fixme test
//		selectedFile = Optional.of(new File("C:\\Users\\gaetan\\Documents\\output_0014.gcode"));
//		selectedPort = Optional.of(new SerialPortDefinition("COM8"));
//		updateButtonPrint();
	}
	
	private void updateLog() {
		StringBuilder html = new StringBuilder().append("<html>");
		html.append("<head>");
		html.append("   <script language=\"javascript\" type=\"text/javascript\">");
		html.append("       function toBottom(){");
		html.append("           window.scrollTo(0, document.body.scrollHeight);");
		html.append("       }");
		html.append("   </script>");
		html.append("</head>");
		html.append("<body onload='toBottom()'>");
		
		logLines.forEach(line -> html.append(line));
		html.append("<div>&nbsp;</div>");
		
		html.append("</body></html>");
		
		htmlLog.getEngine().loadContent(html.toString());
	}
	
	public void bindings() {
//		buttonPrintStart.setDisable(selectedFile.get() != null || selectedPort.get() != null);
		labelFile.setText(selectedFile.get() != null ? selectedFile.get().getAbsolutePath() : "No file selected yet");
	}
	
}
