package Domain;

import utils.AbstractShape;
import utils.Point;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Tile {
    private List<Point> summits = new ArrayList<>();
    private Material material;

    public Tile(){

    }

    public Tile(List<Point> pSummit, Material pMaterial){
        summits = pSummit;
        material = pMaterial;
    }

    public double getHeight() {
        return ShapeHelper.getHeight(toAbstractShape());
    }

    private AbstractShape toAbstractShape() {
        return new AbstractShape(summits);
    }

    public double getWidth() {
        return ShapeHelper.getWidth(toAbstractShape());
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
