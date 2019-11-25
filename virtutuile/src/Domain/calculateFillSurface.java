package Domain;

import application.SealsInfoDto;
import application.SurfaceAssembler;
import application.SurfaceDto;
import application.TileDto;
//import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.Id;
import utils.CardinalPoint;



public class calculateFillSurface {

    private List<Domain.Point> summits;
    private SealsInfo sealsInfo;
    private boolean isRectangular;
    private Tile masterTile;

    public calculateFillSurface(){}

    /**
     * Type 1 pattern
     */
    public List<Tile> fillSurfaceWithType1(List<Point> pSurface,Tile pStartTile, Tile pMasterTile, SealsInfo pSealsInfo,  boolean pIsRectangle){

        //setup des tuiles de base avec le seal
        List<Tile> outTiles = new ArrayList<>();
        Tile startTileWtSeal = new Tile();

        double unitOfWidthA = pMasterTile.getWidth().getValue();
        double unitOfHeightA = pMasterTile.getHeight().getValue();
        double unitOfWidth = pMasterTile.getWidth().getValue() + pSealsInfo.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSealsInfo.getWidth().getValue();

        int amountOfLines = (int) Math.ceil(getHeight(pSurface).getValue() / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(getWidth(pSurface).getValue() / unitOfWidth);

        System.out.println("h " + getHeight(pSurface).getValue() + " - w " + getWidth(pSurface).getValue());
        System.out.println("l " + amountOfLines + " - C " + amountOfColumns);

        if (!pIsRectangle) {
            // TODO: find another logic

            throw new RuntimeException("Ça va te prendre une logique par defaut pour remplir des surfaces irrégulières mon ti-chum");
        }else {

            for (int line = 0; line < amountOfLines ; line++) {
                for (int column = 0; column < amountOfColumns ; column++) {
                    Tile nextTile = new Tile();

                    Point topLeftCorner = Point.translate(pSurface.get(0), column * unitOfWidth, line * unitOfHeight);
                    //System.out.println("X " + column * unitOfWidth + " - Y " + line * unitOfHeight);

                    double rightSurfaceBound = pSurface.get(0).getX().getValue() + getWidth(pSurface).getValue();
                    double bottomSurfaceBound = pSurface.get(0).getY().getValue() + getHeight(pSurface).getValue();

                    boolean isTileOverflowX = topLeftCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                    boolean isTileOverflowY = topLeftCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                    double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                    double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.getY().getValue() : pMasterTile.getHeight().getValue();

                    nextTile.setSummits(rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight));
                    nextTile.setMaterial(pMasterTile.getMaterial());

                    outTiles.add(nextTile);
                }
            }
        }
        System.out.println("******************************************");
        return outTiles;
    }

    /**
     * Type 2 pattern
     */
    public List<Tile> fillSurfaceWithType2(List<Point> pSurface,Tile pStartTile, Tile pMasterTile, SealsInfo pSealsInfo,  boolean pIsRectangle){
        List<Tile> outTiles = new ArrayList<>();
        Tile startTileWtSeal = new Tile();

        double unitOfWidthA = pMasterTile.getWidth().getValue();
        double unitOfHeightA = pMasterTile.getHeight().getValue();
        double unitOfWidth = pMasterTile.getWidth().getValue() + pSealsInfo.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSealsInfo.getWidth().getValue();

        int amountOfLines = (int) Math.ceil(getHeight(pSurface).getValue() / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(getWidth(pSurface).getValue() / unitOfWidth);

        System.out.println("h " + getHeight(pSurface).getValue() + " - w " + getWidth(pSurface).getValue());
        System.out.println("l " + amountOfLines + " - C " + amountOfColumns);

        if (!pIsRectangle) {
            // TODO: find another logic

            throw new RuntimeException("Ça va te prendre une logique par defaut pour remplir des surfaces irrégulières mon ti-chum");
        }else {


            for (int line = 0; line < amountOfLines ; line++) {
                for (int column = 0; column < amountOfColumns ; column++) {

                    Tile nextTile = new Tile();
                    double bottomSurfaceBound;
                    double rightSurfaceBound;
                    boolean isTileOverflowX;
                    boolean isTileOverflowY;
                    double actualWidth;
                    double actualHeight;

                    double newLenght = pMasterTile.getWidth().getValue() - pStartTile.getWidth().getValue();
                    double newHeight = pMasterTile.getHeight().getValue() - pStartTile.getHeight().getValue();
                    Point newTop = Point.translate(pSurface.get(0), -newLenght, -newHeight);

                    Point topLeftCorner = Point.translate(newTop, column * unitOfWidth, line * unitOfHeight);


                    rightSurfaceBound = pSurface.get(0).getX().getValue() + getWidth(pSurface).getValue();
                    bottomSurfaceBound = pSurface.get(0).getY().getValue() + getHeight(pSurface).getValue();

                    boolean isLowerBoundX = topLeftCorner.getX().getValue() < pSurface.get(0).getX().getValue();
                    boolean isLowerBoundY = topLeftCorner.getY().getValue() < pSurface.get(0).getY().getValue();

                    double lowerX = topLeftCorner.getX().getValue() + pSurface.get(0).getX().getValue();
                    double lowerY = topLeftCorner.getY().getValue() + pSurface.get(0).getY().getValue();

                    Measure MlowerX = new Measure(lowerX - topLeftCorner.getX().getValue());
                    Measure MlowerY = new Measure(lowerY - topLeftCorner.getY().getValue());
                    Point finalLeftCorner;

                    if (isLowerBoundX){
                        if (isLowerBoundY){
                            finalLeftCorner = new Point(MlowerX, MlowerY);
                        }else {
                            finalLeftCorner = new Point(MlowerX, topLeftCorner.getY());
                        }

                    }else {
                        if (isLowerBoundY){
                            finalLeftCorner = new Point(topLeftCorner.getX(), MlowerY);
                        }else {
                            finalLeftCorner = new Point(topLeftCorner.getY(), topLeftCorner.getY());
                        }
                    }


                    isTileOverflowX = topLeftCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                    isTileOverflowY = topLeftCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                    actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                    actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.getY().getValue() : pMasterTile.getHeight().getValue();
                    nextTile.setSummits(rectangleInfoToSummits(finalLeftCorner, actualWidth, actualHeight));
                    nextTile.setMaterial(pMasterTile.getMaterial());

                    outTiles.add(nextTile);
                }
            }
        }
        System.out.println("******************************************");
        return outTiles;
    }


    /**
     * Type 3 pattern
     */
    public List<Tile> fillSurfaceWithType3(List<Point> pSurface, Tile pMasterTile, SealsInfo pSealsInfo,  boolean pIsRectangle){
        List<Tile> outTiles = new ArrayList<>();
        boolean notFilled = true;
        double unitOfWidth = pMasterTile.getWidth().getValue() + pSealsInfo.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSealsInfo.getWidth().getValue();


        double rightSurfaceBound = pSurface.get(0).getX().getValue() + getWidth(pSurface).getValue();
        double bottomSurfaceBound = pSurface.get(0).getY().getValue() + getHeight(pSurface).getValue();

        boolean isTileOverflowX;
        boolean isTileOverflowY;
        boolean isTileUnderflowX;
        boolean isTileUnderflowY;

        double actualWidth;
        double actualHeight;

        boolean checker;

        while (notFilled) {
            Tile nextTile = new Tile();
            Point nextTopCorner;
            if(outTiles.size() == 0){

                nextTopCorner = pSurface.get(0);

                isTileOverflowX = nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;

                actualWidth = isTileOverflowX ? rightSurfaceBound - nextTopCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();

                if(pMasterTile.getSummits().get(0).getX().getValue() < pSurface.get(0).getX().getValue()){
                    isTileUnderflowX = pMasterTile.getSummits().get(0).getX().getValue() < pSurface.get(0).getX().getValue();
                    actualWidth = isTileUnderflowX ? pMasterTile.getWidth().getValue() - Math.abs(pMasterTile.getSummits().get(0).getX().getValue() - pSurface.get(0).getX().getValue())
                            : pMasterTile.getWidth().getValue();
                    //nextTopCorner = Point.translate(nextTopCorner, pMasterTile.getWidth().getValue() + actualWidth, 0);
                }
                if(pMasterTile.getSummits().get(0).getY().getValue() < pSurface.get(0).getY().getValue()){
                    isTileUnderflowY = pMasterTile.getSummits().get(0).getX().getValue() < pSurface.get(0).getX().getValue();
                    actualHeight = isTileUnderflowY ? pMasterTile.getHeight().getValue() - Math.abs(pMasterTile.getSummits().get(0).getX().getValue() - pSurface.get(0).getY().getValue())
                            : pMasterTile.getHeight().getValue();

                    //nextTopCorner = Point.translate(nextTopCorner, 0,
                            //pMasterTile.getHeight().getValue() + actualHeight);
                }

                nextTile.setSummits(rectangleInfoToSummits(nextTopCorner, actualWidth, actualHeight));
                nextTile.setMaterial(pMasterTile.getMaterial());
                outTiles.add(nextTile);
            }else{

                for (Tile element : outTiles){
                    /******************************************************************************************/
                    nextTopCorner = Point.translate(element.getSummits().get(0), unitOfWidth, 0);
                    checker = checkExisting(outTiles, nextTopCorner);

                    isTileOverflowX = nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                    isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                    actualWidth = isTileOverflowX ? rightSurfaceBound - nextTopCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                    actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();
                    if(nextTopCorner.getX().getValue() >= rightSurfaceBound){
                        checker = false;
                    }
                    /******************************************************************************************/


                    /******************************************************************************************/
                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), 0, unitOfHeight);
                        checker = checkExisting(outTiles, nextTopCorner);

                        isTileOverflowX = nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                        isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                        actualWidth = isTileOverflowX ? rightSurfaceBound - nextTopCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                        actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();
                        if(nextTopCorner.getY().getValue() >= bottomSurfaceBound){
                            checker = false;
                        }
                    }
                    /******************************************************************************************/
                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), unitOfWidth, unitOfHeight);
                        checker = checkExisting(outTiles, nextTopCorner);

                        isTileOverflowX = nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                        isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                        actualWidth = isTileOverflowX ? rightSurfaceBound - nextTopCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                        actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();
                        if(nextTopCorner.getX().getValue() >= rightSurfaceBound || nextTopCorner.getY().getValue() >= bottomSurfaceBound){
                            checker = false;
                        }
                    }
                    /******************************************************************************************/

                    /******************************************************************************************/
                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), -unitOfWidth, 0);
                        checker = true;
                        checker = checkExisting(outTiles, nextTopCorner);

                        if (nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() <= pSurface.get(0).getX().getValue()){
                            checker = false;
                        }else {
                            isTileUnderflowX = nextTopCorner.getX().getValue() < pSurface.get(0).getX().getValue();
                            isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                            actualWidth = isTileUnderflowX ? pMasterTile.getWidth().getValue() - (nextTopCorner.getX().getValue() - pSurface.get(0).getX().getValue()) : pMasterTile.getWidth().getValue();
                            actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();

                            nextTopCorner = Point.translate(nextTopCorner, pMasterTile.getWidth().getValue() + actualWidth, unitOfHeight);
                        }
                    }
                    /******************************************************************************************/

                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), -unitOfWidth, unitOfHeight);
                        checker = true;
                        checker = checkExisting(outTiles, nextTopCorner);

                        if (nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() <= pSurface.get(0).getX().getValue()
                                || nextTopCorner.getY().getValue() > bottomSurfaceBound){
                            checker = false;
                        }else {
                            isTileUnderflowX = nextTopCorner.getX().getValue() < pSurface.get(0).getX().getValue();
                            isTileOverflowY = nextTopCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound;
                            actualWidth = isTileUnderflowX ? pMasterTile.getWidth().getValue() - (nextTopCorner.getX().getValue() - pSurface.get(0).getX().getValue()) : pMasterTile.getWidth().getValue();
                            actualHeight = isTileOverflowY ? bottomSurfaceBound - nextTopCorner.getY().getValue() : pMasterTile.getHeight().getValue();

                            nextTopCorner = Point.translate(nextTopCorner, pMasterTile.getWidth().getValue() + actualWidth, unitOfHeight);
                        }
                    }


                    /***
                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), unitOfWidth, -unitOfHeight);
                        checker = true;
                        checker = checkExisting(outTiles, nextTopCorner);
                        if(nextTopCorner.getX().getValue() > rightSurfaceBound
                                && nextTopCorner.getY().getValue()  + pMasterTile.getHeight().getValue()  < pSurface.get(0).getX().getValue()){
                            checker = false;
                        }else {
                            isTileOverflowX = nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound;
                            isTileUnderflowY = nextTopCorner.getY().getValue() < pSurface.get(0).getX().getValue();
                            actualWidth = isTileOverflowX ? rightSurfaceBound - nextTopCorner.getX().getValue() : pMasterTile.getWidth().getValue();
                            actualHeight = isTileUnderflowY ? pMasterTile.getHeight().getValue() - (nextTopCorner.getY().getValue() - pSurface.get(0).getY().getValue()) : pMasterTile.getHeight().getValue();

                            nextTopCorner = Point.translate(nextTopCorner, unitOfWidth, pMasterTile.getHeight().getValue() + actualHeight);
                        }
                    }

                    if (!checker){
                        nextTopCorner = Point.translate(element.getSummits().get(0), -unitOfWidth, -unitOfHeight);
                        checker = true;
                        checker = checkExisting(outTiles, nextTopCorner);

                        if(nextTopCorner.getX().getValue() + pMasterTile.getWidth().getValue() < pSurface.get(0).getX().getValue()
                                && nextTopCorner.getY().getValue() < pSurface.get(0).getX().getValue() + pMasterTile.getHeight().getValue()){
                            checker = false;
                        }else {
                            isTileUnderflowX = nextTopCorner.getX().getValue() < pSurface.get(0).getX().getValue();
                            isTileUnderflowY = nextTopCorner.getY().getValue() < pSurface.get(0).getX().getValue();
                            actualWidth = isTileUnderflowX ? pMasterTile.getWidth().getValue() - (nextTopCorner.getX().getValue() - pSurface.get(0).getX().getValue()) : pMasterTile.getWidth().getValue();
                            actualHeight = isTileUnderflowY ? pMasterTile.getHeight().getValue() - (nextTopCorner.getY().getValue() - pSurface.get(0).getY().getValue()) : pMasterTile.getHeight().getValue();

                            nextTopCorner = Point.translate(nextTopCorner, pMasterTile.getWidth().getValue() + actualWidth,
                                    pMasterTile.getHeight().getValue() + actualHeight);
                        }
                    }
*/
                    if(element == outTiles.get(outTiles.size() - 1)){
                        if(!checker){
                            notFilled = false;
                        }else {
                            nextTile.setSummits(rectangleInfoToSummits(nextTopCorner, actualWidth, actualHeight));
                            nextTile.setMaterial(pMasterTile.getMaterial());

                            outTiles.add(nextTile);
                            break;
                        }
                    } else {
                        if(!checker){

                        }else {
                            nextTile.setSummits(rectangleInfoToSummits(nextTopCorner, actualWidth, actualHeight));
                            nextTile.setMaterial(pMasterTile.getMaterial());

                            outTiles.add(nextTile);
                            break;
                        }

                    }
                }



            }
        }
        return outTiles;
    }

    public boolean checkExisting(List<Tile> pTiles, Point pPoint){
        boolean checker = true;
        for (Tile check : pTiles){
            if (pPoint.getX().getValue() == check.getSummits().get(0).getX().getValue()
                    && pPoint.getY().getValue() == check.getSummits().get(0).getY().getValue()){
                checker = false;
                break;
            }
        }
        return checker;
    }

    /***
     * Other usefull fonctions
     */

    public Tile addSeal(Tile pTile, SealsInfo pSealInfo){
        double seal = pSealInfo.getWidth().getValue();
        double largeur = pTile.getWidth().getValue();
        double height = pTile.getHeight().getValue();
        Measure tempX = new Measure(largeur + (seal));
        Measure tempY = new Measure( height+ (seal));
        Tile newTile = new Tile(tempX, tempY, pTile.getMaterial());
        return newTile;
    }

    public Measure getHeight(List<Point> pSurface){
        Measure value = new Measure();
        double minY;
        double maxY;

        minY = pSurface.get(0).getY().getValue();
        maxY = pSurface.get(0).getY().getValue();
        for (Point i:pSurface){
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

    public static List<Point> rectangleInfoToSummits(Point topLeftCorner, double width, double height) {
        double x = topLeftCorner.getX().getValue();
        double y = topLeftCorner.getY().getValue();
        List<Point> temp = new ArrayList<>();
        Measure tempX = new Measure(width + x);
        Measure tempY = new Measure(height + y);

        Point tempA = new Point(topLeftCorner.getX(), topLeftCorner.getY());
        Point tempB = new Point(tempX, topLeftCorner.getY());
        Point tempC = new Point(topLeftCorner.getX(), tempY);
        Point tempD = new Point(tempX, tempY);
        temp.add(tempA);
        temp.add(tempB);
        temp.add(tempC);
        temp.add(tempD);

        return temp;
    }

    public Measure getWidth(List<Point> pSurface){
        Measure value = new Measure();
        double minX;
        double maxX;

        minX = pSurface.get(0).getX().getValue();
        maxX = pSurface.get(0).getX().getValue();
        for (Point i:pSurface){
            if (minX > i.getX().getValue()){
                minX = i.getX().getValue();
            }
            if (maxX < i.getX().getValue()){
                maxX = i.getX().getValue();
            }
        }
        value.setValue(maxX - minX);

        return value;
    }


}
