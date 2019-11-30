package Domain;


import java.util.ArrayList;
import java.util.List;

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
        } else {

            for (int line = 0; line < amountOfLines; line++) {
                for (int column = 0; column < amountOfColumns; column++) {
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
        double unitOfWidth = pMasterTile.getWidth().getValue() + pSealsInfo.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSealsInfo.getWidth().getValue();

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
        double unitOfWidth = pMasterTile.getWidth().getValue() + pSealsInfo.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSealsInfo.getWidth().getValue();

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

        double unitOfWidth = pMasterTile.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue();
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

        Point A = new Point(new Measure(0), new Measure(0));
        Point B = new Point(new Measure(0), new Measure(0));
        Point C = new Point(new Measure(0), new Measure(0));
        Point D = new Point(new Measure(0), new Measure(0));

        boolean isTileOverflowX = false;
        boolean isTileOverflowY = false;
        boolean isTileUnderflowX = false;
        boolean isTileUnderflowY = false;

        double unitOfWidth = pMasterTile.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue();

        //prendre les limites supérieur de la surface
        double rightSurfaceBound = pSurface.get(0).getX().getValue() + getWidth(pSurface).getValue();
        double bottomSurfaceBound = pSurface.get(0).getY().getValue() + getHeight(pSurface).getValue();

        double offsetX = Math.abs(topLeftCorner.getX().getValue() - pSurface.get(0).getX().getValue());
        double offsetY = Math.abs(topLeftCorner.getY().getValue() - pSurface.get(0).getY().getValue());

        //check si le coin est à lexterieur gauche de la surface
        if (topLeftCorner.getX().getValue() < pSurface.get(0).getX().getValue()){
            A.setX(pSurface.get(0).getX());
            C.setX(pSurface.get(0).getX());
            isTileUnderflowX = true;
        }else{
            A.setX(topLeftCorner.getX());
            C.setX(topLeftCorner.getX());
        }
        if (topLeftCorner.getY().getValue() < pSurface.get(0).getY().getValue()){
            A.setY(pSurface.get(0).getY());
            B.setY(pSurface.get(0).getY());
            isTileUnderflowY = true;
        }else{
            A.setY(topLeftCorner.getY());
            B.setY(topLeftCorner.getY());
        }

        if(topLeftCorner.getX().getValue() + pMasterTile.getWidth().getValue() > rightSurfaceBound){
            B.setX(new Measure( A.getX().getValue() + rightSurfaceBound - topLeftCorner.getX().getValue()));
            D.setX(new Measure(C.getX().getValue() +rightSurfaceBound - topLeftCorner.getX().getValue()));
        }else if (!isTileUnderflowX){
            B.setX(new Measure(A.getX().getValue() + pMasterTile.getWidth().getValue()));
            D.setX(new Measure(C.getX().getValue() + pMasterTile.getWidth().getValue()));
        } else {
            B.setX(new Measure(A.getX().getValue() + pMasterTile.getWidth().getValue() - offsetX));
            D.setX(new Measure(C.getX().getValue() +pMasterTile.getWidth().getValue() - offsetX));
        }
        if(topLeftCorner.getY().getValue() + pMasterTile.getHeight().getValue() > bottomSurfaceBound){
            C.setY(new Measure(A.getY().getValue() + bottomSurfaceBound - topLeftCorner.getY().getValue() ));
            D.setY(new Measure(B.getY().getValue() + bottomSurfaceBound - topLeftCorner.getY().getValue() ));
        }else if (!isTileUnderflowY){
            C.setY(new Measure(A.getY().getValue() + pMasterTile.getHeight().getValue()));
            D.setY(new Measure(B.getY().getValue() + pMasterTile.getHeight().getValue()));
        } else {
            C.setY(new Measure(A.getY().getValue() + pMasterTile.getHeight().getValue() - offsetY));
            D.setY(new Measure(B.getY().getValue() + pMasterTile.getHeight().getValue() - offsetY));
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
            if (pPoint.getX().getValue() == check.getX().getValue()
                    && pPoint.getY().getValue() == check.getY().getValue()){
                checker = false;
                break;
            }
        }
        return checker;
    }

    public boolean tileInBound(List<Point> pSurface, Tile pMasterTile, Point topLeftCorner){
        //prendre les limites supérieur de la surface
        double rightSurfaceBound = pSurface.get(0).getX().getValue() + getWidth(pSurface).getValue();
        double bottomSurfaceBound = pSurface.get(0).getY().getValue() + getHeight(pSurface).getValue();

        if (topLeftCorner.getX().getValue() + pMasterTile.getWidth().getValue() <= pSurface.get(0).getX().getValue()){
            return false;
        }
        if(topLeftCorner.getY().getValue() + pMasterTile.getHeight().getValue() <= pSurface.get(0).getY().getValue()){
            return false;
        }
        if(topLeftCorner.getX().getValue() >= rightSurfaceBound ){
            return false;
        }
        if (topLeftCorner.getY().getValue() >= bottomSurfaceBound){
            return false;
        }

        return true;
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

    public Tile getFirstTile(List<Point> pSurface, Tile pMasterTile, SealsInfo pSeal){
        Tile t = new Tile();
        Point newTopCorner = pMasterTile.getSummits().get(0);
        Point firstSurfaceCorner = getTopCorner(pSurface);

        double unitOfWidth = pMasterTile.getWidth().getValue() + pSeal.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue() + pSeal.getWidth().getValue();

        boolean notFirst = true;
        while (notFirst){
            Point temp;
            if (newTopCorner.getX().getValue() > firstSurfaceCorner.getX().getValue()){
                temp = Point.translate(newTopCorner, -unitOfWidth, 0);
            }else if (newTopCorner.getY().getValue() > firstSurfaceCorner.getY().getValue()){
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
            if(element.getX().getValue() < smallestPts.getX().getValue()){
                smallestPts = element;
            }
        }
        for (Point element : pSurface){
            if(element.getX().getValue() == smallestPts.getX().getValue()){
                if(element.getY().getValue() < smallestPts.getY().getValue()){
                    smallestPts = element;
                }
            }
        }

        return smallestPts;
    }

    public Tile rotate90(Tile pMasterTile){
        Tile t = new Tile();
        List<Point> temp = new ArrayList<>();
        double unitOfWidth = pMasterTile.getWidth().getValue();
        double unitOfHeight = pMasterTile.getHeight().getValue();

        Point A = pMasterTile.getSummits().get(0);
        Point B = new Point(new Measure(pMasterTile.getSummits().get(0).getX().getValue() + unitOfHeight),
                            new Measure(pMasterTile.getSummits().get(0).getY().getValue()));

        Point C = new Point(new Measure(pMasterTile.getSummits().get(0).getX().getValue()),
                            new Measure(pMasterTile.getSummits().get(0).getY().getValue() + unitOfWidth));

        Point D = new Point(new Measure(pMasterTile.getSummits().get(0).getX().getValue() + unitOfHeight),
                            new Measure(pMasterTile.getSummits().get(0).getY().getValue() + unitOfWidth));

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
                if(p.getX().getValue() > element.getSummits().get(0).getX().getValue() &&
                        p.getX().getValue() < element.getSummits().get(3).getX().getValue() &&
                        p.getY().getValue() > element.getSummits().get(0).getY().getValue() &&
                        p.getY().getValue() > element.getSummits().get(3).getY().getValue()){
                    return false;
                }
        }

        return true;
    }
}





