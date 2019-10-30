package Domain;

import java.util.ArrayList;

public class Tile {
    private ArrayList<Point> summit;
    private Material material;

    public Tile(ArrayList<Point> pSummit, Material pMaterial){
        summit = pSummit;
        material = pMaterial;
    }

    public Measure getHeight(){
        Measure value = new Measure();
        double minY;
        double maxY;

        minY = summit.get(0).getY().getValue();
        maxY = summit.get(0).getY().getValue();
        for (Point i:summit){
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

        minX = summit.get(0).getY().getValue();
        maxX = summit.get(0).getY().getValue();
        for (Point i:summit){
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
}
