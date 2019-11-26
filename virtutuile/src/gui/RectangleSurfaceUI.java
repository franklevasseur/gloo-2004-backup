package gui;

import Domain.HoleStatus;
import application.*;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
    private HoleStatus isHole;

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

        this.masterTile = surfaceDto.masterTile;
        this.sealsInfo = surfaceDto.sealsInfoDto;
        this.tileInfoTextField = tileInfoTextField;

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        rectangle = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);

        this.rectangleGroup = new Group(rectangle);
        rectangleGroup.setCursor(Cursor.HAND);

        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;

        this.isHole = surfaceDto.isHole;
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
            rectangle.setFill(ColorHelper.utilsColorToMofackingJavafxColorTiChum(sealsInfo.color));
            rectangle.setStroke(Color.BLACK);
        } else {
            rectangle.setFill(Color.WHITE);
            rectangle.setStroke(Color.BLACK);
        }
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

                if (this.isHole != HoleStatus.FILLED || this.tiles == null) {
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
        setRectangleColor();
    }

    public void forceFill() {
        this.isHole = HoleStatus.FILLED;
        fill();
    }

    private void renderTiles(List<TileDto> tiles) {
        if (this.isHole != HoleStatus.FILLED || tiles == null || tiles.size() == 0) {
            return;
        }

        MaterialDto materialDto = tiles.get(0).material;

        List<RectangleInfo> tilesRect = tiles.stream().map(t -> {
            List<Point> pixelPoints = t.summits.stream().map(zoomManager::metersToPixels).collect(Collectors.toList());
            return RectangleHelper.summitsToRectangleInfo(pixelPoints);
        }).collect(Collectors.toList());

        hideTiles();

        this.tiles = tilesRect.stream().map(t -> new TileUI(t, this.tileInfoTextField, this.zoomManager, materialDto)).collect(Collectors.toList());
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
        this.select(false);
    }

    public void select(boolean setToFront) {
        if (attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
            if (setToFront) {
                this.rectangleGroup.toFront();
            }
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
        if (this.isHole == HoleStatus.FILLED) {
            this.renderTiles(controller.updateAndRefill(this.toDto(), this.masterTile, null, this.sealsInfo));
            return;
        }
        this.controller.updateSurface(this.toDto());
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.getSummits().stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;
        dto.isHole = this.isHole;
        dto.masterTile = this.masterTile;

        if (this.isHole == HoleStatus.FILLED && this.tiles != null && this.tiles.size() != 0) {
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

    public void setHole(HoleStatus isHole) {
        this.isHole = isHole;
    }

    public Shape getMainShape() {
        return this.rectangle;
    }

    public void translatePixelBy(Point translation) {
        rectangle.setX(this.rectangle.getX() + translation.x);
        rectangle.setY(this.rectangle.getY() + translation.y);
    }
}