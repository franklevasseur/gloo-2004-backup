package gui.sidepanel;

import gui.ZoomManager;
import utils.imperial.ImperialFractionHelper;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class InputBoxHelper {

    private boolean metric;
    private NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    private ImperialFractionHelper imperialFractionHelper = new ImperialFractionHelper();
    private ZoomManager zoomManager;

    public InputBoxHelper(boolean metric, ZoomManager zoomManager) {
        this.metric = metric;
        this.zoomManager = zoomManager;
    }

    public Double parseToMetric(String input) throws ParseException {
        if (metric) {
            return input.equals("") ? null : format.parse(input).doubleValue();
        }

        double inchesDecimal = imperialFractionHelper.parseImperialFraction(input);
        return zoomManager.inchToMeters(inchesDecimal);
    }

    public String formatMetric(double meters) {
        if (this.metric) {
            return format.format(meters);
        }

        double inchesDecimal = zoomManager.metersToInch(meters);
        return imperialFractionHelper.formatImperialFraction(inchesDecimal);
    }
}
