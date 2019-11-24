package application;

import utils.Color;
import utils.Id;
import utils.Point;
import utils.Id;

import java.util.List;

public class SurfaceDto {
    public Id id;
    public boolean isRectangular;
    public boolean isHole;
    public Color surfaceColor;
    public boolean isFusionned;

    public List<Point> summits; // summits in meters
    public List<TileDto> tiles;
    public List<SurfaceDto> fusionnedSurface;
}
