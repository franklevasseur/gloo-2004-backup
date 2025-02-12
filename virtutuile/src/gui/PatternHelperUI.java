package gui;

import Domain.PatternType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PatternHelperUI {

    private static ObservableList<String> possibleTilePatterns = FXCollections.observableArrayList("", "Default", "Horizontal shift", "Vertical shift", "Mix", "Group mix");

    public static String patternToDisplayString(PatternType pattern) {
        String tilePatternString;
        if (pattern == PatternType.DEFAULT) {
            tilePatternString = "Default";
        } else if (pattern == PatternType.HORIZONTAL_SHIFT) {
            tilePatternString = "Horizontal shift";
        } else if (pattern == PatternType.VERTICAL_SHIFT) {
            tilePatternString = "Vertical shift";
        } else if (pattern == PatternType.MIX) {
            tilePatternString = "Mix";
        } else if (pattern == PatternType.GROUP_MIX) {
            tilePatternString = "Group mix";
        } else {
            tilePatternString = "";
        }
        return tilePatternString;
    }

    public static PatternType stringToPattern(String tilePatternString) {
        PatternType pattern;
        if (tilePatternString == "Default") {
            pattern = PatternType.DEFAULT;
        } else if (tilePatternString == "Horizontal shift") {
            pattern = PatternType.HORIZONTAL_SHIFT;
        } else if (tilePatternString == "Vertical shift") {
            pattern = PatternType.VERTICAL_SHIFT;
        } else if (tilePatternString == "Mix") {
            pattern = PatternType.MIX;
        } else if (tilePatternString == "Group mix") {
            pattern = PatternType.GROUP_MIX;
        } else {
            pattern = PatternType.DEFAULT;
        }
        return pattern;
    }

    public static String getPlaceHolder() {
        return "Default";
    }

    public static ObservableList<String> getPossibleTilePatterns() {
        return possibleTilePatterns;
    }
}
