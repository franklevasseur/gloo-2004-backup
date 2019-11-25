package Domain;

import javafx.scene.shape.*;
import utils.FusionHelper;

import java.util.List;
import java.util.stream.Collectors;

public class FusionnedSurface extends Surface {

    private List<Surface> fusionnedSurfaces;

    public FusionnedSurface(List<Surface> fusionnedSurfaces) {
        super(false, extractResultantSummits(fusionnedSurfaces), false);
        this.fusionnedSurfaces = fusionnedSurfaces;
    }

    public List<Surface> getFusionnedSurfaces() {
        return this.fusionnedSurfaces;
    }

    @Override
    public boolean isFusionned() {
        return true;
    }

    private static List<Point> extractResultantSummits(List<Surface> fusionnedSurfaces) {
        return FusionHelper.getResultSummits(extractAllInnerSurfacesPoints(fusionnedSurfaces)).stream().map(p -> new Point(p)).collect(Collectors.toList());
    }

    private static List<List<utils.Point>> extractAllInnerSurfacesPoints(List<Surface> fusionnedSurfaces) {
        return fusionnedSurfaces
                .stream().map(s -> s.getSummits()
                        .stream().map(su -> su.toAbstract()).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
