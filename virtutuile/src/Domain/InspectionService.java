package Domain;

import utils.Color;
import utils.pairResult;

import java.util.ArrayList;
import java.util.List;

public class InspectionService {

    private static InspectionService instance = new InspectionService();
    public static InspectionService getInstance() {
        return instance;
    }

    public pairResult inspect(Project project, double pWidth, double pHeight) {
        String errorMessage = "Il y a :";
        int count = 0;
        for(Surface i: project.getSurfaces()){
            List<Tile> temp = i.getTiles();

            List<Tile> badTiles = new ArrayList<>();
            for (Tile j: temp){
                if (j.getWidth() < pWidth){
                    badTiles.add(j);
                    count ++;
                }else if (j.getHeight() < pHeight){
                    badTiles.add(j);
                    count ++;
                }
            }
            for (Tile k: badTiles){
                Material badMat = k.getMaterial();
                badMat.setColor(Color.RED);
                Tile badTile = k;
                badTile.setMaterial(badMat);
                temp.remove(k);
                temp.add(badTile);
            }
            i.setTiles(temp);
        }
        errorMessage += Integer.toString(count) + " qui ne respecte pas les contraintes";
        pairResult t = new pairResult(project, errorMessage);

        return t;
    }

    public String inspect(Surface pSurface, double pWidth, double pHeight) {
        // TODO: surlignage des tuiles pas bonne est pas implémente la dedans
        String errorMessage = "Il y a : ";
        int count = 0;

        List<Tile> temp = pSurface.getTiles();

        List<Tile> badTiles = new ArrayList<>();
        for (Tile j: temp){
            if (j.getWidth() < pWidth){
                badTiles.add(j);
                count ++;
            }else if (j.getHeight() < pHeight){
                badTiles.add(j);
                count ++;
            }
        }
        for (Tile k: badTiles){
            Material badMat = k.getMaterial();
            badMat.setColor(Color.RED);
            Tile badTile = k;
            badTile.setMaterial(badMat);
            temp.remove(k);
            temp.add(badTile);
        }
        pSurface.setTiles(temp);

        errorMessage += Integer.toString(count) + " qui ne respecte pas les contraintes";

        return errorMessage;
    }
}
