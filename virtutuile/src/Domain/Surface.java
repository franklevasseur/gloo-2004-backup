package Domain;

import utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Surface implements Serializable {
    private Id id;
    private HoleStatus isHole;
    private List<Tile> tiles = new ArrayList<>();
    private List<Point> summits;
    private SealsInfo sealsInfo;
    private boolean isRectangular;
    private Color surfaceColor;

    private SurfaceFiller surfaceFiller = new SurfaceFiller();

    private Tile masterTile;
    private PatternType pattern;

    private double tileAngle;
    private double tileShifting;

    public Surface(HoleStatus pIsHole, List<Point> pSummits, boolean isRectangular) {
        this.isHole = pIsHole;
        this.summits = pSummits;
        this.id = new Id();
        this.isRectangular = isRectangular;
    }

    public Id getId() {
        return id;
    }

    public List<Point> getSummits() {
        return summits;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public HoleStatus isHole() {
        return isHole;
    }

    public SealsInfo getSealsInfo() {
        return sealsInfo;
    }

    public void setSealsInfo(SealsInfo sealsInfo) {
        this.sealsInfo = sealsInfo;
    }

    public void setHole(HoleStatus hole) {
        isHole = hole;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public void fillSurface(Tile masterTile, SealsInfo pSealsInfo, PatternType pType, double angle, double tileShifting) {
        this.sealsInfo = pSealsInfo;
        this.tileAngle = angle;
        this.tileShifting = tileShifting;

        tiles = surfaceFiller.fillSurface(this, masterTile, pSealsInfo, pType, angle, tileShifting);

        // très important !!
        isHole = HoleStatus.FILLED;
        this.masterTile = masterTile;
        this.pattern = pattern;
    }

    public void setSummits(List<Point> summits) {
        this.summits = summits;
    }

    public boolean getIsRectangular() {
        return this.isRectangular;
    }

    public void setIsRectangular(boolean isRectangular) {
        this.isRectangular = isRectangular;
    }

    public double getHeight() {
        return ShapeHelper.getHeight(this.toAbstractShape());
    }

    private AbstractShape toAbstractShape() {
        return new AbstractShape(summits, this.isHole == HoleStatus.HOLE);
    }

    public double getWidth() {
        return ShapeHelper.getWidth(this.toAbstractShape());
    }

    public boolean isFusionned() {
        return false;
    }

    public Tile getMasterTile() {
        return masterTile;
    }

    public void setMasterTile(Tile masterTile) {
        this.masterTile = masterTile;
    }

    public PatternType getPattern() {
        return pattern;
    }

    public void setPattern(PatternType pattern) {
        this.pattern = pattern;
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
}
