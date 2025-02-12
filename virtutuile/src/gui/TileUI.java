package gui;

import application.MaterialDto;
import application.TileDto;
import gui.sidepanel.TileInfoUI;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import utils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TileUI {

    private Polygon shape;
    private TileInfoUI tileInfoTextField;
    private ZoomManager zoomManager;
    private MaterialDto material;
    private boolean isMasterTile;

    private boolean highligthIfMasterTile;

    private List<Point> pixelSummits;

    public TileUI(TileDto dto, TileInfoUI tileInfoTextField, ZoomManager zoomManager, SurfaceUI parentSurface) {
        this(dto, tileInfoTextField, zoomManager, parentSurface, false);
    }

    public TileUI(TileDto dto, TileInfoUI tileInfoTextField, ZoomManager zoomManager, SurfaceUI parentSurface, boolean highligthIfMasterTile) {
        this.highligthIfMasterTile = highligthIfMasterTile;

        this.tileInfoTextField = tileInfoTextField;
        this.zoomManager = zoomManager;
        this.material = dto.material;

        this.isMasterTile = dto.isMasterTile;

        pixelSummits = dto.summits.stream().map(s -> zoomManager.metersToPixels(s)).collect(Collectors.toList());

        List<Double> flattedSummits = pixelSummits.stream().flatMap(p -> Arrays.asList(p.x, p.y).stream()).collect(Collectors.toList());
        shape = new Polygon();
        shape.getPoints().addAll(flattedSummits);

        updateColor(false);
        shape.setStroke(Color.TRANSPARENT);

        shape.setOnMouseEntered(event -> select());
        shape.setOnMouseExited(event -> unselect());

//        shape.setOnMouseClicked(e -> {
//            Controller.getInstance().debugTileCutting(parentSurface.toDto(), this.toDto());
//        });
    }

    public Node getNode() {
        return this.shape;
    }

    public TileDto toDto() {
        TileDto tile = new TileDto();
        tile.summits = pixelSummits.stream().map(s -> zoomManager.pixelsToMeters(s)).collect(Collectors.toList());
        tile.material = this.material;
        tile.isMasterTile = this.isMasterTile;
        return tile;
    }

    private void updateColor(boolean isSelected) {
        if (isSelected) {
            shape.setFill(Color.PALEGOLDENROD);
        } else if (highligthIfMasterTile && isMasterTile) {
            shape.setFill(Color.CORNFLOWERBLUE);
        } else {
            shape.setFill(ColorHelper.utilsColorToJavafx(material.color));
        }
    }

    public void setHighligthIfMasterTile(boolean highligthIfMasterTile) {
        this.highligthIfMasterTile = highligthIfMasterTile;
        updateColor(false);
    }

    private void select() {
        updateColor(true);
        tileInfoTextField.setNewTileInfo(formatInfoString());
    }

    private void unselect() {
        updateColor(false);
        tileInfoTextField.hide();
    }

    private RectangleInfo formatInfoString() {
        AbstractShape shape = new AbstractShape(pixelSummits);

        double width;
        double height;
        if (RectangleHelper.isInclinedRectangle(pixelSummits)) {
            List<Double> segmentsLenght = Segment.fromPoints(pixelSummits).stream().map(s -> s.getLenght()).collect(Collectors.toList());
            width = this.zoomManager.pixelsToMeters(Collections.min(segmentsLenght));
            height = this.zoomManager.pixelsToMeters(Collections.max(segmentsLenght));
        } else {
            width = this.zoomManager.pixelsToMeters(ShapeHelper.getWidth(shape));
            height = this.zoomManager.pixelsToMeters(ShapeHelper.getHeight(shape));
        }

        Point topLeft = ShapeHelper.getTopLeftCorner(shape);
        double x = this.zoomManager.pixelsToMeters(topLeft.x);
        double y = this.zoomManager.pixelsToMeters(topLeft.y);
        return new RectangleInfo(new Point(x, y), width, height);
    }
}
