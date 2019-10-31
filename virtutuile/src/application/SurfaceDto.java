package application;

import utils.Point;
import utils.Id;

import java.util.List;

public class SurfaceDto {
    public Id id;
    public boolean isRectangular;
    public boolean isHole;
    //public SealsInfo sealsInfo;
    public List<Point> summits; // summits in meters
    public List<TileDto> tiles;
}
