package application;

import Domain.Material;
import Domain.MaterialType;
import utils.Color;

public class MaterialAssembler {

    public static MaterialDto toDto (Material material){

        MaterialDto dto = new MaterialDto();

        dto.color = material.getColor();
        dto.materialType = material.getMaterialType();
        dto.name = material.getMaterialName();

        return dto;
    }

    public static Material fromDto (MaterialDto dto){
        Material material = new Material(dto.color, dto.materialType,dto.name);
        return material;
    }
}
