package Domain;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private List<Point> summits;
    private Material material;

    public Tile(List<Point> pSummit, Material pMaterial){
        summits = pSummit;
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
}
