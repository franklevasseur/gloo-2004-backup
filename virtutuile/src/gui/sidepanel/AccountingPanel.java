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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private boolean lastMetric = true;

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

        editTileMaterialChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> displayMaterialInfo(lastMetric)));
    }

    public void editMaterialButton(boolean metricDisplay) {

        lastMetric = metricDisplay;

        InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);
        NumberFormat formater = NumberFormat.getInstance(Locale.FRANCE);

        try {
            MaterialDto mDTO = new MaterialDto();

            CharSequence mNewHeight = this.mNewHeightInputBox.getCharacters();
            Double newMaterialHeight = parser.parseToMetric(mNewHeight.toString());

            CharSequence mNewWidth = this.mNewLenghtInputBox.getCharacters();
            Double newMaterialWidth = parser.parseToMetric(mNewWidth.toString());

            CharSequence mNbTilePerBox = this.mNewTilePerBoxInput.getCharacters();
            Integer newNbTilePerBox = mNbTilePerBox.toString().equals("") ? null : formater.parse(mNbTilePerBox.toString()).intValue();

            CharSequence mCostPerBox = this.mNewPricePerBoxInputBox.getCharacters();
            Double newCostPerBox = mCostPerBox.toString().equals("") ? null : formater.parse(mCostPerBox.toString()).doubleValue();

            mDTO.name = editTileMaterialChoiceBox.getValue();
            mDTO.tileTypeHeight = newMaterialHeight;
            mDTO.tileTypeWidth = newMaterialWidth;
            mDTO.nbTilePerBox = newNbTilePerBox;
            mDTO.costPerBox = newCostPerBox;
            mDTO.color = ColorHelper.stringToUtils(mNewColorInputBox.getValue());
            domainController.updateMaterial(mDTO);

        } catch (ParseException | RuntimeException e) {
            displayMaterialInfo(metricDisplay);
        }

        hideMaterialInfo();
    }

    public void deleteMaterialButton() {
        String materialName = editTileMaterialChoiceBox.getValue();
        if (materialName != null && !materialName.equals("")) {
            domainController.removeMaterial(materialName);
        }
    }

    public void displayMaterialInfo(boolean metricDisplay) {

        lastMetric = metricDisplay;

        InputBoxHelper helper = new InputBoxHelper(metricDisplay, zoomManager);
        NumberFormat formater = NumberFormat.getInstance(Locale.FRANCE);

        String materialName = editTileMaterialChoiceBox.getValue();
        Optional<MaterialDto> optionalMaterial = domainController.getMaterialByName(materialName);

        if (optionalMaterial.isPresent()) {
            MaterialDto material = optionalMaterial.get();
            mNewHeightInputBox.setText(helper.formatMetric(material.tileTypeHeight));
            mNewLenghtInputBox.setText(helper.formatMetric(material.tileTypeWidth));
            mNewTilePerBoxInput.setText(formater.format(material.nbTilePerBox));
            mNewPricePerBoxInputBox.setText(formater.format(material.costPerBox));
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

    public void displayAccountingForSurfaces(List<SurfaceUI> pSelectedSurfaces) {

        this.materialTableView.getItems().clear();

        List<SurfaceDto> listDTO = new ArrayList<>();
        for (SurfaceUI i : pSelectedSurfaces) {
            if (i.toDto().isHole == HoleStatus.FILLED) {
                listDTO.add(i.toDto());
            }
        }

        List<Accounting> account;
        if (listDTO.size() == 0) {
            account = domainController.getAllAccounting();
        } else {
            account = domainController.getAccountingForSurfaces(listDTO);
        }

        for (Accounting accounting : account) {

            MaterialUI materialUI = new MaterialUI();
            materialUI.name = accounting.getMaterial().getMaterialName();
            materialUI.pricePerBoxe = String.format("%.2f", accounting.getMaterial().getCostPerBox());
            materialUI.color = ColorHelper.utilsColorToString(accounting.getMaterial().getColor());
            materialUI.tilePerBox = String.format("%d", accounting.getMaterial().getNbTilePerBox());
            materialUI.numberOfTiles = String.format("%d", accounting.getUsedTiles());
            materialUI.numberOfBoxes = String.format("%d", accounting.getNbBoxes());
            materialUI.totalPrice = String.format("%.2f", accounting.getTotalCost());
            materialTableView.getItems().add(materialUI);
        }

        appendTotal(account);
    }

    private void appendTotal(List<Accounting> account) {

        double totalcost = 0;
        int totalTiles = 0;
        int totalUsedTiles = 0;
        int totalBox = 0;
        for (Accounting accounting : account) {
            totalcost += accounting.getTotalCost();
            totalTiles += accounting.getAllTiles();
            totalUsedTiles += accounting.getUsedTiles();
            totalBox += accounting.getNbBoxes();
        }

        double avgPricePerBox = totalcost / totalBox;
        double avgTilesPerBox = ((double)totalTiles) / ((double)totalBox);

        MaterialUI materialUI = new MaterialUI();

        materialUI.name = "total";
        materialUI.pricePerBoxe = String.format("%.2f", avgPricePerBox);
        materialUI.color = "";
        materialUI.tilePerBox = String.format("%.2f", avgTilesPerBox);
        materialUI.numberOfTiles = String.format("%d", totalUsedTiles);
        materialUI.numberOfBoxes = String.format("%d", totalBox);
        materialUI.totalPrice = String.format("%.2f", totalcost);
        materialTableView.getItems().add(materialUI);
    }
}
