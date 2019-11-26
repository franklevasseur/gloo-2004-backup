package Domain;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private List<Point> summits = new ArrayList<>();
    private Material material;

    public Tile(){

    }

    public Tile(List<Point> pSummit, Material pMaterial){
        summits = pSummit;
        material = pMaterial;
    }

    public Tile(Measure pHauteur, Measure pLargeur, Material pMaterial){
        Measure tempX = new Measure();
        Measure tempY = new Measure();
        Point tempA = new Point(tempX, tempY);
        Point tempB = new Point(pLargeur, tempY);
        Point tempC = new Point(tempX, pHauteur);
        Point tempD = new Point(pLargeur, pHauteur);
        summits.add(tempA);
        summits.add(tempB);
        summits.add(tempC);
        summits.add(tempD);
        material = pMaterial;
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

        minX = summits.get(0).getX().getValue();
        maxX = summits.get(0).getX().getValue();
        for (Point i:summits){
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

    public Measure getOrientation(){
        Measure value = new Measure();
        return value;
    }

    public List<Point> getSummits() {
        return this.summits;
    }

    public void setSummits(ArrayList<Point> summits) {
        this.summits = summits;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setSummits(List<Point> summits) {
        this.summits = summits;
    }

}
