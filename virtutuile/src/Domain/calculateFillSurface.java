package Domain;


import utils.AbstractShape;
import utils.Point;
import utils.RectangleHelper;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class calculateFillSurface {

    public calculateFillSurface() {
    }

    /**
     * Type 1 pattern
     * cest de la poubelle cette fonction ne pas utiliser
     */
    public List<Tile> fillSurfaceWithType1(List<Point> pSurface, Tile pStartTile, Tile pMasterTile, SealsInfo pSealsInfo, boolean pIsRectangle) {

        //setup des tuiles de base avec le seal
        List<Tile> outTiles = new ArrayList<>();
        Tile startTileWtSeal = new Tile();

        double unitOfWidthA = pMasterTile.getWidth();
        double unitOfHeightA = pMasterTile.getHeight();
        double unitOfWidth = pMasterTile.getWidth() + pSealsInfo.getWidth();
        double unitOfHeight = pMasterTile.getHeight() + pSealsInfo.getWidth();

        int amountOfLines = (int) Math.ceil(getHeight(pSurface) / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(getWidth(pSurface) / unitOfWidth);

        System.out.println("h " + getHeight(pSurface) + " - w " + getWidth(pSurface));
        System.out.println("l " + amountOfLines + " - C " + amountOfColumns);

        if (!pIsRectangle) {
            // TODO: find another logic

            throw new RuntimeException("Ça va te prendre une logique par defaut pour remplir des surfaces irrégulières mon ti-chum");
        } else {

            for (int line = 0; line < amountOfLines; line++) {
                for (int column = 0; column < amountOfColumns; column++) {
                    Tile nextTile = new Tile();

                    Point topLeftCorner = Point.translate(pSurface.get(0), column * unitOfWidth, line * unitOfHeight);
                    //System.out.println("X " + column * unitOfWidth + " - Y " + line * unitOfHeight);

                    double rightSurfaceBound = pSurface.get(0).x + getWidth(pSurface);
                    double bottomSurfaceBound = pSurface.get(0).y + getHeight(pSurface);

                    boolean isTileOverflowX = topLeftCorner.x + pMasterTile.getWidth() > rightSurfaceBound;
                    boolean isTileOverflowY = topLeftCorner.y + pMasterTile.getHeight() > bottomSurfaceBound;
                    double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : pMasterTile.getWidth();
                    double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : pMasterTile.getHeight();

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
     * Tuile tourné 90degré aligné
     */
    public List<Tile> fillSurfaceWithType2(List<Point> pSurface, Tile pMasterTile, SealsInfo pSealsInfo, boolean pIsRectangle) {
        Tile rotated = rotate90(pMasterTile);
        return fillSurfaceWithType3(pSurface, rotated, pSealsInfo, pIsRectangle);
    }


    /**
     * Type 3 pattern
     * Tuile normal aligné
     */
    public List<Tile> fillSurfaceWithType3(List<Point> pSurface, Tile tMasterTile, SealsInfo pSealsInfo, boolean pIsRectangle) {
        Tile pMasterTile = translateTile(pSurface, tMasterTile);
        List<Tile> outTiles = new ArrayList<>();
        List<Point> oldCorner = new ArrayList<>();
        boolean notFilled = true;

        //prendre les vrais valeur de longueur et largeur de la tuile
        double unitOfWidth = pMasterTile.getWidth() + pSealsInfo.getWidth();
        double unitOfHeight = pMasterTile.getHeight() + pSealsInfo.getWidth();

        boolean checker = true;

        int testCount = 0;

        while (notFilled) {
            Tile nextTile = new Tile();
            Point nextTopCorner;
            if (outTiles.size() == 0) {
                nextTopCorner = pMasterTile.getSummits().get(0);
                nextTile = trimTile(pSurface, pMasterTile, nextTopCorner);
                outTiles.add(nextTile);
                oldCorner.add(nextTopCorner);
            }else {
                for (Point element : oldCorner){
                    nextTopCorner = Point.translate(element, unitOfWidth, 0);
                    checker = checkExisting(oldCorner, nextTopCorner);

                    if(!checker || !tileInBound(pSurface, pMasterTile, nextTopCorner)){
                        nextTopCorner = Point.translate(element, -unitOfWidth, 0);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }

                    if(!checker || !tileInBound(pSurface, pMasterTile, nextTopCorner)){
                        nextTopCorner = Point.translate(element, 0, unitOfHeight);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }

                    if(!checker || !tileInBound(pSurface, pMasterTile, nextTopCorner)){
                        nextTopCorner = Point.translate(element, 0, -unitOfHeight);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }
                    /**********************************************************************************/

                    if(!checker || !tileInBound(pSurface, pMasterTile, nextTopCorner)){
                        if(element == oldCorner.get(oldCorner.size() - 1)){
                            notFilled = false;
                            break;
                        }

                    }else{
                        nextTile = trimTile(pSurface, pMasterTile, nextTopCorner);
                        outTiles.add(nextTile);
                        oldCorner.add(nextTopCorner);

                        break;
                    }
                }

            }
            /**
            if(testCount == 1){
                notFilled = false;
            }else {
                testCount++;
            }*/
        }
        return outTiles;
    }

    /**
     * Type 4 pattern
     * Tuile normal désaligné
     */
    public List<Tile> fillSurfaceWithType4(List<Point> pSurface, Tile tMasterTile, SealsInfo pSealsInfo, boolean pIsRectangle) {
        Tile pMasterTile = translateTile(pSurface, tMasterTile);
        List<Tile> outTiles = new ArrayList<>();
        List<Point> oldCorner = new ArrayList<>();
        boolean notFilled = true;

        //prendre les vrais valeur de longueur et largeur de la tuile
        double unitOfWidth = pMasterTile.getWidth() + pSealsInfo.getWidth();
        double unitOfHeight = pMasterTile.getHeight() + pSealsInfo.getWidth();

        boolean checker = true;
        boolean inBound = true;
        int testCount = 0;

        while (notFilled) {
            Tile nextTile = new Tile();
            Point nextTopCorner;
            if (outTiles.size() == 0) {
                nextTopCorner = pMasterTile.getSummits().get(0);
                nextTile = trimTile(pSurface, pMasterTile, nextTopCorner);
                outTiles.add(nextTile);
                oldCorner.add(nextTopCorner);
            }else {
                for (Point element : oldCorner){
                    nextTopCorner = Point.translate(element, unitOfWidth, 0);
                    checker = checkExisting(oldCorner, nextTopCorner);

                    inBound = tileInBound(pSurface, pMasterTile, nextTopCorner);
                    if(!checker || !inBound){
                        nextTopCorner = Point.translate(element, -unitOfWidth, 0);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }

                    inBound = tileInBound(pSurface, pMasterTile, nextTopCorner);
                    if(!checker || !inBound){
                        nextTopCorner = Point.translate(element, unitOfWidth/2, unitOfHeight);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }

                    inBound = tileInBound(pSurface, pMasterTile, nextTopCorner);
                    if(!checker || !inBound){
                        nextTopCorner = Point.translate(element, unitOfWidth/2, -unitOfHeight);
                        checker = checkExisting(oldCorner, nextTopCorner);
                    }
                    /**********************************************************************************/

                    inBound = tileInBound(pSurface, pMasterTile, nextTopCorner);
                    if(!checker || !inBound){
                        if(element == oldCorner.get(oldCorner.size() - 1)){
                            notFilled = false;
                            break;
                        }

                    }else{
                        nextTile = trimTile(pSurface, pMasterTile, nextTopCorner);
                        outTiles.add(nextTile);
                        oldCorner.add(nextTopCorner);

                        break;
                    }
                }

            }
            /**
            if(testCount == 7){
                notFilled = false;
            }else {
                testCount++;
            }*/
        }
        return outTiles;
    }

    /**
     * déplace le premier tile au debut
     */
    public Tile translateTile(List<Point> pSurface, Tile pMasterTile){
        Tile t = new Tile();
        Point newTopCorner = pSurface.get(0);
        Point firstSurfaceCorner = getTopCorner(pSurface);

        double unitOfWidth = pMasterTile.getWidth();
        double unitOfHeight = pMasterTile.getHeight();
        t.setSummits(rectangleInfoToSummits(newTopCorner, unitOfWidth, unitOfHeight));
        t.setMaterial(pMasterTile.getMaterial());
        return t;

    }

    /**
     * Prend en paramètre le coin gauche de la tuile la surface qui la contient et la tuile de base
     * retourne les 4 sommets qui fit dans la surface
     */
    public Tile  trimTile(List<Point> pSurface, Tile pMasterTile, Point topLeftCorner) {
        List<Point> temp = new ArrayList<>();

        Point A = new Point(0, 0);
        Point B = new Point(0, 0);
        Point C = new Point(0, 0);
        Point D = new Point(0, 0);

        boolean isTileOverflowX = false;
        boolean isTileOverflowY = false;
        boolean isTileUnderflowX = false;
        boolean isTileUnderflowY = false;

        double unitOfWidth = pMasterTile.getWidth();
        double unitOfHeight = pMasterTile.getHeight();

        //prendre les limites supérieur de la surface
        double rightSurfaceBound = pSurface.get(0).x + getWidth(pSurface);
        double bottomSurfaceBound = pSurface.get(0).y + getHeight(pSurface);

        double offsetX = Math.abs(topLeftCorner.x - pSurface.get(0).x);
        double offsetY = Math.abs(topLeftCorner.y - pSurface.get(0).y);

        //check si le coin est à lexterieur gauche de la surface
        if (topLeftCorner.x < pSurface.get(0).x){
            A.x = pSurface.get(0).x;
            C.x = pSurface.get(0).x;
            isTileUnderflowX = true;
        }else{
            A.x = topLeftCorner.x;
            C.x = topLeftCorner.x;
        }
        if (topLeftCorner.y < pSurface.get(0).y){
            A.y = pSurface.get(0).y;
            B.y = pSurface.get(0).y;
            isTileUnderflowY = true;
        }else{
            A.y = topLeftCorner.y;
            B.y = topLeftCorner.y;
        }

        if(topLeftCorner.x + pMasterTile.getWidth() > rightSurfaceBound){
            B.x =  A.x + rightSurfaceBound - topLeftCorner.x;
            D.x = C.x +rightSurfaceBound - topLeftCorner.x;
        }else if (!isTileUnderflowX){
            B.x = A.x + pMasterTile.getWidth();
            D.x = C.x + pMasterTile.getWidth();
        } else {
            B.x = A.x + pMasterTile.getWidth() - offsetX;
            D.x = C.x +pMasterTile.getWidth() - offsetX;
        }
        if(topLeftCorner.y + pMasterTile.getHeight() > bottomSurfaceBound){
            C.y = A.y + bottomSurfaceBound - topLeftCorner.y;
            D.y = B.y + bottomSurfaceBound - topLeftCorner.y;
        }else if (!isTileUnderflowY){
            C.y = A.y + pMasterTile.getHeight();
            D.y = B.y + pMasterTile.getHeight();
        } else {
            C.y = A.y + pMasterTile.getHeight() - offsetY;
            D.y = B.y + pMasterTile.getHeight() - offsetY;
        }

        temp.add(A);
        temp.add(B);
        temp.add(C);
        temp.add(D);
        Tile t = new Tile(temp, pMasterTile.getMaterial());
        return t;
    }

    public boolean checkExisting(List<Point> pOld, Point pPoint){
        boolean checker = true;
        for (Point check : pOld){
            if (pPoint.x == check.x
                    && pPoint.y == check.y){
                checker = false;
                break;
            }
        }
        return checker;
    }

    public boolean tileInBound(List<Point> pSurface, Tile pMasterTile, Point topLeftCorner){
        //prendre les limites supérieur de la surface
        double rightSurfaceBound = pSurface.get(0).x + getWidth(pSurface);
        double bottomSurfaceBound = pSurface.get(0).y + getHeight(pSurface);

        if (topLeftCorner.x + pMasterTile.getWidth() <= pSurface.get(0).x){
            return false;
        }
        if(topLeftCorner.y + pMasterTile.getHeight() <= pSurface.get(0).y){
            return false;
        }
        if(topLeftCorner.x >= rightSurfaceBound ){
            return false;
        }
        if (topLeftCorner.y >= bottomSurfaceBound){
            return false;
        }

        return true;
    }

    private double getHeight(List<Point> pSurface) {
        return ShapeHelper.getHeight(toAbstract(pSurface));
    }

    private double getWidth(List<Point> pSurface) {
        return ShapeHelper.getWidth(toAbstract(pSurface));
    }

    private AbstractShape toAbstract(List<Point> surfaceSummits) {
        return new AbstractShape(surfaceSummits);
    }

    /***
     * Other usefull fonctions
     */

    public Tile addSeal(Tile pTile, SealsInfo pSealInfo){
        double seal = pSealInfo.getWidth();
        double largeur = pTile.getWidth();
        double height = pTile.getHeight();
        double tempX = largeur + seal;
        double tempY =  height + seal;

        List<utils.Point> abstractSummits = RectangleHelper.rectangleInfoToSummits(new utils.Point(0, 0), tempX, tempY);
        return new Tile(abstractSummits.stream().map(s -> new Point(s.x, s.y)).collect(Collectors.toList()), pTile.getMaterial());
    }

    public static List<Point> rectangleInfoToSummits(Point topLeftCorner, double width, double height) {
        double x = topLeftCorner.x;
        double y = topLeftCorner.y;
        List<Point> temp = new ArrayList<>();
        double tempX = width + x;
        double tempY = height + y;

        Point tempA = new Point(topLeftCorner.x, topLeftCorner.y);
        Point tempB = new Point(tempX, topLeftCorner.y);
        Point tempC = new Point(topLeftCorner.x, tempY);
        Point tempD = new Point(tempX, tempY);
        temp.add(tempA);
        temp.add(tempB);
        temp.add(tempC);
        temp.add(tempD);

        return temp;
    }

    public Tile getFirstTile(List<Point> pSurface, Tile pMasterTile, SealsInfo pSeal){
        Tile t = new Tile();
        Point newTopCorner = pMasterTile.getSummits().get(0);
        Point firstSurfaceCorner = getTopCorner(pSurface);

        double unitOfWidth = pMasterTile.getWidth() + pSeal.getWidth();
        double unitOfHeight = pMasterTile.getHeight() + pSeal.getWidth();

        boolean notFirst = true;
        while (notFirst){
            Point temp;
            if (newTopCorner.x > firstSurfaceCorner.x){
                temp = Point.translate(newTopCorner, -unitOfWidth, 0);
            }else if (newTopCorner.y > firstSurfaceCorner.y){
                temp = Point.translate(newTopCorner, 0, -unitOfHeight);
            }else {
                temp = newTopCorner;
                notFirst = false;
            }
            newTopCorner = temp;
        }
        t.setSummits(rectangleInfoToSummits(newTopCorner, unitOfWidth, unitOfHeight));
        t.setMaterial(pMasterTile.getMaterial());
        return t;
    }

    public Point getTopCorner(List<Point> pSurface){
        Point smallestPts = pSurface.get(0);
        for (Point element : pSurface){
            if(element.x < smallestPts.x){
                smallestPts = element;
            }
        }
        for (Point element : pSurface){
            if(element.x == smallestPts.x){
                if(element.y < smallestPts.y){
                    smallestPts = element;
                }
            }
        }

        return smallestPts;
    }

    public Tile rotate90(Tile pMasterTile){
        Tile t = new Tile();
        List<Point> temp = new ArrayList<>();
        double unitOfWidth = pMasterTile.getWidth();
        double unitOfHeight = pMasterTile.getHeight();

        Point A = pMasterTile.getSummits().get(0);
        Point B = new Point(pMasterTile.getSummits().get(0).x + unitOfHeight,
                            pMasterTile.getSummits().get(0).y);

        Point C = new Point(pMasterTile.getSummits().get(0).x,
                            pMasterTile.getSummits().get(0).y + unitOfWidth);

        Point D = new Point(pMasterTile.getSummits().get(0).x + unitOfHeight,
                            pMasterTile.getSummits().get(0).y + unitOfWidth);

        temp.add(A);
        temp.add(B);
        temp.add(C);
        temp.add(D);
        t.setSummits(temp);
        t.setMaterial(pMasterTile.getMaterial());

        return t;
    }

    public boolean isInsideTile(List<Tile> pOutTiles, Tile pTile){

        for (Tile element : pOutTiles) {
            for(Point p : pTile.getSummits())
                if(p.x > element.getSummits().get(0).x &&
                        p.x < element.getSummits().get(3).x &&
                        p.y > element.getSummits().get(0).y &&
                        p.y > element.getSummits().get(3).y){
                    return false;
                }
        }

        return true;
    }
}





