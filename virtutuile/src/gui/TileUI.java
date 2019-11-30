package gui;

import application.MaterialDto;
import application.TileDto;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.List;
import java.util.stream.Collectors;

public class TileUI {

    private Rectangle rectangle;
    private Label tileInfoTextField;
    private ZoomManager zoomManager;
    private MaterialDto material;

    public TileUI(RectangleInfo rectInfo, Label tileInfoTextField, ZoomManager zoomManager, MaterialDto material) {
        this.tileInfoTextField = tileInfoTextField;
        this.zoomManager = zoomManager;
        this.material = material;

        rectangle = new Rectangle(rectInfo.topLeftCorner.x, rectInfo.topLeftCorner.y, rectInfo.width, rectInfo.height);

        if (material.color == utils.Color.BLACK) {
            rectangle.setFill(Color.BLACK);
        }else if(material.color == utils.Color.WHITE){
            rectangle.setFill(Color.WHITE);
        }else if(material.color == utils.Color.YELLOW){
            rectangle.setFill(Color.YELLOW);
        }else if(material.color == utils.Color.GREEN){
            rectangle.setFill(Color.GREEN);
        }else if(material.color == utils.Color.BLUE){
            rectangle.setFill(Color.BLUE);
        }else if(material.color == utils.Color.RED){
            rectangle.setFill(Color.RED);
        }else if(material.color == utils.Color.VIOLET){
            rectangle.setFill(Color.VIOLET);
        }

        rectangle.setOnMouseEntered(event -> select());
        rectangle.setOnMouseExited(event -> unselect());
    }

    public Node getNode() {
        return this.rectangle;
    }

    public TileDto toDto() {
        TileDto tile = new TileDto();
        List<Point> pixelSummits = RectangleHelper.rectangleInfoToSummits(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        tile.summits = pixelSummits.stream().map(s -> zoomManager.pixelsToMeters(s)).collect(Collectors.toList());
        tile.material = this.material;
        return tile;
    }

    private void select() {
        rectangle.setFill(Color.PALEGOLDENROD);
        tileInfoTextField.setText(formatInfoString());
    }

    private void unselect() {
        if (material.color == utils.Color.BLACK) {
            rectangle.setFill(Color.BLACK);
        }else if(material.color == utils.Color.WHITE){
            rectangle.setFill(Color.WHITE);
        }else if(material.color == utils.Color.YELLOW){
            rectangle.setFill(Color.YELLOW);
        }else if(material.color == utils.Color.GREEN){
            rectangle.setFill(Color.GREEN);
        }else if(material.color == utils.Color.BLUE){
            rectangle.setFill(Color.BLUE);
        }else if(material.color == utils.Color.RED){
            rectangle.setFill(Color.RED);
        }else if(material.color == utils.Color.VIOLET){
            rectangle.setFill(Color.VIOLET);
        }
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
