package gui.sidepanel;

import gui.SnapGridUI;
import gui.ZoomManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.text.ParseException;

public class SnapGridPanel {

    private TextField resizeSG;
    private Label snapgridLabel;
    private Button snapGridbutton;
    private SnapGridUI snapGrid;
    private ZoomManager zoomManager;

    public SnapGridPanel(TextField resizeSG,
                         Label snapgridLabel,
                         Button snapGridbutton,
                         SnapGridUI snapGrid,
                         ZoomManager zoomManager) {
        this.resizeSG = resizeSG;
        this.snapgridLabel = snapgridLabel;
        this.snapGridbutton = snapGridbutton;
        this.snapGrid = snapGrid;
        this.zoomManager = zoomManager;
    }

    public void snapGridApply(boolean metricDisplay) {
        try {

            InputBoxHelper helper = new InputBoxHelper(metricDisplay, zoomManager);

            CharSequence newSnapgridSize = this.resizeSG.getCharacters();
            Double snapGridSize = helper.parseToMetric(newSnapgridSize.toString());

            snapGrid.setSnapGridGap(zoomManager.metersToPixels(snapGridSize));
        } catch (ParseException ignored) {
            showSnapgridInfo(metricDisplay);
        }
    }

    public void showSnapgridInfo(boolean metricDisplay) {

        InputBoxHelper formater = new InputBoxHelper(metricDisplay, zoomManager);

        this.resizeSG.setVisible(true);
        this.snapgridLabel.setVisible(true);
        this.snapGridbutton.setVisible(true);

        double meters = zoomManager.pixelsToMeters(snapGrid.getSnapGripGap());
        resizeSG.setText(formater.formatMetric(meters));
    }

    public void hideSnapgridInfo() {
        this.resizeSG.setVisible(false);
        this.snapgridLabel.setVisible(false);
        this.snapGridbutton.setVisible(false);
    }
}
