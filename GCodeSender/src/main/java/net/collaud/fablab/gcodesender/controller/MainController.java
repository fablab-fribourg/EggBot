package net.collaud.fablab.gcodesender.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.collaud.fablab.gcodesender.config.Config;
import net.collaud.fablab.gcodesender.config.ConfigKey;
import net.collaud.fablab.gcodesender.gcode.GcodeFileService;
import net.collaud.fablab.gcodesender.gcode.GcodeFileStatus;
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
	private GcodeFileService gcodeFileService;

	@Autowired
	private GcodeService gcodeService;

	@Autowired
	private Config config;

	@Setter
	private Stage stage;

	@FXML
	private Label labelFile;

	@FXML
	private Label labelFileStatus;

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

	@FXML
	private HBox manualGCodePane;

	@FXML
	private ControlController controlPaneController;

	@FXML
	private ScalePanelController scalePaneController;

	private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
	private final List<String> logLines = new LinkedList<>();
	private ObjectProperty<SerialPortDefinition> selectedPort;
	private final BooleanProperty printing = new SimpleBooleanProperty(false);
	private File lastDirectory;

	@FXML
	private void reloadPorts() {
		final List<SerialPortDefinition> list = serialService.getListPorts();
		comboPort.setItems(FXCollections.observableArrayList(list));
		Optional<String> last = Optional.ofNullable(config.getProperty(ConfigKey.LAST_PORT));
		SerialPortDefinition value = null;
		if (last.isPresent()) {
			final Optional<SerialPortDefinition> find = list.stream()
					.filter(e -> e.getName().equals(last.get()))
					.findFirst();
			if (find.isPresent()) {
				value = find.get();
			}
		}
		selectedPort.setValue(value);
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
		if (lastDirectory != null && lastDirectory.exists()) {
			fileChooser.setInitialDirectory(lastDirectory);
		}
		fileChooser.setTitle("Open GCode file");
		addGCodeExtensionFilter(fileChooser);
		File f = fileChooser.showOpenDialog(stage);
		selectedFile.setValue(f);
		gcodeFileService.analyseFile(f);
	}

	private void addGCodeExtensionFilter(FileChooser fileChooser) {
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gcode file", 
				Arrays.asList(new String[]{"*.gcode", "*.ngc", "*.nc", "*.tap"})));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All ", "*.*"));
	}

	@FXML
	private void print() {
		log.info("Print file {} on port {}", selectedFile.get().getAbsolutePath(), selectedPort.get());
		gcodeService.print(scalePaneController.getScaleValue().doubleValue());
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
		final BooleanBinding condOpenPort = portStatus.isEqualTo(PortStatus.OPEN)
				.or(portStatus.isEqualTo(PortStatus.OPENNING))
				.or(portStatus.isEqualTo(PortStatus.WAITING_FOR_ARDUINO))
				.or(portStatus.isEqualTo(PortStatus.CLOSING));
		selectedPort = comboPort.valueProperty();
		panePort.disableProperty().bind(printing);
		comboPort.disableProperty().bind(condOpenPort);
		buttonOpenPort.disableProperty().bind(selectedPort.isNull().or(condOpenPort));
		buttonClosePort.disableProperty().bind(portStatus.isNotEqualTo(PortStatus.OPEN));
		buttonReloadPort.disableProperty().bind(condOpenPort);
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

		selectedPort.addListener((ObservableValue<? extends SerialPortDefinition> observable, SerialPortDefinition oldValue, SerialPortDefinition newValue) -> {
			config.setProperty(ConfigKey.LAST_PORT, newValue != null ? newValue.getName() : null);
		});

		//pane file
		paneFile.disableProperty().bind(printing);
		final ObjectProperty<GcodeFileStatus> fileStatus = gcodeFileService.getFileStatus();
		labelFileStatus.textProperty().bind(fileStatus.asString());
		fileStatus.addListener((ObservableValue<? extends GcodeFileStatus> observable, GcodeFileStatus oldValue, GcodeFileStatus newValue) -> {
			switch (newValue) {
				case COMPLETE:
					labelFileStatus.setTextFill(Color.GREEN);
					break;
				case ERROR:
					labelFileStatus.setTextFill(Color.RED);
					break;
				case READING:
					labelFileStatus.setTextFill(Color.ORANGE);
					break;
				default:
					labelFileStatus.setTextFill(Color.BLACK);
			}
		});
		selectedFile.addListener((ObservableValue<? extends File> observable, File oldValue, File newValue) -> {
			labelFile.setText(Optional.ofNullable(newValue).map(f -> f.getName()).orElse(""));
			config.setProperty(ConfigKey.LAST_FILE, newValue != null ? newValue.getAbsolutePath() : null);
			if (newValue != null) {
				lastDirectory = newValue.getParentFile();
				log.info("File selected : " + selectedFile.getValue());
			}
		});

		//Pane pring
		panePrint.disableProperty().bind(fileStatus.isNotEqualTo(GcodeFileStatus.COMPLETE).or(portStatus.isNotEqualTo(PortStatus.OPEN)));
		buttonPrintStart.disableProperty().bind(printing);
		buttonPrintStop.disableProperty().bind(printing.not());

		gcodeService.addObserver((GcodeNotifyMessage msg) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("<div style=\"color:");
			sb.append(msg.getType() == GcodeNotifyMessage.Type.ERROR ? "red" : "black");
			sb.append(";\">");
//			sb.append(msg.getType().toString());
//			sb.append(" ");
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

		//Pane scale
		scalePaneController.init(controlPaneController.getLimits(), gcodeFileService.getLimits());

		//Pane manual gcode
		manualGCodePane.disableProperty().bind(printing.or(portStatus.isNotEqualTo(PortStatus.OPEN)));

		//Init values
		reloadPorts();
		config.getOptionalProperty(ConfigKey.LAST_FILE).ifPresent(p -> {
			File f = new File(p);
			selectedFile.set(f);
			gcodeFileService.analyseFile(f);
		});

	}

	private synchronized void updateLog() {
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
