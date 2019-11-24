package gui;

import application.Controller;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI implements SurfaceUI {

    private Id id;
    private List<TileUI> tiles;
    private boolean isHole;

    private Group rectangleGroup;
    private Rectangle rectangle;

    private Label tileInfoTextField;

    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();

    private Controller controller = Controller.getInstance();
    private ZoomManager zoomManager;
    private SelectionManager selectionManager;
    private SnapGridUI snapGrid;

    private Point lastPointOfContact = new Point(0, 0);
    private boolean currentlyBeingDragged = false;

    private TileDto masterTile;
    private SealsInfoDto sealsInfo;

    public RectangleSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              SnapGridUI snapGrid,
                              Label tileInfoTextField
                              ) {

        this.id = surfaceDto.id;
        this.snapGrid = snapGrid;

        this.tileInfoTextField = tileInfoTextField;

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        rectangle = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);
        this.rectangleGroup = new Group(rectangle);
        rectangleGroup.setCursor(Cursor.HAND);

        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;

        this.isHole = surfaceDto.isHole;

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    private void initializeGroup() {
        rectangleGroup.setOnMouseClicked(t -> {
            selectionManager.selectSurface(this);
            t.consume();
        });

        rectangleGroup.setOnMousePressed(mouseEvent -> {
            this.lastPointOfContact = new Point(mouseEvent.getX() - rectangle.getX(), mouseEvent.getY() - rectangle.getY());
//            System.out.println(String.format("(%f, %f)", mouseEvent.getX(), mouseEvent.getY()));
        });

        rectangleGroup.setOnMouseReleased(mouseEvent -> {
            if (this.currentlyBeingDragged) {
                this.currentlyBeingDragged = false;
                this.snapToGrid();

                if (this.isHole) {
                    controller.updateSurface(this.toDto());
                    return;
                }
                this.renderTiles(controller.updateAndRefill(this.toDto(), this.masterTile, null, this.sealsInfo));
            }
        });

        rectangleGroup.setOnMouseDragged(t -> {
            hideAttachmentPoints();
            hideTiles();

            this.currentlyBeingDragged = true;

            double newX = t.getX() - this.lastPointOfContact.x;
            double newY = t.getY() - this.lastPointOfContact.y;
            rectangle.setX(newX);
            rectangle.setY(newY);

            t.consume();
        });
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

    public void fill() {
        this.renderTiles(controller.fillSurface(this.toDto(), this.masterTile, null, this.sealsInfo));
    }

    public void forceFill() {
        this.isHole = false;
        fill();
    }

    private void renderTiles(List<TileDto> tiles) {
        if (this.isHole || tiles == null || tiles.size() == 0) {
            return;
        }

        List<RectangleInfo> tilesRect = tiles.stream().map(t -> {
            List<Point> pixelPoints = t.summits.stream().map(zoomManager::metersToPixels).collect(Collectors.toList());
            return RectangleHelper.summitsToRectangleInfo(pixelPoints);
        }).collect(Collectors.toList());

        hideTiles();

        this.tiles = tilesRect.stream().map(t -> new TileUI(t, this.tileInfoTextField, this.zoomManager)).collect(Collectors.toList());
        this.rectangleGroup.getChildren().addAll(this.tiles.stream().map(t -> t.getNode()).collect(Collectors.toList()));
    }

    public void hideTiles() {
        if (this.tiles != null) {
            this.rectangleGroup.getChildren().removeIf(c -> this.tiles.stream().map(t -> t.getNode()).collect(Collectors.toList()).contains(c));
            this.tiles.clear();
        }
    }

    public Node getNode() {
        return rectangleGroup;
    }

    public void select() {
        if (attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
        }
    }

    public void unselect() {
        hideAttachmentPoints();
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
    }

    public void commitIncreaseSize() {
        this.renderTiles(controller.updateAndRefill(this.toDto(), this.masterTile, null, this.sealsInfo));
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.getSummits().stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;
        dto.isHole = this.isHole;

        if (!this.isHole && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> r.toDto()).collect(Collectors.toList());
        }

        return dto;
    }

    private void displayAttachmentPoints() {
        List<Point> summits = this.getSummits();

        for (Point summit: summits) {
            attachmentPoints.add(new AttachmentPointUI(summit, summit.cardinality, this));
        }

        rectangleGroup.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    private List<Point> getSummits() {
        Point topLeft = new Point(this.rectangle.getX(), this.rectangle.getY());
        return RectangleHelper.rectangleInfoToSummits(topLeft, rectangle.getWidth(), rectangle.getHeight());
    }

    private void hideAttachmentPoints() {
        rectangleGroup.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }

    @Override
    public Id getId() {
        return id;
    }

    public void delete() {
        hide();
        controller.removeSurface(this.toDto());
    }

    public void hide() {
        this.hideTiles();
        this.unselect();
    }

    public void setSealsInfo(SealsInfoDto newSealInfos) {
        this.sealsInfo = newSealInfos;
    }

    public void setMasterTile(TileDto newMasterTile) {
        this.masterTile = newMasterTile;
    }

    public TileDto getMasterTile() {
        return this.masterTile;
    }

    public SealsInfoDto getSealsInfo() {
        return this.sealsInfo;
    }

    public void setSize(double width, double height){
        double pixelWidth = zoomManager.metersToPixels(width);
        double pixelHeight = zoomManager.metersToPixels(height);
        rectangle.setWidth(pixelWidth);
        rectangle.setHeight(pixelHeight);
    }

    public void setPosition(Point position){

        Point topLeftCorner = zoomManager.metersToPixels(position);

        rectangle.setX(topLeftCorner.x);
        rectangle.setY(topLeftCorner.y);
    }

    public void setHole(boolean isHole) {
        this.isHole = isHole;
    }

    public Shape getMainShape() {
        return this.rectangle;
    }

    public void translateBy(Point translation) {
        this.setPosition(new Point(this.rectangle.getX() + translation.x, this.rectangle.getY() + translation.y));
    }
}