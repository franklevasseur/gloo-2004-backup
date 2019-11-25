package Domain;

import utils.Id;

import java.util.ArrayList;
import java.util.List;

public class Surface {
    private Id id;
    private HoleStatus isHole;
    private List<Tile> tiles = new ArrayList<>();
    private List<Point> summits;
    private SealsInfo sealsInfo;
    private boolean isRectangular = true;

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

    public void fillSurface(Tile masterTile, SealsInfo pSealsInfo, PatternType pType){
        // TODO: Ajouter paramètre de la tuile type
        //this.tiles = pTile;
        calculateFillSurface filler = new calculateFillSurface();
        Measure tempX = new Measure(1.4);
        Measure tempY = new Measure(1.4);
        //variable pour les dimension dune tuile normal
        Tile tileType = new Tile(tempX, tempY, masterTile.getMaterial());
        this.sealsInfo = pSealsInfo;

        switch(pType) {
            case TYPE1:

                tiles = filler.fillSurfaceWithType1(summits, masterTile, tileType, pSealsInfo, isRectangular);
                break;
            case TYPE2:
                tiles = filler.fillSurfaceWithType3(summits, masterTile, pSealsInfo, isRectangular);
                break;
            case TYPE3:
                tiles = filler.fillSurfaceWithType3(summits, masterTile, pSealsInfo, isRectangular);
                break;
            default:
                // code block
        }

        //return tiles;
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

    public Measure getHeight(){
        Measure value = new Measure();
        double minY;
        double maxY;

        minY = summits.get(0).getY().getValue();
        maxY = summits.get(0).getY().getValue();
        for (Point i:summits){
            if (minY > i.getY().getValue()){
                minY = i.getY().getValue();
            }
            if (maxY < i.getY().getValue()){
                maxY = i.getY().getValue();
            }
        }
        value.setValue(maxY - minY);

        return value;
    }

    public Measure getWidth(){
        Measure value = new Measure();
        double minX;
        double maxX;

        minX = summits.get(0).getY().getValue();
        maxX = summits.get(0).getY().getValue();
        for (Point i:summits){
            if (minX > i.getY().getValue()){
                minX = i.getY().getValue();
            }
            if (maxX < i.getY().getValue()){
                maxX = i.getY().getValue();
            }
        }
        value.setValue(maxX - minX);

        return value;
    }

    public boolean isFusionned() {
        return false;
    }
}
