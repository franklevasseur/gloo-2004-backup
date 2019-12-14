package application;

import Domain.Material;
import Domain.Tile;
import utils.Point;

import java.util.List;
import java.util.Optional;

public class TileAssembler {

    private MaterialAssembler materialAssembler;
    private MaterialService materialService;

    public TileAssembler(MaterialAssembler materialAssembler, MaterialService materialService) {
        this.materialAssembler = materialAssembler;
        this.materialService = materialService;
    }

    public TileDto toDto(Tile tile) {
        if (tile == null) {
            return null;
        }

        TileDto tileDto = new TileDto();
        tileDto.summits = tile.getSummits();
        tileDto.material = materialAssembler.toDto(tile.getMaterial());
        tileDto.isMasterTile = tile.isMasterTile();
        return tileDto;
    }

    public Tile fromDto(TileDto tDto) {
        if (tDto == null) {
            return null;
        }

        List<Point> points = tDto.summits;

        MaterialDto materialDto = tDto.material;

        Optional<Material> optionalMaterial = materialService.getMaterialByName(materialDto.name);
        Material material = optionalMaterial.isEmpty() ? materialAssembler.fromDto(materialDto) : materialAssembler.fromDto(materialDto, optionalMaterial.get());
        return new Tile(points, material, tDto.isMasterTile);
    }
}
