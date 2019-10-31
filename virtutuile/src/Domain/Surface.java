package Domain;

import utils.Id;

import java.util.ArrayList;
import java.util.List;

public class Surface {
    private Id id;
    private boolean isHole;
    private List<Tile> tiles = new ArrayList<>();
    private List<Point> summits;
    private SealsInfo sealsInfo;
    private boolean isRectangular = true;

    public Surface(boolean pIsHole, List<Point> pSummits, boolean isRectangular){
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

    public boolean isHole() {
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

    public void setHole(boolean hole) {
        isHole = hole;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    //endregion

    public void fillSurface(Tile masterTile, SealsInfo pSealsInfo){
        //this.tiles = pTile;
        this.sealsInfo = pSealsInfo;
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
}
