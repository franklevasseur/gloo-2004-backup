package gui;

import utils.Point;

public interface BoundingBoxResizable {
    void resizeRespectingBoundingBox(Point topLeftBounding, Point bottomRightBounding, double deltaWidth, double deltaHeight);
}
