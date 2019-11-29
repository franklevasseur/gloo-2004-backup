package application;

import Domain.*;
import utils.Color;
import utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceAssembler {

    public static SurfaceDto toDto (Surface surface){

        SurfaceDto dto = new SurfaceDto();

        dto.isHole = surface.isHole();
        dto.summits = surface.getSummits().stream().map(s -> {
            double x = s.getX().getValue();
            double y = s.getY().getValue();
            return new Point(x, y);
        }).collect(Collectors.toList());
        dto.id = surface.getId();
        dto.isRectangular = surface.getIsRectangular();
        dto.isFusionned = surface.isFusionned();
        dto.tiles = surface.getTiles().stream().map(t -> toDto(t)).collect(Collectors.toList());

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

    public static void fromDto (SurfaceDto dto, Surface destinationSurface){

        List<Domain.Point> summits = dto.summits.stream().map(s -> {
            Measure xMeasure = new Measure(s.x, UnitType.m);
            Measure yMeasure = new Measure(s.y, UnitType.m);
            return new Domain.Point(xMeasure, yMeasure);
        }).collect(Collectors.toList());

        if (dto.tiles != null) {
            List<Tile> tiles = dto.tiles.stream().map(tDto -> fromDto(tDto)).collect(Collectors.toList());
            destinationSurface.setTiles(tiles);
        }

        destinationSurface.setHole(dto.isHole);
        destinationSurface.setSummits(summits);
        destinationSurface.setIsRectangular(dto.isRectangular);
        if (dto.masterTile != null) {
            destinationSurface.setMasterTile(fromDto(dto.masterTile));
        }
        if (dto.sealsInfoDto != null) {
            destinationSurface.setSealsInfo(fromDto(dto.sealsInfoDto));
        }

        if (dto.isFusionned) {
            FusionnedSurface fusionnedSurface = (FusionnedSurface) destinationSurface;
            List<Surface> innerSurfaces = dto.fusionnedSurface.stream().map(s -> fromDto(s)).collect(Collectors.toList());
            fusionnedSurface.setFusionnedSurfaces(innerSurfaces);
        }
    }

    public static Surface fromDto (SurfaceDto dto) {
        Surface surface = new Surface(dto.isHole, new ArrayList<Domain.Point>(), dto.isRectangular);
        SurfaceAssembler.fromDto(dto, surface);
        return surface;
    }

    public static TileDto toDto(Tile tile) {
        TileDto tileDto = new TileDto();
        tileDto.summits = tile.getSummits().stream().map(p -> {
            double x = p.getX().getValue();
            double y = p.getY().getValue();
            return new Point(x, y);
        }).collect(Collectors.toList());
        tileDto.material = MaterialAssembler.toDto(tile.getMaterial());
        return tileDto;
    }

    public static Tile fromDto(TileDto tDto) {
        List<Domain.Point> points = tDto.summits.stream().map(p -> {
            Measure xMeasure = new Measure(p.x, UnitType.m);
            Measure yMeasure = new Measure(p.y, UnitType.m);
            return new Domain.Point(xMeasure, yMeasure);
        }).collect(Collectors.toList());

        Material material = MaterialAssembler.fromDto(tDto.material);
        return new Tile(points, material);
    }

    public static SealsInfoDto toDto(SealsInfo sealsInfo) {
        SealsInfoDto dto = new SealsInfoDto();
        dto.sealWidth = sealsInfo.getWidth().getValue();
        dto.color = sealsInfo.getColor();
        return dto;
    }

    public static SealsInfo fromDto(SealsInfoDto sDto) {
        Measure width = new Measure(sDto.sealWidth);
        SealsInfo sealsInfo = new SealsInfo(width, sDto.color);
        return  sealsInfo;
    }
}
