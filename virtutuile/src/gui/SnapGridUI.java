package gui;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import utils.Point;
import utils.RectangleError;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.ArrayList;
import java.util.List;

public class SnapGridUI {

    private double gridGap = 50;
    private Pane parentNode;
    private List<Line> displayedLines = new ArrayList<>();
    private boolean isVisible = false;
    private Circle originIndicator;
    private Line xAxis;
    private Line yAxis;

    private List<Point> currentViewBox;

    public SnapGridUI(Pane parentNode) {
        this.parentNode = parentNode;

        originIndicator = new Circle();
        originIndicator.setFill(Color.RED);
        originIndicator.setRadius(7.5);
        this.parentNode.getChildren().addAll(originIndicator);

        xAxis = new Line(0, 0, 750, 0);
        yAxis = new Line(0, 0, 0, 750);
        this.parentNode.getChildren().add(xAxis);
        this.parentNode.getChildren().add(yAxis);
    }

    public void renderForViewBox(List<Point> viewBoxSummits) {
        currentViewBox = viewBoxSummits;
        removeGrid();

        RectangleInfo viewBoxRectangle;
        try {
            viewBoxRectangle = RectangleHelper.summitsToRectangleInfo(viewBoxSummits);
        } catch (RectangleError err) {
            return;
        }

        xAxis.setStartX(viewBoxRectangle.topLeftCorner.x);
        xAxis.setStartY(0);
        xAxis.setEndX(viewBoxRectangle.topLeftCorner.x + viewBoxRectangle.width);
        xAxis.setEndY(0);
        xAxis.setStrokeWidth(viewBoxRectangle.width / 750);

        yAxis.setStartX(0);
        yAxis.setStartY(viewBoxRectangle.topLeftCorner.y);
        yAxis.setEndX(0);
        yAxis.setEndY(viewBoxRectangle.topLeftCorner.y + viewBoxRectangle.height);
        yAxis.setStrokeWidth(viewBoxRectangle.width / 750);

        if (!this.isVisible) {
            return;
        }

        originIndicator.setRadius(viewBoxRectangle.width / 100);

        this.renderLines(viewBoxRectangle);
        this.renderColumn(viewBoxRectangle);
        this.parentNode.getChildren().addAll(displayedLines);
        toBack();
    }

    public void toBack() {
        displayedLines.forEach(Node::toBack);
    }

    public Bounds getOriginBounds() {
        return originIndicator.getBoundsInParent();
    }

    public Point getNearestGridPoint(Point point) {
        int i = (int) Math.round(point.x / gridGap);
        int j = (int) Math.round(point.y / gridGap);
        return new Point(i * gridGap, j * gridGap);
    }

    public void setVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return this.isVisible;
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

    public void setSnapGridGap(double pGap){
        gridGap = pGap;
        renderForViewBox(currentViewBox);
    }

    public double getSnapGripGap(){
        return this.gridGap;
    }
}
