package net.collaud.fablab.gcodesender.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
	private Button buttonPrint;

	@FXML
	private ComboBox<SerialPortDefinition> comboPort;

	@FXML
	private WebView htmlLog;

	private Optional<SerialPortDefinition> selectedPort = Optional.empty();
	private Optional<File> selectedFile = Optional.empty();
	private List<String> logLines = new ArrayList<>();

	@FXML
	private void reloadPorts() {
		comboPort.setItems(FXCollections.observableArrayList(serialService.getListPorts()));
		selectedPort = Optional.empty();
		updateButtonPrint();
	}

	@FXML
	private void chooseGCodeFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GCode file");
		addGCodeExtensionFilter(fileChooser);
		selectedFile = Optional.ofNullable(fileChooser.showOpenDialog(stage));
		selectedFile.ifPresent(f -> labelFile.setText(f.getAbsolutePath()));
		log.info("File selected : " + selectedFile.orElse(null));
		updateButtonPrint();
	}

	private void addGCodeExtensionFilter(FileChooser fileChooser) {
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Gcode file ", "*.gcode");
		fileChooser.getExtensionFilters().add(extFilter);
	}

	@FXML
	private void print() {
		log.info("Print file {} on port {}", selectedFile.get().getAbsolutePath(), selectedPort.get());
		gcodeService.sendFile(selectedFile.get(), selectedPort.get());
		logLines.clear();
	}
	
	public void updateButtonPrint() {
		buttonPrint.setDisable(!selectedFile.isPresent() || !selectedPort.isPresent());
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		reloadPorts();
		comboPort.setOnAction(event -> {
			selectedPort = Optional.ofNullable(comboPort.getSelectionModel().getSelectedItem());
			log.info("Port selected : " + selectedPort.orElse(null));
			updateButtonPrint();
		});
		gcodeService.addObserver((Observable o, Object arg) -> {
			NotifyMessage msg = (NotifyMessage) arg;
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

		logLines.forEach(line ->html.append(line));
		html.append("<div>&nbsp;</div>");

		html.append("</body></html>");

		htmlLog.getEngine().loadContent(html.toString());
	}
	
}
