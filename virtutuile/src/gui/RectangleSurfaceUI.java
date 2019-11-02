package gui;

import application.Controller;
import application.SurfaceDto;
import application.TileDto;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI implements SurfaceUI {

    private Id id;
    private List<Rectangle> tiles;
    private boolean isHole;

    private Rectangle rectangle;
    private boolean isSelected = false;
    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();
    private Pane parentNode;
    private Controller controller = Controller.getInstance();
    private ZoomManager zoomManager;
    private SelectionManager selectionManager;
    private SnapGridUI snapGrid;

    private boolean currentlyBeingDragged = false;

    public RectangleSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              Pane parentNode,
                              SnapGridUI snapGrid) {

        this.id = surfaceDto.id;
        this.snapGrid = snapGrid;

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        rectangle = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.BLACK);

        this.parentNode = parentNode;
        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;

        this.isHole = surfaceDto.isHole;

        if (!surfaceDto.isHole && surfaceDto.tiles != null && surfaceDto.tiles.size() != 0) {
            this.renderTiles(surfaceDto.tiles.stream().map(t -> {
                List<Point> pixelPoints = t.summits.stream().map(zoomManager::metersToPixels).collect(Collectors.toList());
                return RectangleHelper.summitsToRectangleInfo(pixelPoints);
            }).collect(Collectors.toList()));
        }

        RectangleSurfaceUI that = this;
        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                selectionManager.selectSurface(that);
                t.consume();
            }
        });

        rectangle.setOnMouseReleased(mouseEvent -> {
            if (this.currentlyBeingDragged) {
                this.currentlyBeingDragged = false;
                this.snapToGrid();
            }
        });

        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                hideAttachmentPoints();
                hideTiles();

                that.currentlyBeingDragged = true;

                rectangle.setX(t.getX() - (rectangle.getWidth() / 2));
                rectangle.setY(t.getY() - (rectangle.getHeight() / 2));

                t.consume();

                controller.updateSurface(that.toDto());
            }
        });

        rectangle.setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                rectangle.setCursor(Cursor.HAND);
            }
        });

        rectangle.setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                rectangle.setCursor(Cursor.DEFAULT);
            }
        });

        this.parentNode.getChildren().add(rectangle);
        rectangle.toFront();
    }

    private void snapToGrid() {
        if (this.snapGrid.isVisible()) {
            Point currentRectanglePosition = new Point(this.rectangle.getX(), this.rectangle.getY());
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentRectanglePosition);
            this.rectangle.setX(nearestGridPoint.x);
            this.rectangle.setY(nearestGridPoint.y);
            this.controller.updateSurface(this.toDto());
        }
    }

    private void renderTiles(List<RectangleInfo> tiles) {
        // TODO: refactor this in its own class
        this.tiles = tiles.stream().map(t -> {
            Rectangle tileUI = new Rectangle(t.topLeftCorner.x, t.topLeftCorner.y, t.width, t.height);
            tileUI.setFill(Color.PALETURQUOISE);
            tileUI.setStroke(Color.DARKTURQUOISE);
            return tileUI;
        }).peek(t -> {
            this.parentNode.getChildren().add(t);
            t.toFront();
        }).collect(Collectors.toList());
    }

    private void hideTiles() {
        if (this.tiles != null) {
            this.parentNode.getChildren().removeIf(this.tiles::contains);
            this.tiles.clear();
        }
    }

    public Node getNode() {
        return rectangle;
    }

    public void select() {
        isSelected = true;
        if (attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
        }
    }

    public void unselect() {
        isSelected = false;
        hideAttachmentPoints();
    }

    public void increaseSizeBy(double deltaWidth, double deltaHeight) {
        double newWidth = rectangle.getWidth() + deltaWidth;
        double newHeight = rectangle.getHeight() + deltaHeight;

        hideTiles();

        if (newWidth >=0) {
            rectangle.setWidth(newWidth);
        }
        if (newHeight >= 0) {
            rectangle.setHeight(newHeight);
        }

        controller.updateSurface(this.toDto());
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.getSummits().stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;

        if (!this.isHole && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> {
                TileDto tile = new TileDto();
                tile.summits = RectangleHelper.rectangleInfoToSummits(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                return tile;
            }).collect(Collectors.toList());
        }

        return dto;
    }

    private void displayAttachmentPoints() {
        List<Point> summits = this.getSummits();

        for (Point summit: summits) {
            attachmentPoints.add(new AttachmentPointUI(summit, summit.cardinality, this));
        }

        parentNode.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    private List<Point> getSummits() {
        Point topLeft = new Point(rectangle.getX(), rectangle.getY());
        return RectangleHelper.rectangleInfoToSummits(topLeft, rectangle.getWidth(), rectangle.getHeight());
    }

    private void hideAttachmentPoints() {
        parentNode.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }

    @Override
    public Id getId() {
        return id;
    }

    public void remove() {
        this.hideTiles();
        this.unselect();
        controller.removeSurface(this.toDto());
        parentNode.getChildren().remove(this.getNode());
    }
}