package application;

import Domain.Material;

public class MaterialAssembler {

    public MaterialDto toDto(Material material) {
        MaterialDto dto = new MaterialDto();

        dto.color = material.getColor();
        dto.name = material.getMaterialName();
        dto.costPerBox = material.getCostPerBox();
        dto.nbTilePerBox = material.getNbTilePerBox();
        dto.tileTypeHeight = material.getTileTypeHeight();
        dto.tileTypeWidth = material.getTileTypeWidth();

        return dto;
    }

    public Material fromDto(MaterialDto dto, Material material) {
        material.setColor(dto.color);
        material.setCostPerBox(dto.costPerBox);
        material.setNbTilePerBox(dto.nbTilePerBox);
        material.setTileTypeHeight(dto.tileTypeHeight);
        material.setTileTypeWidth(dto.tileTypeWidth);
        return material;
    }

    public Material fromDto(MaterialDto dto) {
        return new Material(dto.color, dto.name, dto.nbTilePerBox, dto.costPerBox, dto.tileTypeWidth, dto.tileTypeHeight);
    }
}
