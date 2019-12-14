package application;

import Domain.*;
import utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceAssembler {

    private TileAssembler tileAssembler;
    private SealingAssembler sealingAssembler;

    public SurfaceAssembler(TileAssembler tileAssembler, SealingAssembler sealingAssembler) {
        this.tileAssembler = tileAssembler;
        this.sealingAssembler = sealingAssembler;
    }

    public SurfaceDto toDto (Surface surface) {
        if (surface == null) {
            return null;
        }

        SurfaceDto dto = new SurfaceDto();

        dto.isHole = surface.isHole();
        dto.summits = surface.getSummits();
        dto.id = surface.getId();
        dto.isRectangular = surface.getIsRectangular();
        dto.isFusionned = surface.isFusionned();
        dto.tiles = surface.getTiles().stream().map(t -> tileAssembler.toDto(t)).collect(Collectors.toList());
        dto.pattern = surface.getPattern();
        dto.surfaceColor = surface.getSurfaceColor();

        dto.tileAngle = surface.getTileAngle();
        dto.tileShifting = surface.getTileShifting();

        if (surface.getMasterTile() != null) {
            dto.masterTile = tileAssembler.toDto(surface.getMasterTile());
        }
        if (surface.getSealsInfo() != null) {
            dto.sealsInfoDto = sealingAssembler.toDto(surface.getSealsInfo());
        }

        if (surface.isFusionned()) {
            dto.fusionnedSurface = ((FusionnedSurface) surface).getFusionnedSurfaces().stream().map(fs -> toDto(fs)).collect(Collectors.toList());
        }

        return dto;
    }

    public Surface fromDto (SurfaceDto dto, Surface destinationSurface) {

        List<Point> summits = dto.summits;

        if (dto.tiles != null) {
            List<Tile> tiles = dto.tiles.stream().map(tDto -> tileAssembler.fromDto(tDto)).collect(Collectors.toList());
            destinationSurface.setTiles(tiles);
        }

        destinationSurface.setHole(dto.isHole);
        destinationSurface.setSummits(summits);
        destinationSurface.setIsRectangular(dto.isRectangular);
        destinationSurface.setTileAngle(dto.tileAngle);
        destinationSurface.setTileShifting(dto.tileShifting);
        destinationSurface.setSurfaceColor(dto.surfaceColor);
        if (dto.masterTile != null) {
            destinationSurface.setMasterTile(tileAssembler.fromDto(dto.masterTile));
        }
        if (dto.sealsInfoDto != null) {
            destinationSurface.setSealsInfo(sealingAssembler.fromDto(dto.sealsInfoDto));
        }
        if (dto.pattern != null) {
            destinationSurface.setPattern(dto.pattern);
        }

        if (dto.isFusionned) {
            FusionnedSurface fusionnedSurface = (FusionnedSurface) destinationSurface;
            List<Surface> innerSurfaces = dto.fusionnedSurface.stream().map(s -> fromDto(s)).collect(Collectors.toList());
            fusionnedSurface.setFusionnedSurfaces(innerSurfaces);
        }

        return destinationSurface;
    }

    public Surface fromDto (SurfaceDto dto) {
        if (dto == null) {
            return null;
        }
        Surface surface = new Surface(dto.id, dto.isHole, new ArrayList<Point>(), dto.isRectangular);
        this.fromDto(dto, surface);
        return surface;
    }
}
