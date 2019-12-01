package Domain;

import java.util.List;

public class InspectionService {

    private static InspectionService instance = new InspectionService();
    public static InspectionService getInstance() {
        return instance;
    }

    public String inspect(Project project, double pWidth, double pHeight) {
        String errorMessage = "Il y a :";
        int count = 0;
        for(Surface i: project.getSurfaces()){
            List<Tile> temp = i.getTiles();
            for (Tile j: temp){
                if (j.getWidth() < pWidth){
                    count ++;
                }else if (j.getHeight() < pHeight){
                    count ++;
                }
            }
        }
        errorMessage += Integer.toString(count) + " qui ne respecte pas les contraintes";
        return errorMessage;
    }

    public String inspect(Surface pSurface, double pWidth, double pHeight) {
        String errorMessage = "Il y a : ";
        int count = 0;
        List<Tile> temp = pSurface.getTiles();
            for (Tile j: temp) {
                if (j.getWidth() < pWidth) {
                    count++;
                } else if (j.getHeight() < pHeight) {
                    count++;
                }
            }
        errorMessage += Integer.toString(count) + " qui ne respecte pas les contraintes";
        return errorMessage;
    }
}
