package application;

import Domain.*;
import utils.Point;
import utils.RectangleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceAssembler {

    public static SurfaceDto toDto (Surface surface) {
        if (surface == null) {
            return null;
        }

        SurfaceDto dto = new SurfaceDto();

        dto.isHole = surface.isHole();
        dto.summits = surface.getSummits();
        dto.id = surface.getId();
        dto.isRectangular = surface.getIsRectangular();
        dto.isFusionned = surface.isFusionned();
        dto.tiles = surface.getTiles().stream().map(t -> toDto(t)).collect(Collectors.toList());
        dto.pattern = surface.getPattern();

        dto.tileAngle = surface.getTileAngle();

        if (surface.getMasterTile() != null) {
            dto.masterTile = toDto(surface.getMasterTile());
        }
        if (surface.getSealsInfo() != null) {
            dto.sealsInfoDto = toDto(surface.getSealsInfo());
        }

        if (surface.isFusionned()) {
            dto.fusionnedSurface = ((FusionnedSurface) surface).getFusionnedSurfaces().stream().map(fs -> toDto(fs)).collect(Collectors.toList());
        }

        return dto;
    }

    public static void fromDto (SurfaceDto dto, Surface destinationSurface) {

        List<Point> summits = dto.summits;

        if (dto.tiles != null) {
            List<Tile> tiles = dto.tiles.stream().map(tDto -> fromDto(tDto)).collect(Collectors.toList());
            destinationSurface.setTiles(tiles);
        }

        destinationSurface.setHole(dto.isHole);
        destinationSurface.setSummits(summits);
        destinationSurface.setIsRectangular(dto.isRectangular);
        destinationSurface.setTileAngle(dto.tileAngle);
        if (dto.masterTile != null) {
            destinationSurface.setMasterTile(fromDto(dto.masterTile));
        }
        if (dto.sealsInfoDto != null) {
            destinationSurface.setSealsInfo(fromDto(dto.sealsInfoDto));
        }
        if (dto.pattern != null) {
            destinationSurface.setPattern(dto.pattern);
        }

        if (dto.isFusionned) {
            FusionnedSurface fusionnedSurface = (FusionnedSurface) destinationSurface;
            List<Surface> innerSurfaces = dto.fusionnedSurface.stream().map(s -> fromDto(s)).collect(Collectors.toList());
            fusionnedSurface.setFusionnedSurfaces(innerSurfaces);
        }
    }

    public static Surface fromDto (SurfaceDto dto) {
        if (dto == null) {
            return null;
        }
        Surface surface = new Surface(dto.isHole, new ArrayList<Point>(), dto.isRectangular);
        SurfaceAssembler.fromDto(dto, surface);
        return surface;
    }

    public static TileDto toDto(Tile tile) {
        if (tile == null) {
            return null;
        }

        TileDto tileDto = new TileDto();
        tileDto.summits = tile.getSummits();
        tileDto.material = MaterialAssembler.toDto(tile.getMaterial());
        tileDto.isCut = tile.isCut();
        return tileDto;
    }

    public static Tile fromDto(TileDto tDto) {
        if (tDto == null) {
            return null;
        }

        List<Point> points = tDto.summits;

        Material material = MaterialAssembler.fromDto(tDto.material);
        return new Tile(points, material, tDto.isCut);
    }

    public static SealsInfoDto toDto(SealsInfo sealsInfo) {
        if (sealsInfo == null) {
            return null;
        }

        SealsInfoDto dto = new SealsInfoDto();
        dto.sealWidth = sealsInfo.getWidth();
        dto.color = sealsInfo.getColor();
        return dto;
    }

    public static SealsInfo fromDto(SealsInfoDto sDto) {
        if (sDto == null) {
            return null;
        }

        double width = sDto.sealWidth;
        SealsInfo sealsInfo = new SealsInfo(width, sDto.color);
        return  sealsInfo;
    }
}
