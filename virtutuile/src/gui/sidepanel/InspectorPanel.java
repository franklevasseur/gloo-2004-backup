package gui.sidepanel;

import application.Controller;
import gui.ZoomManager;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.text.ParseException;
import java.util.function.Function;

public class InspectorPanel {

    private TextField minInspectionLengthTextField;
    private Button inspectButton;
    private TextArea inspectionArea;

    private ZoomManager zoomManager;
    private Controller domainController = Controller.getInstance();

    private boolean metricDisplay = true;

    public InspectorPanel(TextField minInspectionLengthTextField,
                          Button inspectButton,
                          TextArea inspectionArea,
                          ZoomManager zoomManager) {

        this.minInspectionLengthTextField = minInspectionLengthTextField;
        this.inspectButton = inspectButton;
        this.inspectionArea = inspectionArea;
        this.zoomManager = zoomManager;
        
        inspectionArea.setDisable(true);
        inspectionArea.setStyle("-fx-text-fill: #ff0000; -fx-opacity: 1.0;");
        inspectButton.setDisable(true);

        minInspectionLengthTextField.textProperty().addListener((observableValue, oldString, newString) -> {

            this.parse(this.minInspectionLengthTextField.getText(), sucess -> {
                this.inspectButton.setDisable(!sucess);
                return null;
            });
        });
    }

    public void setMetric(boolean metricDisplay) {
        this.metricDisplay = metricDisplay;
    }

    public void inspect(boolean metricDisplay) {
        this.metricDisplay = metricDisplay;

        String unit = "m";
        double meters = this.parse(this.minInspectionLengthTextField.getText(), null);

        double minInspectionLength = meters;
        if (!metricDisplay) {
            minInspectionLength = zoomManager.metersToInch(meters);
            unit = "in";
        }
        String inspectionResult = domainController.inspectProject(meters, meters);
        inspectionArea.setText(String.format("Inspection result for min lenght = %.2f %s : \n\n%s", minInspectionLength, unit, inspectionResult));
    }

    private Double parse(String inputText, Function<Boolean, Void> callback) {
        boolean parseSucess = true;

        InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);
        Double meters = 0.0;

        try {
            meters = parser.parseToMetric(inputText);
        } catch (ParseException e) {
            parseSucess = false;
        }

        if (callback != null) {
            callback.apply(parseSucess);
        }
        return meters;
    }
}
