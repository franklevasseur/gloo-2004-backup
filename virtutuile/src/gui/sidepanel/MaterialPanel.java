package gui.sidepanel;

import application.Controller;
import application.MaterialDto;
import gui.ColorHelper;
import gui.ZoomManager;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.text.ParseException;


public class MaterialPanel {

    private TextField materialNameInputBox;
    private TextField tilePerBoxInputBox;
    private TextField boxPriceInputBox;
    private TextField tileHeightMaterialInputBox;
    private TextField tileWidthMaterialInputBox;
    private ChoiceBox<String> materialColorChoiceBox;

    private ZoomManager zoomManager;

    private Controller domainController = Controller.getInstance();

    public MaterialPanel(TextField materialNameInputBox,
                         TextField tilePerBoxInputBox,
                         TextField boxPriceInputBox,
                         TextField tileHeightMaterialInputBox,
                         TextField tileWidthMaterialInputBox,
                         ChoiceBox<String> materialColorChoiceBox,
                         ZoomManager zoomManager) {
        this.materialNameInputBox = materialNameInputBox;
        this.tilePerBoxInputBox = tilePerBoxInputBox;
        this.boxPriceInputBox = boxPriceInputBox;
        this.tileHeightMaterialInputBox = tileHeightMaterialInputBox;
        this.tileWidthMaterialInputBox = tileWidthMaterialInputBox;
        this.materialColorChoiceBox = materialColorChoiceBox;
        this.zoomManager = zoomManager;
    }

    public void createNewMaterial(boolean metricDisplay) {

        try {
            InputBoxHelper parser = new InputBoxHelper(metricDisplay, zoomManager);

            MaterialDto dto = new MaterialDto();
            dto.name = materialNameInputBox.getText();
            dto.color = ColorHelper.stringToUtils(materialColorChoiceBox.getValue());

            CharSequence boxCost = this.boxPriceInputBox.getCharacters();
            dto.costPerBox = parser.parseToMetric(boxCost.toString());

            CharSequence tilePerBox = this.tilePerBoxInputBox.getCharacters();
            dto.nbTilePerBox = parser.parseToMetric(tilePerBox.toString()).intValue();

            CharSequence tileHeight = this.tileHeightMaterialInputBox.getCharacters();
            dto.tileTypeHeight = parser.parseToMetric(tileHeight.toString());

            CharSequence tileWidth = this.tileWidthMaterialInputBox.getCharacters();
            dto.tileTypeWidth = parser.parseToMetric(tileWidth.toString());

            domainController.createMaterial(dto);

            materialNameInputBox.clear();
            boxPriceInputBox.clear();
            tilePerBoxInputBox.clear();
            tileHeightMaterialInputBox.clear();
            tileWidthMaterialInputBox.clear();

        } catch (ParseException ignored) {
            // if there a parse error, we just empty all boxes...
        }
    }
}
