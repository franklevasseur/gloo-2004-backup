package gui.sidepanel;

import application.Controller;
import gui.ZoomManager;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class InspectorPanel {

    private TextField minInspectionLengthTextField;
    private Button inspectButton;
    private TextArea inspectionArea;
    private Double minInspectionLength;
    private ZoomManager zoomManager;
    private Controller domainController = Controller.getInstance();

    public InspectorPanel(TextField minInspectionLengthTextField,
                          Button inspectButton,
                          TextArea inspectionArea,
                          Double minInspectionLength,
                          ZoomManager zoomManager) {

        this.minInspectionLengthTextField = minInspectionLengthTextField;
        this.inspectButton = inspectButton;
        this.inspectionArea = inspectionArea;
        this.minInspectionLength = minInspectionLength;
        this.zoomManager = zoomManager;
        
        inspectionArea.setDisable(true);
        inspectionArea.setStyle("-fx-text-fill: #ff0000; -fx-opacity: 1.0;");
        inspectButton.setDisable(true);

        // TODO: arrange this
        InputBoxHelper parser = new InputBoxHelper(true, zoomManager);

        minInspectionLengthTextField.textProperty().addListener((observableValue, oldString, newString) -> {

            boolean parseSucess = true;
            try {
                CharSequence minInspectionLengthInput = this.minInspectionLengthTextField.getCharacters();
                this.minInspectionLength = parser.parseToMetric(minInspectionLengthInput.toString());
            } catch (ParseException e) {
                parseSucess = false;
            }

            this.inspectButton.setDisable(!parseSucess);
        });
    }

    public void inspect(boolean metricDisplay) {
        String unit = "m";
        double inspectValue = minInspectionLength;
        if (!metricDisplay) {
            inspectValue = zoomManager.inchToMeters(minInspectionLength);
            unit = "in";
        }
        String inspectionResult = domainController.inspectProject(inspectValue, inspectValue);
        inspectionArea.setText(String.format("Inspection result for min lenght = %.2f %s : \n\n%s", minInspectionLength, unit, inspectionResult));
    }
}
