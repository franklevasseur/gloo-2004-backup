package application;

import Domain.Material;

public class MaterialAssembler {

    public static MaterialDto toDto (Material material){

        MaterialDto dto = new MaterialDto();

        dto.color = material.getColor();
        dto.materialType = material.getMaterialType();
        dto.id = material.getId();

        return dto;
    }

    public static Material fromDto (MaterialDto dto){
        Material material = new Material(dto.color, dto.materialType);
        return material;
    }
}
