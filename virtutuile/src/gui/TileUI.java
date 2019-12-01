package gui;

import application.MaterialDto;
import application.TileDto;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TileUI {

    private Shape shape;
    private Label tileInfoTextField;
    private ZoomManager zoomManager;
    private MaterialDto material;
    private boolean isCut;

    private List<Point> pixelSummits;

    public TileUI(TileDto dto, Label tileInfoTextField, ZoomManager zoomManager, SurfaceUI parentSurface) {
        this.tileInfoTextField = tileInfoTextField;
        this.zoomManager = zoomManager;
        this.material = dto.material;

        isCut = dto.isCut;
        pixelSummits = dto.summits.stream().map(s -> zoomManager.metersToPixels(s)).collect(Collectors.toList());

        if (dto.isCut) {
            List<Double> flattedSummits = pixelSummits.stream().flatMap(p -> Arrays.asList(p.x, p.y).stream()).collect(Collectors.toList());
            shape = new Polygon();
            ((Polygon) shape).getPoints().addAll(flattedSummits);
        } else {
            RectangleInfo rectInfo = RectangleHelper.summitsToRectangleInfo(pixelSummits);
            shape = new Rectangle(rectInfo.topLeftCorner.x, rectInfo.topLeftCorner.y, rectInfo.width, rectInfo.height);
        }

        if (material.color == utils.Color.BLACK) {
            shape.setFill(Color.BLACK);
        }else if(material.color == utils.Color.WHITE){
            shape.setFill(Color.WHITE);
        }else if(material.color == utils.Color.YELLOW){
            shape.setFill(Color.YELLOW);
        }else if(material.color == utils.Color.GREEN){
            shape.setFill(Color.GREEN);
        }else if(material.color == utils.Color.BLUE){
            shape.setFill(Color.BLUE);
        }else if(material.color == utils.Color.RED){
            shape.setFill(Color.RED);
        }else if(material.color == utils.Color.VIOLET){
            shape.setFill(Color.VIOLET);
        }

        shape.setOnMouseEntered(event -> select());
        shape.setOnMouseExited(event -> unselect());

        shape.setOnMouseClicked(e -> {
            AbstractShape thisFuckingTile = new AbstractShape(this.toDto().summits);
            AbstractShape thisFuckingParentSurface = new AbstractShape(parentSurface.toDto().summits);
            boolean isAllOut = ShapeHelper.isAllOutside(thisFuckingTile, thisFuckingParentSurface);
            System.out.println(String.format("tile is all outside : %b", isAllOut));
        });
    }

    public Node getNode() {
        return this.shape;
    }

    public TileDto toDto() {
        TileDto tile = new TileDto();
        tile.summits = pixelSummits.stream().map(s -> zoomManager.pixelsToMeters(s)).collect(Collectors.toList());
        tile.material = this.material;
        tile.isCut = this.isCut;
        return tile;
    }

    private void select() {
        shape.setFill(Color.PALEGOLDENROD);
        tileInfoTextField.setText(formatInfoString());
    }

    private void unselect() {
        if (material.color == utils.Color.BLACK) {
            shape.setFill(Color.BLACK);
        }else if(material.color == utils.Color.WHITE){
            shape.setFill(Color.WHITE);
        }else if(material.color == utils.Color.YELLOW){
            shape.setFill(Color.YELLOW);
        }else if(material.color == utils.Color.GREEN){
            shape.setFill(Color.GREEN);
        }else if(material.color == utils.Color.BLUE){
            shape.setFill(Color.BLUE);
        }else if(material.color == utils.Color.RED){
            shape.setFill(Color.RED);
        }else if(material.color == utils.Color.VIOLET){
            shape.setFill(Color.VIOLET);
        }
        tileInfoTextField.setText("");
    }

    private String formatInfoString() {
        AbstractShape shape = new AbstractShape(pixelSummits);

        double width = this.zoomManager.pixelsToMeters(ShapeHelper.getWidth(shape));
        double height = this.zoomManager.pixelsToMeters(ShapeHelper.getHeight(shape));

        Point topLeft = ShapeHelper.getTopLeftCorner(shape);
        double x = this.zoomManager.pixelsToMeters(topLeft.x);
        double y = this.zoomManager.pixelsToMeters(topLeft.y);
        return String.format("width: %.1f, height: %.1f, x: %.1f, y: %.1f", width, height, x, y);
    }
}
