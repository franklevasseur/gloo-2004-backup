package gui.sidepanel;

import gui.ZoomManager;
import javafx.scene.control.Label;
import utils.RectangleInfo;

public class TileInfoUI {

    private Label tileInfoTextField;
    private ZoomManager zoomManager;
    private boolean metricDisplay = true;
    private RectangleInfo tileInfo;

    public TileInfoUI(Label tileInfoTextField, ZoomManager zoomManager) {
        this.tileInfoTextField = tileInfoTextField;
        this.zoomManager = zoomManager;
    }

    public void setMetric(boolean metricDisplay) {
        this.metricDisplay = metricDisplay;
        if (tileInfo != null) {
            setNewTileInfo(tileInfo, metricDisplay);
        }
    }

    public void setNewTileInfo(RectangleInfo tileInfo) {
        this.tileInfo = tileInfo;
        setNewTileInfo(tileInfo, metricDisplay);
    }

    private void setNewTileInfo(RectangleInfo tileInfo, boolean metricDisplay) {

        InputBoxHelper formater = new InputBoxHelper(metricDisplay, zoomManager);
        String width = formater.formatMetric(tileInfo.width);
        String height = formater.formatMetric(tileInfo.height);
        String x = formater.formatMetric(tileInfo.topLeftCorner.x);
        String y = formater.formatMetric(tileInfo.topLeftCorner.y);

        String formatedString = String.format("width: %s, height: %s, x: %s, y: %s", width, height, x, y);
        tileInfoTextField.setText(formatedString);
    }

    public void hide() {
        tileInfoTextField.setText("");
    }
}
