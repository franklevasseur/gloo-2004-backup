package gui.sidepanel;

import Domain.Accounting;
import Domain.HoleStatus;
import application.Controller;
import application.MaterialDto;
import application.SurfaceDto;
import gui.ColorHelper;
import gui.MaterialUI;
import gui.SurfaceUI;
import gui.ZoomManager;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountingPanel {

    private TableView<MaterialUI> materialTableView;
    private TextField mNewHeightInputBox;
    private TextField mNewLenghtInputBox;
    private TextField mNewTilePerBoxInput;
    private TextField mNewPricePerBoxInputBox;
    private ChoiceBox<String> editTileMaterialChoiceBox;
    private ChoiceBox<String> mNewColorInputBox;

    private Controller domainController = Controller.getInstance();

    private ZoomManager zoomManager;

    public AccountingPanel(TableView<MaterialUI> materialTableView,
                           TextField mNewHeightInputBox,
                           TextField mNewLenghtInputBox,
                           TextField mNewTilePerBoxInput,
                           TextField mNewPricePerBoxInputBox,
                           ChoiceBox<String> editTileMaterialChoiceBox,
                           ChoiceBox<String> mNewColorInputBox,
                           ZoomManager zoomManager) {
        this.materialTableView = materialTableView;
        this.mNewHeightInputBox = mNewHeightInputBox;
        this.mNewLenghtInputBox = mNewLenghtInputBox;
        this.mNewTilePerBoxInput = mNewTilePerBoxInput;
        this.mNewPricePerBoxInputBox = mNewPricePerBoxInputBox;
        this.editTileMaterialChoiceBox = editTileMaterialChoiceBox;
        this.mNewColorInputBox = mNewColorInputBox;
        this.zoomManager = zoomManager;

        // TODO: should call method with correct unit system
        editTileMaterialChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> displayMaterialInfo(true)));
    }

    public void editMaterialButton(boolean metricDisplay) {

        InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);

        try {
            MaterialDto mDTO = new MaterialDto();

            CharSequence mNewHeight = this.mNewHeightInputBox.getCharacters();
            Double newMaterialHeight = parser.parseToMetric(mNewHeight.toString());

            CharSequence mNewWidth = this.mNewLenghtInputBox.getCharacters();
            Double newMaterialWidth = parser.parseToMetric(mNewWidth.toString());

            CharSequence mNbTilePerBox = this.mNewTilePerBoxInput.getCharacters();
            Integer newNbTilePerBox = parser.parseToMetric(mNbTilePerBox.toString()).intValue();

            CharSequence mCostPerBox = this.mNewPricePerBoxInputBox.getCharacters();
            Integer newCostPerBox = parser.parseToMetric(mCostPerBox.toString()).intValue();

            mDTO.name = editTileMaterialChoiceBox.getValue();
            mDTO.tileTypeHeight = newMaterialHeight;
            mDTO.tileTypeWidth = newMaterialWidth;
            mDTO.nbTilePerBox = newNbTilePerBox;
            mDTO.costPerBox = newCostPerBox;
            mDTO.color = ColorHelper.stringToUtils(mNewColorInputBox.getValue());
            domainController.updateMaterial(mDTO);

        } catch (ParseException e) {
            displayMaterialInfo(metricDisplay);
        }

        hideMaterialInfo();
    }

    public void displayMaterialInfo(boolean metricDisplay) {

        InputBoxHelper formater = new InputBoxHelper(metricDisplay, zoomManager);

        String materialName = editTileMaterialChoiceBox.getValue();
        Optional<MaterialDto> optionalMaterial = domainController.getMaterialByName(materialName);

        if (optionalMaterial.isPresent()) {
            MaterialDto material = optionalMaterial.get();
            mNewHeightInputBox.setText(formater.formatMetric(material.tileTypeHeight));
            mNewLenghtInputBox.setText(formater.formatMetric(material.tileTypeWidth));
            mNewTilePerBoxInput.setText(formater.formatMetric(material.nbTilePerBox));
            mNewPricePerBoxInputBox.setText(formater.formatMetric(material.costPerBox));
            mNewColorInputBox.setValue(ColorHelper.utilsColorToString(material.color));
        }
    }

    public void hideMaterialInfo() {
        mNewHeightInputBox.clear();
        mNewLenghtInputBox.clear();
        mNewTilePerBoxInput.clear();
        mNewPricePerBoxInputBox.clear();
        mNewColorInputBox.setValue("");
    }

    public void displayAccountingForSurfaces(List<SurfaceUI> pSelectedSurfaces, boolean metricDisplay) {
        this.materialTableView.getItems().clear();

        List<SurfaceDto> listDTO = new ArrayList<>();
        for (SurfaceUI i : pSelectedSurfaces) {
            if (i.toDto().isHole == HoleStatus.FILLED) {
                listDTO.add(i.toDto());
            }
        }
        if (listDTO.size() == 0) {
            domainController.getAccounting();
        } else {
            domainController.getSurfaceAccount(listDTO);
        }
        List<Accounting> account = domainController.Maccount;
        for (Accounting accounting : account) {

            InputBoxHelper formatter = new InputBoxHelper(metricDisplay, zoomManager);

            MaterialUI materialUI = new MaterialUI();
            materialUI.name = accounting.getMaterial().getMaterialName();
            materialUI.pricePerBoxe = formatter.formatMetric(accounting.getMaterial().getCostPerBox());
            materialUI.color = ColorHelper.utilsColorToString(accounting.getMaterial().getColor());
            materialUI.tilePerBox = formatter.formatMetric(accounting.getMaterial().getNbTilePerBox());
            materialUI.numberOfTiles = formatter.formatMetric(accounting.getUsedTiles());
            materialUI.numberOfBoxes = formatter.formatMetric(accounting.getNbBoxes());
            materialUI.totalPrice = formatter.formatMetric(accounting.getTotalCost());

            materialTableView.getItems().add(materialUI);
        }
    }
}
