package utils;

import java.util.List;

public class AbstractShape {

    public boolean toSubstract;
    public List<Point> summits;

    public AbstractShape(List<Point> summits, boolean toSubstract) {
        this.summits = summits;
        this.toSubstract = toSubstract;
    }
}
