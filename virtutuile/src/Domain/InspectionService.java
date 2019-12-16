package Domain;

import utils.Color;

public class InspectionService {

    public String inspect(Project project, double pWidth, double pHeight) {
        int count = 0;
        for (Surface surface : project.getSurfaces()) {
            count += inspectOneSurface(surface, pWidth, pHeight);
        }
        return String.format("There is %d tiles that do not respect the minimal cut length.", count);
    }

    private int inspectOneSurface(Surface surface, double pWidth, double pHeight) {
        int count = 0;
        for (Tile tile : surface.getTiles()) {
            if (tile.getWidth() < pWidth || tile.getHeight() < pHeight) {
                Material material = tile.getMaterial().deepCopy();
                material.setColor(Color.RED);
                tile.setMaterial(material);
                count++;
            }
        }
        return count;
    }
}
