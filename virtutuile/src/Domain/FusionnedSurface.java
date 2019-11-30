package Domain;

import utils.AbstractShape;
import utils.FusionHelper;
import utils.Point;

import java.util.List;
import java.util.stream.Collectors;

public class FusionnedSurface extends Surface {

    private List<Surface> fusionnedSurfaces;

    public FusionnedSurface(List<Surface> fusionnedSurfaces) {
        super(HoleStatus.NONE, extractResultantSummits(fusionnedSurfaces), false);
        this.fusionnedSurfaces = fusionnedSurfaces;
    }

    public List<Surface> getFusionnedSurfaces() {
        return this.fusionnedSurfaces;
    }

    @Override
    public boolean isFusionned() {
        return true;
    }

    public void setFusionnedSurfaces(List<Surface> surfaces) {
        this.fusionnedSurfaces = surfaces;
    }

    private static List<Point> extractResultantSummits(List<Surface> fusionnedSurfaces) {
        return FusionHelper.getFusionResultSummits(extractAllInnerSurfacesPoints(fusionnedSurfaces)).summits;
    }

    private static List<AbstractShape> extractAllInnerSurfacesPoints(List<Surface> fusionnedSurfaces) {
        return fusionnedSurfaces
                .stream().map(s -> new AbstractShape(s.getSummits(), s.isHole() == HoleStatus.HOLE))
                .collect(Collectors.toList());
    }
}
