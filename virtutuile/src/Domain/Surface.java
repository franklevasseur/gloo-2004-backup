package Domain;

import utils.Id;

import java.util.ArrayList;

public class Surface {
    private Id id;
    private boolean isHole;
    private ArrayList<Tile> tiles;
    private ArrayList<Point> summits;
    private SealsInfo sealsInfo;

    public Surface(boolean pIsHole, ArrayList<Point> pSummits){
        this.isHole = pIsHole;
        this.summits = pSummits;
        this.id = new Id();
    }

    //region Get
    /**
     * Get fonctions
     */
    public Id getId() {
        return id;
    }

    public ArrayList<Point> getSummits() {
        return summits;
    }

    public ArrayList<Tile> getTiles() {
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

    public void setTiles(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

    //endregion

    public void fillSurface(Tile masterTile, SealsInfo pSealsInfo){
        //this.tiles = pTile;
        this.sealsInfo = pSealsInfo;
    }


}
