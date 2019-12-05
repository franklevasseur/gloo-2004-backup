package gui;

import Domain.HoleStatus;
import application.Controller;
import application.SurfaceDto;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import utils.AbstractShape;
import utils.Point;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IrregularSurfaceUI extends SurfaceUI {

    private Polygon polygon;

    private Point lastPointOfContact = new Point(0, 0);
    private boolean currentlyBeingDragged = false;

    public IrregularSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              SnapGridUI snapGrid,
                              Label tileInfoTextField) {
        super(surfaceDto, zoomManager, selectionManager, snapGrid, tileInfoTextField);

        renderRectangleFromSummits(surfaceDto.summits.stream().map(s -> zoomManager.metersToPixels(s)).collect(Collectors.toList()));
        setPolygonColor();

        summits = this.getSummits();

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    private void renderRectangleFromSummits(List<Point> newSummits) {
        super.surfaceGroup.getChildren().remove(polygon);

        List<Double> allNumbers = new ArrayList<>();
        newSummits.stream().forEach(s -> {
            allNumbers.add(s.x);
            allNumbers.add(s.y);
        });

        polygon = new Polygon();
        polygon.setFill(Color.WHITE);
        polygon.setStroke(Color.BLACK);
        polygon.getPoints().addAll(allNumbers);

        polygon.setCursor(Cursor.HAND);

        super.surfaceGroup.getChildren().add(polygon);
    }

    private void setPolygonColor() {
        if (this.isHole == HoleStatus.HOLE) {
            polygon.setFill(Color.TRANSPARENT);
            polygon.setStroke(Color.BLACK);
        } else if (this.isHole == HoleStatus.NONE) {
            polygon.setFill(Color.WHITE);
            polygon.setStroke(Color.BLACK);
        }
        else if (sealsInfo != null) {
            polygon.setFill(ColorHelper.utilsColorToJavafx(sealsInfo.color));
            polygon.setStroke(Color.BLACK);
        } else {
            polygon.setFill(Color.WHITE);
            polygon.setStroke(Color.BLACK);
        }
    }

    private void initializeGroup() {
        surfaceGroup.setOnMouseClicked(t -> {
            selectionManager.selectSurface(this);
            t.consume();
        });

        surfaceGroup.setOnMousePressed(mouseEvent -> {
            this.lastPointOfContact = new Point(mouseEvent.getX() - getPosition().x, mouseEvent.getY() - getPosition().y);
//            System.out.println(String.format("(%f, %f)", mouseEvent.getX(), mouseEvent.getY()));
        });

        surfaceGroup.setOnMouseReleased(mouseEvent -> {
            if (this.currentlyBeingDragged) {
                this.currentlyBeingDragged = false;
                this.snapToGrid();

                if (this.isHole != HoleStatus.FILLED || this.tiles == null) {
                    controller.updateSurface(this.toDto());
                    return;
                }
                this.renderTiles(controller.updateAndRefill(this.toDto(), super.masterTile, super.pattern, super.sealsInfo, super.tileAngle));
                setPolygonColor();
            }
        });

        surfaceGroup.setOnMouseDragged(t -> {
            hideAttachmentPoints();
            hideTiles();

            this.currentlyBeingDragged = true;

            double newX = t.getX() - this.lastPointOfContact.x;
            double newY = t.getY() - this.lastPointOfContact.y;

//            System.out.println(String.format("(%f, %f)", getPosition().x, getPosition().y));

            Point translation = Point.diff(new Point(newX, newY), getPosition());
            this.translatePixelBy(translation);

            t.consume();
        });
    }

    private void snapToGrid() {
        if (super.snapGrid.isVisible()) {
            Point currentRectanglePosition = new Point(getPosition().x, getPosition().y);
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentRectanglePosition);

            Point translation = Point.diff(getPosition(), new Point(nearestGridPoint.x, nearestGridPoint.y));
            this.renderRectangleFromSummits(this.summits.stream().map(s -> s.translate(translation)).collect(Collectors.toList()));

            summits = this.getSummits();

            this.controller.updateSurface(this.toDto());
        }
    }

    private List<Point> getSummits() {
        List<Double> coords = polygon.getPoints();
        List<Point> returned = new ArrayList<>();

        int index = 0;
        for (Double coord : coords) {
            if (index % 2 == 1) {
                Double x = coords.get(index - 1);
                Double y = coords.get(index);
                returned.add(new Point(x, y));
            }
            index++;
        }
        return returned;
    }

    private Point getPosition() {
        AbstractShape shape = new AbstractShape(this.getSummits(), false);
        return ShapeHelper.getTopLeftCorner(shape);
    }

    @Override
    public Shape getMainShape() {
        return polygon;
    }

    @Override
    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.summits.stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = false;
        dto.id = this.id;
        dto.isHole = this.isHole;
        dto.masterTile = super.masterTile;
        dto.pattern = super.pattern;

        if (this.isHole == HoleStatus.FILLED && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> r.toDto()).collect(Collectors.toList());
        }

        return dto;
    }

    @Override
    public void fill() {
        this.renderTiles(controller.fillSurface(this.toDto(), super.masterTile, super.pattern, super.sealsInfo, super.tileAngle));
        setPolygonColor();
    }

    @Override
    public void setSize(double width, double height) {
        // nothing...
    }

    @Override
    public void setPosition(Point position) {
        Point pixelPosition = zoomManager.metersToPixels(position);

        Point translation = Point.diff(pixelPosition, getPosition());
        translatePixelBy(translation);
    }

    @Override
    public void translatePixelBy(Point translation) {
        this.renderRectangleFromSummits(this.summits.stream().map(s -> s.translate(translation)).collect(Collectors.toList()));
        summits = this.getSummits();
    }
}
