package gui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.ArrayList;
import java.util.List;

public class SnapGridUI {

    private double gridGap = 100;
    private Pane parentNode;
    private List<Line> displayedLines = new ArrayList<>();

    public SnapGridUI(Pane parentNode) {
        this.parentNode = parentNode;
    }

    public void renderForViewBox(List<Point> viewBoxSummits) {
        removeGrid();
        RectangleInfo viewBoxRectangle = RectangleHelper.summitsToRectangleInfo(viewBoxSummits);
        this.renderLines(viewBoxRectangle);
        this.renderColumn(viewBoxRectangle);
        this.parentNode.getChildren().addAll(displayedLines);
    }

    private void renderLines(RectangleInfo viewBoxRectangle) {
        int lowerBoundLine = (int) Math.ceil(viewBoxRectangle.topLeftCorner.y / gridGap);
        int upperBoundLine = (int) Math.ceil((viewBoxRectangle.topLeftCorner.y + viewBoxRectangle.height) / gridGap);

        int nLines = upperBoundLine - lowerBoundLine;
        if (nLines < 1 || nLines >= 1000) {
            return;
        }

        int power = (int) Math.floor(Math.log10(nLines));
        int fuck = (int) Math.pow(10, power > 0 ? power - 1 : power);

        for (int i = lowerBoundLine; i <= upperBoundLine; i += fuck) {
            double yi = i * gridGap;
            Line line = new Line(viewBoxRectangle.topLeftCorner.x, yi, viewBoxRectangle.topLeftCorner.x + viewBoxRectangle.width, yi);
            line.setStroke(Color.GRAY);
            line.getStrokeDashArray().addAll(5d);
            this.displayedLines.add(line);
        }
    }

    private void renderColumn(RectangleInfo viewBoxRectangle) {
        int lowerBoundColumn = (int) Math.ceil(viewBoxRectangle.topLeftCorner.x / gridGap);
        int upperBoundColumn = (int) Math.ceil((viewBoxRectangle.topLeftCorner.x + viewBoxRectangle.width) / gridGap);

        int nColumns = upperBoundColumn - lowerBoundColumn;
        if (nColumns < 1 || nColumns >= 1000) {
            return;
        }

        int power = (int) Math.floor(Math.log10(nColumns));
        int fuck = (int) Math.pow(10, power > 0 ? power - 1 : power);

        for (int i = lowerBoundColumn; i <= upperBoundColumn; i += fuck) {
            double xi = i * gridGap;
            Line line = new Line(xi, viewBoxRectangle.topLeftCorner.y, xi, viewBoxRectangle.topLeftCorner.y + viewBoxRectangle.height);
            line.setStroke(Color.GRAY);
            line.getStrokeDashArray().addAll(5d);
            this.displayedLines.add(line);
        }
    }

    public void removeGrid() {
        this.parentNode.getChildren().removeIf(displayedLines::contains);
        this.displayedLines.clear();
    }
}
