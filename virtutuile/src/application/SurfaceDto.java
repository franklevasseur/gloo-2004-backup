package application;

import utils.Id;
import utils.Point;

import java.util.List;

public class SurfaceDto {
    public Id id;
    public boolean isRectangular;
    public boolean isHole;

    public List<Point> summits; // summits in meters
    public List<TileDto> tiles;
}
