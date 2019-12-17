package gui.sidepanel;

import Domain.HoleStatus;
import Domain.PatternType;
import application.*;
import gui.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import utils.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public class SurfacePropertiesPanel {

    private TextField tileHeightInputbox;
    private TextField tileWidthInputbox;
    private TextField sealWidthInputBox;
    private TextField surfaceHeightInputBox;
    private TextField surfaceWidthInputBox;
    private TextField masterTileX;
    private TextField masterTileY;
    private TextField tileAngleInputBox;
    private TextField tileShiftingInputBox;
    private TextField surfacePositionXInputBox;
    private TextField surfacePositionYInputBox;
    private ChoiceBox<String> sealColorChoiceBox;
    private ChoiceBox<String> tilePatternInputBox;
    private ChoiceBox<String> tileMaterialChoiceBox;
    private ChoiceBox<String> surfaceColorChoiceBox;

    private Alert alert;

    private ZoomManager zoomManager;
    private Controller domainController = Controller.getInstance();

    public SurfacePropertiesPanel(TextField tileHeightInputbox,
                                  TextField tileWidthInputbox,
                                  TextField sealWidthInputBox,
                                  TextField surfaceHeightInputBox,
                                  TextField surfaceWidthInputBox,
                                  TextField masterTileX,
                                  TextField masterTileY,
                                  TextField tileAngleInputBox,
                                  TextField tileShiftingInputBox,
                                  TextField surfacePositionXInputBox,
                                  TextField surfacePositionYInputBox,
                                  ChoiceBox<String> sealColorChoiceBox,
                                  ChoiceBox<String> tilePatternInputBox,
                                  ChoiceBox<String> tileMaterialChoiceBox,
                                  ChoiceBox<String> surfaceColorChoiceBox,
                                  ZoomManager zoomManager,
                                  Alert alert) {
        this.tileHeightInputbox = tileHeightInputbox;
        this.tileWidthInputbox = tileWidthInputbox;
        this.sealWidthInputBox = sealWidthInputBox;
        this.surfaceHeightInputBox = surfaceHeightInputBox;
        this.surfaceWidthInputBox = surfaceWidthInputBox;
        this.masterTileX = masterTileX;
        this.masterTileY = masterTileY;
        this.tileAngleInputBox = tileAngleInputBox;
        this.tileShiftingInputBox = tileShiftingInputBox;
        this.surfacePositionXInputBox = surfacePositionXInputBox;
        this.surfacePositionYInputBox = surfacePositionYInputBox;
        this.sealColorChoiceBox = sealColorChoiceBox;
        this.tilePatternInputBox = tilePatternInputBox;
        this.tileMaterialChoiceBox = tileMaterialChoiceBox;
        this.surfaceColorChoiceBox = surfaceColorChoiceBox;
        this.zoomManager = zoomManager;
        this.alert = alert;
    }

    public void editSurface(List<SurfaceUI> selectedSurfaces, boolean metricDisplay) {

        if (selectedSurfaces.size() != 0) {
            SurfaceUI chosenSurface = selectedSurfaces.get(0);

            InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);

            try {
                CharSequence masterTileXInput = this.masterTileX.getCharacters();
                Double newMasterTileX = parser.parseToMetric(masterTileXInput.toString());

                CharSequence masterTileYInput = this.masterTileY.getCharacters();
                Double newMasterTileY = parser.parseToMetric(masterTileYInput.toString());

                CharSequence tileAngleInput = this.tileAngleInputBox.getCharacters();
                Double tileAngle = tileAngleInput.toString().equals("") ? null : format.parse(tileAngleInput.toString()).doubleValue();

                CharSequence tileShiftInput = this.tileShiftingInputBox.getCharacters();
                Double tileShift = parser.parseToMetric(tileShiftInput.toString());

                CharSequence sealWidthInput = this.sealWidthInputBox.getCharacters();
                Double newSealWidth = parser.parseToMetric(sealWidthInput.toString());

                CharSequence sealColorInput = this.sealColorChoiceBox.getValue();

                CharSequence surfaceColorInput = this.surfaceColorChoiceBox.getValue();
                chosenSurface.setSurfaceColor(ColorHelper.stringToUtils(surfaceColorInput.toString()));

                CharSequence patternInput = this.tilePatternInputBox.getValue();

                CharSequence surfaceHeightInput = this.surfaceHeightInputBox.getCharacters();
                double newsurfaceHeight = parser.parseToMetric(surfaceHeightInput.toString());

                CharSequence surfaceWidthInput = this.surfaceWidthInputBox.getCharacters();
                double newSurfaceWidth = parser.parseToMetric(surfaceWidthInput.toString());

                CharSequence tileMaterialInput = tileMaterialChoiceBox.getValue();
                tileMaterialInput = tileMaterialInput == null ? "" : tileMaterialInput;

                TileDto masterTile = null;
                if (newMasterTileX != null && newMasterTileY != null && !tileMaterialInput.equals("")) {
                    MaterialDto chosenMaterial = domainController.getMaterialByName(tileMaterialInput.toString()).get();

                    masterTile = new TileDto();
                    masterTile.material = chosenMaterial;

                    RectangleInfo masterTileRect = new RectangleInfo(new Point(newMasterTileX, newMasterTileY), chosenMaterial.tileTypeWidth, chosenMaterial.tileTypeHeight);
                    masterTile.summits = RectangleHelper.rectangleInfoToSummits(masterTileRect.topLeftCorner, masterTileRect.width, masterTileRect.height);
                    chosenSurface.setMasterTile(masterTile);
                }

                CharSequence positionXinput = surfacePositionXInputBox.getCharacters();
                double newPositionX = parser.parseToMetric(positionXinput.toString());

                CharSequence positionYinput = surfacePositionYInputBox.getCharacters();
                double newPositionY = parser.parseToMetric(positionYinput.toString());

                Point position = new Point(newPositionX, newPositionY);
                chosenSurface.setPosition(position);

                chosenSurface.setSize(newSurfaceWidth, newsurfaceHeight);
                if (masterTile == null || newSealWidth == null || sealColorInput == null || patternInput == null || tileAngle == null || tileShift == null) {
                    this.domainController.updateSurface(chosenSurface.toDto());
                } else {
                    SealsInfoDto sealsInfoDto = new SealsInfoDto();
                    sealsInfoDto.sealWidth = newSealWidth;
                    sealsInfoDto.color = ColorHelper.stringToUtils(sealColorInput.toString());
                    chosenSurface.setSealsInfo(sealsInfoDto);

                    PatternType pattern = PatternHelperUI.stringToPattern(patternInput.toString());

                    PatternType fallBackPattern = chosenSurface.getPattern() == null ? PatternType.DEFAULT : chosenSurface.getPattern();
                    pattern = checkPattern(pattern, fallBackPattern, masterTile);
                    chosenSurface.setPattern(pattern);

                    tileAngle = checkTileAngle(tileAngle);
                    chosenSurface.setTileAngle(tileAngle);

                    chosenSurface.setTileShifting(tileShift);
                    this.domainController.updateAndRefill(chosenSurface.toDto(), masterTile, pattern, sealsInfoDto, tileAngle, tileShift);
                }

                hideSurfaceInfo();
            } catch (ParseException | NumberFormatException e) {
                displaySurfaceInfo(selectedSurfaces, metricDisplay);
            }
        }
    }

    private PatternType checkPattern(PatternType newPattern, PatternType previous, TileDto masterTIile) {
        if (newPattern == PatternType.MIX || newPattern == PatternType.GROUP_MIX) {
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(masterTIile.summits);

            double minSide = Math.min(tileRect.height, tileRect.width);
            double maxSide = Math.max(tileRect.height, tileRect.width);
            boolean isAllowed = Math.abs(maxSide - 2 * minSide) < Point.DOUBLE_TOLERANCE;

            if (!isAllowed) {
                alert.setHeaderText(String.format("You can select pattern '%s' only if the tile width is half of its length...",
                        PatternHelperUI.patternToDisplayString(newPattern)));
                alert.setContentText(String.format("Falling back on pattern '%s'", PatternHelperUI.patternToDisplayString(previous)));
                alert.showAndWait();
                return previous;
            }
        }
        return newPattern;
    }

    private double checkTileAngle(double newAngle) {
        if (newAngle < 0 || newAngle > 90) {
            alert.setHeaderText(String.format("Tile angle must be an angle in degrees between 0 and 90... %.1f is out of bound", newAngle));
            alert.setContentText(String.format("Falling back on angle = %f", 0.0));
            alert.showAndWait();
            return 0;
        }
        return newAngle;
    }

    public void displaySurfaceInfo(List<SurfaceUI> selectedSurfaces, boolean metricDisplay) {
        SurfaceUI firstOne = selectedSurfaces.get(0);

        InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);
        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);

        double height = ShapeHelper.getHeight(new AbstractShape(firstOne.toDto().summits, false));
        double width = ShapeHelper.getWidth(new AbstractShape(firstOne.toDto().summits, false));
        Point topLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(firstOne.toDto().summits, false));

        surfaceHeightInputBox.setText(parser.formatMetric(height));
        surfaceWidthInputBox.setText(parser.formatMetric(width));

        if (firstOne.getSealsInfo() != null) {
            sealWidthInputBox.setText(parser.formatMetric(firstOne.getSealsInfo().sealWidth));
            sealColorChoiceBox.setValue(ColorHelper.utilsColorToString(firstOne.getSealsInfo().color));
        }

        surfaceColorChoiceBox.setValue(ColorHelper.utilsColorToString(firstOne.getSurfaceColor()));

        if (firstOne.getPattern() != null) {
            PatternType pattern = firstOne.getPattern();
            tilePatternInputBox.setValue(PatternHelperUI.patternToDisplayString(pattern));
        } else if (firstOne.toDto().isHole == HoleStatus.FILLED) {
            tilePatternInputBox.setValue(PatternHelperUI.getPlaceHolder());
        } else {
            tilePatternInputBox.setValue("");
        }

        surfacePositionXInputBox.setText(parser.formatMetric(topLeft.x));
        surfacePositionYInputBox.setText(parser.formatMetric(topLeft.y));

        if (firstOne.toDto().isHole == HoleStatus.FILLED && firstOne.toDto().tiles != null && firstOne.toDto().tiles.size() > 0) {
            String tileMaterial = firstOne.toDto().tiles.get(0).material.name;
            tileMaterialChoiceBox.setValue(tileMaterial);

            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(firstOne.getMasterTile().summits);

            tileHeightInputbox.setText(parser.formatMetric(tileRect.height));
            tileWidthInputbox.setText(parser.formatMetric(tileRect.width));
            masterTileX.setText(parser.formatMetric(tileRect.topLeftCorner.x));
            masterTileY.setText(parser.formatMetric(tileRect.topLeftCorner.y));

            tileAngleInputBox.setText(format.format(firstOne.getTileAngle()));

            tileShiftingInputBox.setText(parser.formatMetric(firstOne.getTileShifting()));
        }
    }

    public void hideSurfaceInfo() {
        tileHeightInputbox.clear();
        tileWidthInputbox.clear();
        surfaceHeightInputBox.clear();
        surfaceWidthInputBox.clear();
        surfacePositionXInputBox.clear();
        surfacePositionYInputBox.clear();
        sealWidthInputBox.clear();

        masterTileX.clear();
        masterTileY.clear();

        tileAngleInputBox.clear();
        tileShiftingInputBox.clear();

        tilePatternInputBox.setValue("");
        sealColorChoiceBox.setValue("");
        surfaceColorChoiceBox.setValue("");
    }
}
