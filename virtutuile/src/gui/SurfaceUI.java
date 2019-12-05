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
import javafx.scene.shape.Shape;
import utils.Id;
import utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SurfaceUI {

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
    protected double tileShift;

    protected List<TileUI> tiles;

    protected Label tileInfoTextField;

    protected List<Point> summits;
    protected Id id;

    private List<AttachmentPointUI> attachmentPoints = new ArrayList<>();

    protected Controller controller = Controller.getInstance();

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

        this.surfaceGroup = new Group();
        this.id = surfaceDto.id;
        this.isHole = surfaceDto.isHole;
        this.sealsInfo = surfaceDto.sealsInfoDto;
        this.masterTile = surfaceDto.masterTile;
        this.pattern = surfaceDto.pattern;
    }

    abstract public Shape getMainShape(); // gives the rectangle without the tiles and anchor points
    abstract public SurfaceDto toDto();
    abstract public void fill();
    abstract public void setSize(double width, double height);
    abstract public void setPosition(Point position);
    abstract public void translatePixelBy(Point translation);

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

        this.tiles = tiles.stream().map(t -> new TileUI(t, tileInfoTextField, this.zoomManager, this)).collect(Collectors.toList());
        this.surfaceGroup.getChildren().addAll(this.tiles.stream().map(t -> t.getNode()).collect(Collectors.toList()));
    }

    public double getTileAngle() {
        return tileAngle;
    }

    public void setTileAngle(double tileAngle) {
        this.tileAngle = tileAngle;
    }

    public double getTileShift() {
        return tileShift;
    }

    public void setTileShift(double tileShift) {
        this.tileShift = tileShift;
    }
}
