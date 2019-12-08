package gui;

import Domain.HoleStatus;
import Domain.PatternType;
import application.Controller;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SurfaceUI {

    protected Shape shape;

    protected TileDto masterTile;
    protected PatternType pattern;
    protected SealsInfoDto sealsInfo;
    protected HoleStatus isHole;

    protected Group surfaceGroup;

    protected SurfaceDto surfaceDto;
    protected ZoomManager zoomManager;
    protected SelectionManager selectionManager;
    protected SnapGridUI snapGrid;

    protected double tileAngle;
    protected double tileShifting;
    protected Color surfaceColor;

    protected List<TileUI> tiles;

    protected Label tileInfoTextField;

    protected List<Point> summits;
    protected Id id;

    private List<AttachmentPointUI> attachmentPoints = new ArrayList<>();

    protected Controller controller = Controller.getInstance();

    protected boolean currentlyMovingTiles = false;

    protected Point lastPointOfContactRelativeToSurface = new Point(0, 0);
    protected Point lastPointOfContactRelativeToMasterTile = new Point(0, 0);

    public SurfaceUI(SurfaceDto surfaceDto,
                     ZoomManager zoomManager,
                     SelectionManager selectionManager,
                     SnapGridUI snapGrid,
                     Label tileInfoLabel) {
        this.zoomManager = zoomManager;
        this.surfaceDto = surfaceDto;
        this.selectionManager = selectionManager;
        this.snapGrid = snapGrid;
        this.tileInfoTextField = tileInfoLabel;
        this.tileAngle  = surfaceDto.tileAngle;
        this.tileShifting  = surfaceDto.tileShifting;

        this.surfaceGroup = new Group();
        this.id = surfaceDto.id;
        this.isHole = surfaceDto.isHole;
        this.sealsInfo = surfaceDto.sealsInfoDto;
        this.masterTile = surfaceDto.masterTile;
        this.pattern = surfaceDto.pattern;
        this.surfaceColor = surfaceDto.surfaceColor;
    }

    abstract public Shape getMainShape(); // gives the rectangle without the tiles and anchor points
    abstract public SurfaceDto toDto();
    abstract public void fill();
    abstract public void setSize(double width, double height);
    abstract public void setPosition(Point position);
    abstract public void translatePixelBy(Point translation);
    abstract protected void handleSurfaceDrag(MouseEvent event);
    abstract protected Point getPixelPosition();

    public void setMasterTile(TileDto masterTile) {
        this.masterTile = masterTile;
    }

    public TileDto getMasterTile() {
        return this.masterTile;
    }

    public void setSealsInfo(SealsInfoDto newSealInfos) {
        this.sealsInfo = newSealInfos;
    }

    public SealsInfoDto getSealsInfo() {
        return this.sealsInfo;
    }

    public PatternType getPattern() {
        return this.pattern;
    }

    public void setPattern(PatternType pattern) {
        this.pattern = pattern;
    }

    public Node getNode() {
        return surfaceGroup;
    }

    public void unselect() {
        hideAttachmentPoints();
    }

    public void select(boolean setToFront) {
        if(attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
            if (setToFront) {
                this.surfaceGroup.toFront();
            }
        }
    }

    public Id getId() {
        return id;
    }

    protected void displayAttachmentPoints() {
        for(Point summit: summits) {
            attachmentPoints.add(new AttachmentPointUI(summit, summit.cardinality, this));
        }
        surfaceGroup.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    protected void hideAttachmentPoints() {
        surfaceGroup.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }

    protected void updateColor() {
        this.updateColor(false);
    }

    protected void updateColor(boolean isCurrentlyMoving) {
        if (this.isHole == HoleStatus.HOLE) {
            shape.setFill(javafx.scene.paint.Color.TRANSPARENT);
            shape.setStroke(javafx.scene.paint.Color.BLACK);
        } else if (this.isHole == HoleStatus.NONE || isCurrentlyMoving) {
            shape.setFill(ColorHelper.utilsColorToJavafx(this.surfaceColor));
            shape.setStroke(javafx.scene.paint.Color.BLACK);
        }
        else if (sealsInfo != null) {
            shape.setFill(ColorHelper.utilsColorToJavafx(sealsInfo.color));
            shape.setStroke(javafx.scene.paint.Color.BLACK);
        } else {
            shape.setFill(javafx.scene.paint.Color.WHITE);
            shape.setStroke(javafx.scene.paint.Color.BLACK);
        }
    }

    public void delete() {
        hide();
        controller.removeSurface(this.toDto());
    }

    public void hideTiles() {
        if (this.tiles != null) {
            this.surfaceGroup.getChildren().removeIf(c -> this.tiles.stream().map(t -> t.getNode()).collect(Collectors.toList()).contains(c));
            this.tiles.clear();
        }
    }

    public void hide() {
        this.hideTiles();
        this.unselect();
    }

    public void setHole(HoleStatus isHole) {
        this.isHole = isHole;
    }

    public void forceFill() {
        this.isHole = HoleStatus.FILLED;
        fill();
    }

    protected void renderTiles(List<TileDto> tiles) {
        if (this.isHole != HoleStatus.FILLED || tiles == null || tiles.size() == 0) {
            return;
        }

        hideTiles();

        this.tiles = tiles.stream().map(t -> new TileUI(t, tileInfoTextField, this.zoomManager, this, this.currentlyMovingTiles)).collect(Collectors.toList());
        this.surfaceGroup.getChildren().addAll(this.tiles.stream().map(t -> t.getNode()).collect(Collectors.toList()));
    }

    public double getTileAngle() {
        return tileAngle;
    }

    public void setTileAngle(double tileAngle) {
        this.tileAngle = tileAngle;
    }

    public double getTileShifting() {
        return tileShifting;
    }

    public void setTileShifting(double tileShifting) {
        this.tileShifting = tileShifting;
    }

    public Color getSurfaceColor() {
        return surfaceColor;
    }

    public void setSurfaceColor(Color surfaceColor) {
        this.surfaceColor = surfaceColor;
    }

    public void setCurrentlyMovingTiles(boolean currentlyMovingTiles) {
        this.currentlyMovingTiles = currentlyMovingTiles;
        if (this.tiles == null) {
            return;
        }

        this.tiles.forEach(t -> t.setHighligthIfMasterTile(currentlyMovingTiles));
    }

    protected void initializeGroup() {
        surfaceGroup.setOnMouseDragged(e -> {
            if (!currentlyMovingTiles) {
                this.handleSurfaceDrag(e);
                return;
            }
            handleTileDrag(e);
        });

        surfaceGroup.setOnMousePressed(mouseEvent -> {
            this.lastPointOfContactRelativeToSurface = new Point(mouseEvent.getX() - getPixelPosition().x, mouseEvent.getY() - getPixelPosition().y);

            if (masterTile == null) {
                return;
            }

            Point masterTilePixelTopLeft = zoomManager.metersToPixels(ShapeHelper.getTopLeftCorner(new AbstractShape(masterTile.summits)));
            this.lastPointOfContactRelativeToMasterTile = new Point(mouseEvent.getX() - masterTilePixelTopLeft.x, mouseEvent.getY() - masterTilePixelTopLeft.y);
        });
    }

    private void handleTileDrag(MouseEvent event) {
        if (this.masterTile == null || this.isHole != HoleStatus.FILLED) {
            return;
        }

        double newX = event.getX() - this.lastPointOfContactRelativeToMasterTile.x;
        double newY = event.getY() - this.lastPointOfContactRelativeToMasterTile.y;
        Point currentEventPoint = new Point(newX, newY);

        Point masterTilePixelTopLeft = zoomManager.metersToPixels(ShapeHelper.getTopLeftCorner(new AbstractShape(masterTile.summits)));
        Point translation = zoomManager.pixelsToMeters(Point.diff(currentEventPoint, masterTilePixelTopLeft));

        this.masterTile.summits = this.masterTile.summits.stream()
                .map(s -> Point.translate(s, translation.x, translation.y))
                .collect(Collectors.toList());
        this.fill();

        event.consume();
    }
}
