package application;

import Domain.Material;

public class MaterialAssembler {

    public static MaterialDto toDto (Material material){
        MaterialDto dto = new MaterialDto();

        dto.color = material.getColor();
        dto.name = material.getMaterialName();
        dto.costPerBox = material.getCostPerBox();
        dto.nbTilePerBox = material.getNbTilePerBox();
        dto.tileTypeHeight = material.getTileTypeHeight();
        dto.tileTypeWidth = material.getTileTypeWidth();

        return dto;
    }

    public static Material fromDto (MaterialDto dto){
        Material material = new Material(dto.color, dto.name, dto.nbTilePerBox, dto.costPerBox, dto.tileTypeWidth, dto.tileTypeHeight);
        return material;
    }
}
