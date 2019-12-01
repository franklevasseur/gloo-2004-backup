package gui;

import Domain.HoleStatus;
import application.*;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI extends SurfaceUI {
    
    private Rectangle rectangle;

    private Point lastPointOfContact = new Point(0, 0);
    private boolean currentlyBeingDragged = false;

    public RectangleSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              SnapGridUI snapGrid,
                              Label tileInfoTextField
                              ) {

        super(surfaceDto, zoomManager, selectionManager, snapGrid, tileInfoTextField);

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        rectangle = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);
        summits = this.getSummits();

        super.surfaceGroup.getChildren().add(rectangle);
        surfaceGroup.setCursor(Cursor.HAND);

        this.setRectangleColor();

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    private void setRectangleColor() {
        if (this.isHole == HoleStatus.HOLE) {
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(Color.BLACK);
        } else if (this.isHole == HoleStatus.NONE) {
            rectangle.setFill(Color.WHITE);
            rectangle.setStroke(Color.BLACK);
        }
        else if (sealsInfo != null) {
            rectangle.setFill(ColorHelper.utilsColorToJavafx(sealsInfo.color));
            rectangle.setStroke(Color.BLACK);
        } else {
            rectangle.setFill(Color.WHITE);
            rectangle.setStroke(Color.BLACK);
        }
    }

    private void initializeGroup() {
        surfaceGroup.setOnMouseClicked(t -> {
            selectionManager.selectSurface(this);
            t.consume();
        });

        surfaceGroup.setOnMousePressed(mouseEvent -> {
            this.lastPointOfContact = new Point(mouseEvent.getX() - rectangle.getX(), mouseEvent.getY() - rectangle.getY());
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
                this.renderTiles(controller.updateAndRefill(this.toDto(), super.masterTile, null, super.sealsInfo));
            }
        });

        surfaceGroup.setOnMouseDragged(t -> {
            hideAttachmentPoints();
            hideTiles();

            this.currentlyBeingDragged = true;

            double newX = t.getX() - this.lastPointOfContact.x;
            double newY = t.getY() - this.lastPointOfContact.y;
            rectangle.setX(newX);
            rectangle.setY(newY);
            summits = this.getSummits();

            t.consume();
        });
    }

    private void snapToGrid() {
        if (this.snapGrid.isVisible()) {
            Point currentRectanglePosition = new Point(this.rectangle.getX(), this.rectangle.getY());
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentRectanglePosition);
            this.rectangle.setX(nearestGridPoint.x);
            this.rectangle.setY(nearestGridPoint.y);
            summits = this.getSummits();

            this.controller.updateSurface(this.toDto());
        }
    }

    public void fill() {
        this.renderTiles(controller.fillSurface(this.toDto(), super.masterTile, null, super.sealsInfo));
        setRectangleColor();
    }

    public void increaseSizeBy(double deltaWidth, double deltaHeight) {
        double newWidth = rectangle.getWidth() + deltaWidth;
        double newHeight = rectangle.getHeight() + deltaHeight;

        hideTiles();

        if (newWidth >= 0) {
            rectangle.setWidth(newWidth);
        }
        if (newHeight >= 0) {
            rectangle.setHeight(newHeight);
        }
        summits = this.getSummits();
    }

    public void commitIncreaseSize() {
        if (this.isHole == HoleStatus.FILLED) {
            this.renderTiles(controller.updateAndRefill(this.toDto(), super.masterTile, null, super.sealsInfo));
            return;
        }
        this.controller.updateSurface(this.toDto());
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.summits.stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;
        dto.isHole = this.isHole;
        dto.masterTile = super.masterTile;
        dto.pattern = super.pattern;

        if (this.isHole == HoleStatus.FILLED && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> r.toDto()).collect(Collectors.toList());
        }

        return dto;
    }

    private List<Point> getSummits() {
        Point topLeft = new Point(this.rectangle.getX(), this.rectangle.getY());
        return RectangleHelper.rectangleInfoToSummits(topLeft, rectangle.getWidth(), rectangle.getHeight());
    }

    public void setSize(double width, double height){
        double pixelWidth = zoomManager.metersToPixels(width);
        double pixelHeight = zoomManager.metersToPixels(height);
        rectangle.setWidth(pixelWidth);
        rectangle.setHeight(pixelHeight);
        summits = this.getSummits();
    }

    public void setPosition(Point position) {

        Point topLeftCorner = zoomManager.metersToPixels(position);

        rectangle.setX(topLeftCorner.x);
        rectangle.setY(topLeftCorner.y);
        summits = this.getSummits();
    }

    public Shape getMainShape() {
        return this.rectangle;
    }

    public void translatePixelBy(Point translation) {
        rectangle.setX(this.rectangle.getX() + translation.x);
        rectangle.setY(this.rectangle.getY() + translation.y);
        summits = this.getSummits();
    }
}