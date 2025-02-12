package gui.sidepanel;

import gui.*;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SidePanelUI {

    private SurfacePropertiesPanel surfacePropertiesPanel;
    private MaterialPanel materialPanel;
    private AccountingPanel accountingPanel;
    private DistanceSurfaceLabelUI distanceSurfaceLabelUI;
    private SnapGridPanel snapGridPanel;
    private InspectorPanel inspectorPanel;
    private TileInfoUI tileInfoUI;

    public SidePanelUI(ZoomManager zoomManager,
                       Alert alert,
                       TextField tileHeightInputbox,
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
                       TextField materialNameInputBox,
                       TextField tilePerBoxInputBox,
                       TextField boxPriceInputBox,
                       TextField tileHeightMaterialInputBox,
                       TextField tileWidthMaterialInputBox,
                       ChoiceBox<String> materialColorChoiceBox,
                       TableView<MaterialUI> materialTableView,
                       TextField mNewHeightInputBox,
                       TextField mNewLenghtInputBox,
                       TextField mNewTilePerBoxInput,
                       TextField mNewPricePerBoxInputBox,
                       ChoiceBox<String> editTileMaterialChoiceBox,
                       ChoiceBox<String> mNewColorInputBox,
                       Label distanceSurfacePresentationLabel,
                       Label distanceSurfaceLabel,
                       TextField resizeSG,
                       Label snapgridLabel,
                       Button snapGridbutton,
                       SnapGridUI snapGrid,
                       TextField minInspectionLengthTextField,
                       Button inspectButton,
                       TextArea inspectionArea,
                       Label tileInfo) {

        this.surfacePropertiesPanel = new SurfacePropertiesPanel(tileHeightInputbox,
                tileWidthInputbox,
                sealWidthInputBox,
                surfaceHeightInputBox,
                surfaceWidthInputBox,
                masterTileX,
                masterTileY,
                tileAngleInputBox,
                tileShiftingInputBox,
                surfacePositionXInputBox,
                surfacePositionYInputBox,
                sealColorChoiceBox,
                tilePatternInputBox,
                tileMaterialChoiceBox,
                surfaceColorChoiceBox,
                zoomManager,
                alert);

        this.materialPanel = new MaterialPanel(materialNameInputBox,
                tilePerBoxInputBox,
                boxPriceInputBox,
                tileHeightMaterialInputBox,
                tileWidthMaterialInputBox,
                materialColorChoiceBox,
                zoomManager);

        this.accountingPanel = new AccountingPanel(materialTableView,
                mNewHeightInputBox,
                mNewLenghtInputBox,
                mNewTilePerBoxInput,
                mNewPricePerBoxInputBox,
                editTileMaterialChoiceBox,
                mNewColorInputBox,
                zoomManager);

        this.distanceSurfaceLabelUI = new DistanceSurfaceLabelUI(distanceSurfacePresentationLabel,
                distanceSurfaceLabel,
                zoomManager);

        this.snapGridPanel = new SnapGridPanel(resizeSG,
                snapgridLabel,
                snapGridbutton,
                snapGrid,
                zoomManager);

        this.inspectorPanel = new InspectorPanel(minInspectionLengthTextField,
                inspectButton,
                inspectionArea,
                zoomManager);

        this.tileInfoUI = new TileInfoUI(tileInfo, zoomManager);

        this.hideSnapGrid();
    }

    public void updateSelection(List<SurfaceUI> selectedSurfaces, boolean metricDisplay) {

        inspectorPanel.setMetric(metricDisplay);
        tileInfoUI.setMetric(metricDisplay);
        if (snapGridPanel.isVisible()) {
            snapGridPanel.showSnapgridInfo(metricDisplay);
        }

        if (selectedSurfaces.size() > 0) {
            displayInfo(selectedSurfaces, metricDisplay);
            return;
        }
        hideInfo();
    }

    private void displayInfo(List<SurfaceUI> selectedSurfaces, boolean metricDisplay) {
        accountingPanel.displayAccountingForSurfaces(selectedSurfaces);
        surfacePropertiesPanel.displaySurfaceInfo(selectedSurfaces, metricDisplay);
        accountingPanel.displayMaterialInfo(metricDisplay);
        distanceSurfaceLabelUI.updateDistanceSurface(selectedSurfaces.stream().map(s -> s.toDto()).collect(Collectors.toList()), metricDisplay);
    }

    public void hideInfo() {
        accountingPanel.displayAccountingForSurfaces(new ArrayList<>()); // empty
        surfacePropertiesPanel.hideSurfaceInfo();
        accountingPanel.hideMaterialInfo();
        distanceSurfaceLabelUI.eraseDistance();
    }

    public void displaySnapGrid(boolean metricDisplay) {
        snapGridPanel.showSnapgridInfo(metricDisplay);
    }

    public void hideSnapGrid() {
        snapGridPanel.hideSnapgridInfo();
    }

    public void applySnapGrid(boolean metricDisplay) {
        snapGridPanel.snapGridApply(metricDisplay);
    }

    public void editSurface(List<SurfaceUI> selectedSurfaces, boolean metric) {
        surfacePropertiesPanel.editSurface(selectedSurfaces, metric);
    }

    public void createNewMaterial(boolean metric) {
        materialPanel.createNewMaterial(metric);
    }

    public void updateMaterial(boolean metricDisplay) {
        accountingPanel.editMaterialButton(metricDisplay);
    }

    public void deleteMaterial() {
        accountingPanel.deleteMaterialButton();
    }

    public void inspect(boolean metricDisplay) {
        inspectorPanel.inspect(metricDisplay);
    }

    public TileInfoUI getTileInfoUI() {
        return this.tileInfoUI;
    }
}
