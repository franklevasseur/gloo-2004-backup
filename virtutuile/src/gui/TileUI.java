package gui;

import application.TileDto;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.RectangleHelper;
import utils.RectangleInfo;

public class TileUI {

    private Rectangle rectangle;
    private Label tileInfoTextField;
    private ZoomManager zoomManager;

    public TileUI(RectangleInfo rectInfo, Label tileInfoTextField, ZoomManager zoomManager) {
        this.tileInfoTextField = tileInfoTextField;
        this.zoomManager = zoomManager;

        rectangle = new Rectangle(rectInfo.topLeftCorner.x, rectInfo.topLeftCorner.y, rectInfo.width, rectInfo.height);
        rectangle.setFill(Color.PALETURQUOISE);
        rectangle.setStroke(Color.DARKTURQUOISE);

        rectangle.setOnMouseEntered(event -> select());
        rectangle.setOnMouseExited(event -> unselect());
    }

    public Node getNode() {
        return this.rectangle;
    }

    public TileDto toDto() {
        TileDto tile = new TileDto();
        tile.summits = RectangleHelper.rectangleInfoToSummits(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        return tile;
    }

    private void select() {
        rectangle.setFill(Color.PALEGOLDENROD);
        tileInfoTextField.setText(formatInfoString());
    }

    private void unselect() {
        rectangle.setFill(Color.PALETURQUOISE);
        tileInfoTextField.setText("");
    }

    private String formatInfoString() {
        double width = this.zoomManager.pixelsToMeters(this.rectangle.getWidth());
        double height = this.zoomManager.pixelsToMeters(this.rectangle.getHeight());
        double x = this.zoomManager.pixelsToMeters(this.rectangle.getX());
        double y = this.zoomManager.pixelsToMeters(this.rectangle.getY());
        return String.format("width: %.1f, height: %.1f, x: %.1f, y: %.1f", width, height, x, y);
    }
}
