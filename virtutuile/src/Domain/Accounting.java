package Domain;

import utils.Point;

import java.util.ArrayList;
import java.util.List;

public class Accounting {

    private List<Surface> surfaces = new ArrayList<>();
    private Material material;
    private int usedTiles;
    private double totalCost;
    private int nbBoxes;

    public Accounting(List<Surface> pSurfaces, Material pMaterials) {
        this.material = pMaterials;
        this.surfaces = pSurfaces;
        usedTiles = 0;
        totalCost = 0;
        nbBoxes = 0;
    }


    public Material getMaterial() {
        return this.material;
    }

    public int getNbBoxes() {
        double divisionResult = ((double) usedTiles) / ((double) material.getNbTilePerBox());
        nbBoxes = (int) Math.ceil(divisionResult);
        return nbBoxes;
    }

    public double getTotalCost() {
        totalCost = material.getCostPerBox() * nbBoxes;
        return totalCost;
    }

    public int getUsedTiles() {
        return F_usedTiles();
    }

    private int F_usedTiles() {

        for (Surface var : surfaces) {
            List<Tile> tiles = var.getTiles();
            if (!(tiles.size() <= 0)) {
                if (tiles.get(0).getMaterial().getMaterialName() == material.getMaterialName()) {
                    usedTiles += tiles.size();
                }
            }

        }
        return usedTiles;
    }


    private double TotalArea() {
        for (Surface i : surfaces) {
            //check si la surface est une surface fusionne
            if (i.isFusionned()) {

            } else {

            }
        }
        return 2.0;
    }

    private double areaOfSurface(Surface pSurface) {

        return 2.0;
    }

    private Surface surfaceSansTrou(FusionnedSurface pSurface) {
        Surface trou = null;
        for (Surface i : pSurface.getFusionnedSurfaces()) {
            if (i.isHole() == HoleStatus.HOLE) {
                trou = i;
            }
        }
        if (trou == null) {
            //return pSurface;
        } else {
/**
 for(Point j: trou){
 for (Point k: pSurface.getFusionnedSurfaces() ) {

 if (...) {
 iter.remove();
 }
 }

 }
 */
        }
        return pSurface;
    }

    private Surface surfaceTrou(Surface pSurface) {

        return pSurface;
    }
}
