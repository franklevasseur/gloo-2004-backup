package Domain;

import utils.Color;

import java.util.ArrayList;
import java.util.List;

public class InspectionService {

    private static InspectionService instance = new InspectionService();
    public static InspectionService getInstance() {
        return instance;
    }

    public String inspect(Project project, double pWidth, double pHeight) {
        int count = 0;
        for(Surface i: project.getSurfaces()) {
            count += inspectOneSurface(i, pWidth, pHeight);
        }
        return String.format("There is %d tiles that do not respect the minimal cut length.", count);
    }

    private int inspectOneSurface(Surface surface, double pWidth, double pHeight) {
        List<Tile> temp = surface.getTiles();

        int count = 0;
        List<Tile> badTiles = new ArrayList<>();
        for (Tile j: temp){
            if (j.getWidth() < pWidth || j.getHeight() < pHeight) {
                badTiles.add(j);
                j.getMaterial().setColor(Color.RED);
                count ++;
            }
        }
        surface.setTiles(temp);
        return count;
    }
}
