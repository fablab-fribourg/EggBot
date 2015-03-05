package net.collaud.fablab.gcodesender.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.gcode.GcodeNotifyMessage;
import net.collaud.fablab.gcodesender.gcode.GcodeService;
import net.collaud.fablab.gcodesender.serial.PortStatus;
import static net.collaud.fablab.gcodesender.serial.PortStatus.ERROR;
import static net.collaud.fablab.gcodesender.serial.PortStatus.NOT_RESPONDING;
import static net.collaud.fablab.gcodesender.serial.PortStatus.OPEN;
import net.collaud.fablab.gcodesender.serial.SerialPortDefinition;
import net.collaud.fablab.gcodesender.serial.SerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

//FIXME persit lastDirectory and port
@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
	private Button buttonReloadPort;

	@FXML
	private Button buttonOpenPort;

	@FXML
	private Button buttonClosePort;

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

	@FXML
	private TitledPane panePort;

	@FXML
	private TitledPane paneFile;

	@FXML
	private TitledPane panePrint;

	@FXML
	private TitledPane controlPane;

	private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
	private final List<String> logLines = new LinkedList<>();
	private ObjectProperty<SerialPortDefinition> selectedPort;
	private final BooleanProperty printing = new SimpleBooleanProperty(false);
	private File lastDirectory;

	@FXML
	private void reloadPorts() {
		comboPort.setItems(FXCollections.observableArrayList(serialService.getListPorts()));
		selectedPort.setValue(null);
	}

	@FXML
	private void openPort() {
		serialService.openPort(comboPort.getValue());
	}

	@FXML
	private void closePort() {
		serialService.closePort();
	}

	@FXML
	private void chooseGCodeFile() {
		FileChooser fileChooser = new FileChooser();
		if (lastDirectory != null) {
			fileChooser.setInitialDirectory(lastDirectory);
		}
		fileChooser.setTitle("Open GCode file");
		addGCodeExtensionFilter(fileChooser);
		File f = fileChooser.showOpenDialog(stage);
		if (f != null) {
			lastDirectory = f.getParentFile();
			selectedFile.setValue(f);
			log.info("File selected : " + selectedFile.getValue());
		}
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
		printing.setValue(false);
		gcodeService.stopPrint();
	}

	@FXML
	private void linkProject() {
		goToLink("https://github.com/fablab-fribourg/EggBot");
	}

	@FXML
	private void linkGaetan() {
		goToLink("http://collaud.net");
	}

	@FXML
	private void linkFablab() {
		goToLink("http://fablab-fribourg.ch/");
	}

	private void goToLink(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException ex) {
			log.error("Cannot open brower for link " + url, ex);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//pane port
		final ObjectProperty<PortStatus> portStatus = serialService.getPortStatus();
		selectedPort = comboPort.valueProperty();
		panePort.disableProperty().bind(printing);
		comboPort.disableProperty().bind(portStatus.isNotEqualTo(PortStatus.CLOSED));
		buttonOpenPort.disableProperty().bind(selectedPort.isNull().or(portStatus.isNotEqualTo(PortStatus.CLOSED)));
		buttonClosePort.disableProperty().bind(portStatus.isNotEqualTo(PortStatus.OPEN));
		buttonReloadPort.disableProperty().bind(portStatus.isNotEqualTo(PortStatus.CLOSED));
		labelPortStatus.textProperty().bind(portStatus.asString());
		portStatus.addListener((ObservableValue<? extends PortStatus> observable, PortStatus oldValue, PortStatus newValue) -> {
			labelPortStatus.setTooltip(new Tooltip(newValue.getDetail()));
			switch (newValue) {
				case OPEN:
					labelPortStatus.setTextFill(Color.GREEN);
					break;
				case ERROR:
				case NOT_RESPONDING:
					labelPortStatus.setTextFill(Color.RED);
					break;
				default:
					labelPortStatus.setTextFill(Color.BLACK);
			}
		});

		//pane file
		paneFile.disableProperty().bind(printing);
		selectedFile.addListener((ObservableValue<? extends File> observable, File oldValue, File newValue) -> {
			labelFile.setText(Optional.ofNullable(newValue).map(f -> f.getName()).orElse("No file selected yet !"));
		});

		//Pane pring
		panePrint.disableProperty().bind(selectedFile.isNull().or(portStatus.isNotEqualTo(PortStatus.OPEN)));
		buttonPrintStart.disableProperty().bind(printing);
		buttonPrintStop.disableProperty().bind(printing.not());

		gcodeService.addObserver((GcodeNotifyMessage msg) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("<div style=\"color:");
			sb.append(msg.getType() == GcodeNotifyMessage.Type.ERROR ? "red" : "black");
			sb.append(";\">");
			sb.append(msg.getType().toString());
			sb.append(" ");
			sb.append(msg.getMessage());
			sb.append("</div>");
			logLines.add(sb.toString());
			if (logLines.size() > 1000) {
				logLines.remove(0);
			}
			Platform.runLater(() -> {
				updateLog();
				if (msg.isEndOfPrint()) {
					printing.set(false);
				}
			});
		});

		//Pane control
		controlPane.disableProperty().bind(printing.or(portStatus.isNotEqualTo(PortStatus.OPEN)));

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

}
