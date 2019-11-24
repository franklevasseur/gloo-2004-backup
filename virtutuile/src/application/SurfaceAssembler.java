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
        dto.summits = surface.getSummits().stream().map(s -> {
            double x = s.getX().getValue();
            double y = s.getY().getValue();
            return new Point(x, y);
        }).collect(Collectors.toList());
        dto.id = surface.getId();
        dto.isRectangular = surface.getIsRectangular();
        dto.isFusionned = surface.isFusionned();
        dto.tiles = surface.getTiles().stream().map(t -> {
            TileDto tileDto = new TileDto();
            tileDto.summits = t.getSummits().stream().map(p -> {
                double x = p.getX().getValue();
                double y = p.getY().getValue();
                return new Point(x, y);
            }).collect(Collectors.toList());
            return tileDto;
        }).collect(Collectors.toList());

        if (surface.isFusionned()) {
            dto.fusionnedSurface = ((FusionnedSurface) surface).getFusionnedSurfaces().stream().map(fs -> toDto(fs)).collect(Collectors.toList());
        }

        return dto;
    }

    public static void fromDto (SurfaceDto dto, Surface destinationSurface){

        // TODO: aller chercher sealsInfo dans le dto
        List<Domain.Point> summits = dto.summits.stream().map(s -> {
            Measure xMeasure = new Measure(s.x, UnitType.m);
            Measure yMeasure = new Measure(s.y, UnitType.m);
            return new Domain.Point(xMeasure, yMeasure);
        }).collect(Collectors.toList());

        if (dto.tiles != null) {
            List<Tile> tiles = dto.tiles.stream().map(tDto -> {
                List<Domain.Point> points = tDto.summits.stream().map(p -> {
                    Measure xMeasure = new Measure(p.x, UnitType.m);
                    Measure yMeasure = new Measure(p.y, UnitType.m);
                    return new Domain.Point(xMeasure, yMeasure);
                }).collect(Collectors.toList());

                // TODO: pass the actual material in the dto
                return new Tile(points, new Material(new Color(), MaterialType.tileMaterial));
            }).collect(Collectors.toList());

            destinationSurface.setTiles(tiles);
        }

        destinationSurface.setHole(dto.isHole);
        destinationSurface.setSummits(summits);
        destinationSurface.setIsRectangular(dto.isRectangular);
    }

    public static Surface fromDto (SurfaceDto dto) {
        Surface surface = new Surface(dto.isHole, new ArrayList<Domain.Point>(), dto.isRectangular);
        SurfaceAssembler.fromDto(dto, surface);
        return surface;
    }
}
