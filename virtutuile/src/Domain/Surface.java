package Domain;

import utils.AbstractShape;
import utils.Id;
import utils.RectangleHelper;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Surface {
    private Id id;
    private HoleStatus isHole;
    private List<Tile> tiles = new ArrayList<>();
    private List<Point> summits;
    private SealsInfo sealsInfo;
    private boolean isRectangular = true;

    private Tile masterTile;

    public Surface(HoleStatus pIsHole, List<Point> pSummits, boolean isRectangular){
        this.isHole = pIsHole;
        this.summits = pSummits;
        this.id = new Id();
        this.isRectangular = isRectangular;
    }

    //region Get
    /**
     * Get fonctions
     */
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

    //endregion
    //region set
    /**
     * Set functions
     */

    public void setSealsInfo(SealsInfo sealsInfo) {
        this.sealsInfo = sealsInfo;
    }

    public void setHole(HoleStatus hole) {
        isHole = hole;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    //endregion

    public void fillSurface(Tile masterTile, SealsInfo pSealsInfo, PatternType pType) {
        calculateFillSurface filler = new calculateFillSurface();

        this.sealsInfo = pSealsInfo;

        switch(pType) {
            case TYPE1:

                tiles = filler.fillSurfaceWithType3(summits, masterTile, pSealsInfo, isRectangular);
                break;
            case TYPE2:
                tiles = filler.fillSurfaceWithType2(summits, masterTile, pSealsInfo, isRectangular);
                break;
            case TYPE3:
                tiles = filler.fillSurfaceWithType4(summits, masterTile, pSealsInfo, isRectangular);
                break;
            default:
                // code block
        }

        tiles.forEach(t -> {
            List<utils.Point> summits = t.getSummits().stream().map(s -> s.toAbstract()).collect(Collectors.toList());
            t.setSummits(RectangleHelper.getClockWise(summits).stream().map(s -> new Point(s.x, s.y)).collect(Collectors.toList()));
        });

        isHole = HoleStatus.FILLED;
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
        return new AbstractShape(summits.stream().map(s -> s.toAbstract()).collect(Collectors.toList()), this.isHole == HoleStatus.HOLE);
    }

    public double getWidth(){
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
}
