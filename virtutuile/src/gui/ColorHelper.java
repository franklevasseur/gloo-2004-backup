package gui;

import javafx.scene.paint.Color;

public class ColorHelper {

    public static Color utilsColorToJavafx(utils.Color utilsColor) {
        if (utilsColor == utils.Color.BLACK) {
            return Color.BLACK;
        }else if(utilsColor == utils.Color.WHITE){
            return Color.WHITE;
        }else if(utilsColor == utils.Color.YELLOW){
            return  Color.YELLOW;
        }else if(utilsColor == utils.Color.GREEN){
            return Color.GREEN;
        }else if(utilsColor == utils.Color.BLUE){
            return Color.BLUE;
        }else if(utilsColor == utils.Color.RED){
            return Color.RED;
        }else if(utilsColor == utils.Color.VIOLET){
            return Color.VIOLET;
        }
        return Color.WHITE;
    }

    public static String utilsColorToString(utils.Color utilsColor) {
        if (utilsColor == utils.Color.BLACK) {
            return "BLACK";
        }else if(utilsColor == utils.Color.WHITE){
            return "WHITE";
        }else if(utilsColor == utils.Color.YELLOW){
            return  "YELLOW";
        }else if(utilsColor == utils.Color.GREEN){
            return "GREEN";
        }else if(utilsColor == utils.Color.BLUE){
            return "BLUE";
        }else if(utilsColor == utils.Color.RED){
            return "RED";
        }else if(utilsColor == utils.Color.VIOLET){
            return "VIOLET";
        }
        return "WHITE";
    }

    public static utils.Color stringToUtils(String stringColor) {
        if (stringColor == "BLACK") {
            return utils.Color.BLACK;
        }else if(stringColor == "WHITE"){
            return utils.Color.WHITE;
        }else if(stringColor == "YELLOW"){
            return  utils.Color.YELLOW;
        }else if(stringColor == "GREEN"){
            return utils.Color.GREEN;
        }else if(stringColor == "BLUE"){
            return utils.Color.BLUE;
        }else if(stringColor == "RED"){
            return utils.Color.RED;
        }else if(stringColor == "VIOLET"){
            return utils.Color.VIOLET;
        }
        return utils.Color.WHITE;
    }
}
