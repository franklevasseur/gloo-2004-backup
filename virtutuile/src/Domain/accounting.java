package Domain;

import utils.Point;

import java.util.ArrayList;
import java.util.List;

public class accounting {

    private List<Surface> surfaces = new ArrayList<>();
    private Material material;
    private double usedTiles;
    private double totalCost;
    private double nbBoxes;

    public accounting(List<Surface> pSurfaces, Material pMaterials){
        this.material = pMaterials;
        this.surfaces = pSurfaces;
        usedTiles = 0;
        totalCost = 0;
        nbBoxes = 0;
    }

    public double getNbBoxes() {
        nbBoxes = material.getNbTilePerBox()/F_usedTiles();
        return Math.ceil(nbBoxes);
    }

    public double getTotalCost() {
        totalCost = material.getCostPerBox() * getNbBoxes();
        return totalCost;
    }

    public double getUsedTiles() {
        return F_usedTiles();
    }

    private double F_usedTiles(){

        for (Surface var : surfaces)
        {
            List<Tile> tiles = var.getTiles();
            if (tiles.get(0).getMaterial().getMaterialName() == material.getMaterialName()){
                usedTiles += tiles.size();
            }
        }
        return usedTiles;
    }

    private double TotalArea(){
        for ( Surface i: surfaces){
            //check si la surface est une surface fusionne
            if(i.isFusionned()){

            }else {

            }
        }
        return 2.0;
    }

    private double areaOfSurface(Surface pSurface){

        return 2.0;
    }

    private Surface surfaceSansTrou(FusionnedSurface pSurface){
        Surface trou = null;
        for ( Surface i: pSurface.getFusionnedSurfaces()){
            if (i.isHole() == HoleStatus.HOLE){
                trou = i;
            }
        }
        if(trou == null){
            //return pSurface;
        }else {
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

    private Surface surfaceTrou(Surface pSurface){

        return pSurface;
    }
}
