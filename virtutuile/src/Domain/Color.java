package Domain;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Color {
    private int red;
    private int blue;
    private int green;

    public Color(){

    }

    public Color(int pRed, int pGreen, int pBlue){
        red = pRed;
        green = pGreen;
        blue = pBlue;
    }

    public int getBlue() {
        return blue;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setRed(int red) {
        this.red = red;
    }

    /**
     *
     * @return une list des valeurs de couleur (rouge, vert, bleu)
     */
    public ArrayList<Integer> getAttributs(){
        ArrayList<Integer> value = new ArrayList<>();
        value.add(red);
        value.add(green);
        value.add(blue);
        return value;
    }

    /**
     *
     * @param pColor est une liste de int qui prend (rouge, vert, bleu) a l'interieur
     */
    public void setAttributs(ArrayList<Integer> pColor){
        this.red = pColor.get(0);
        this.green = pColor.get(1);
        this.blue = pColor.get(2);
    }
}
