package application;

import Domain.HoleStatus;
import utils.Color;
import utils.Id;
import utils.Point;

import java.util.List;

public class SurfaceDto {
    public Id id;
    public boolean isRectangular;
    public HoleStatus isHole;
    public Color surfaceColor;
    public boolean isFusionned;
    public TileDto masterTile;
    public SealsInfoDto sealsInfoDto;

    public List<Point> summits; // summits in meters
    public List<TileDto> tiles;
    public List<SurfaceDto> fusionnedSurface;
}
