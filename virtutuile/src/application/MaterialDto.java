package application;

import Domain.MaterialType;
import utils.Color;
import utils.Id;

public class MaterialDto {
    public String name;
    public Color color;
    public MaterialType materialType;
    public int nbTilePerBox;
    public double costPerBox;
    public double tileTypeWidth;
    public double tileTypeHeight;
}
