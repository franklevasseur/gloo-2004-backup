package application;

import Domain.*;
import utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceAssembler {

    public static SurfaceDto toDto (Surface surface){

        SurfaceDto dto = new SurfaceDto();

        dto.isHole = surface.isHole();
        dto.summits = surface.getSummits();
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

        List<Point> summits = dto.summits;

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
        Surface surface = new Surface(dto.isHole, new ArrayList<Point>(), dto.isRectangular);
        SurfaceAssembler.fromDto(dto, surface);
        return surface;
    }

    public static TileDto toDto(Tile tile) {
        TileDto tileDto = new TileDto();
        tileDto.summits = tile.getSummits();
        tileDto.material = MaterialAssembler.toDto(tile.getMaterial());
        return tileDto;
    }

    public static Tile fromDto(TileDto tDto) {
        List<Point> points = tDto.summits.stream().map(p -> {
            double xdouble = p.x;
            double ydouble = p.y;
            return new Point(xdouble, ydouble);
        }).collect(Collectors.toList());

        Material material = MaterialAssembler.fromDto(tDto.material);
        return new Tile(points, material);
    }

    public static SealsInfoDto toDto(SealsInfo sealsInfo) {
        SealsInfoDto dto = new SealsInfoDto();
        dto.sealWidth = sealsInfo.getWidth();
        dto.color = sealsInfo.getColor();
        return dto;
    }

    public static SealsInfo fromDto(SealsInfoDto sDto) {
        double width = sDto.sealWidth;
        SealsInfo sealsInfo = new SealsInfo(width, sDto.color);
        return  sealsInfo;
    }
}
