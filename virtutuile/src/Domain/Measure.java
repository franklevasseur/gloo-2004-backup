package Domain;

public class Measure {
    private double value;
    static double pixelMeterFactor = 1;
    //private UnitType unit;

    public Measure(){
        this.value = 0;
    }
    public Measure(double pValue, UnitType pUnit){
        this.value = pValue;
        //unit = pUnit;
    }

    public double getValue() {
        return value;
    }

    /*
    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }*/

    public void setValue(double value) {
        this.value = value;
    }

    public double getTypedvalue(UnitType pUnit){
        switch (pUnit){
            case mm:
                return (value*pixelMeterFactor)/1000;
            case cm:
                return (value*pixelMeterFactor)/100;
            case dm:
                return (value*pixelMeterFactor)/10;
            case m:
                return value*pixelMeterFactor;
            case in:
                return (value*pixelMeterFactor)/39.3701;
            case ft:
                return (value*pixelMeterFactor)/3.28084;
            case yd:
                return (value*pixelMeterFactor)/1.09361;
            default:
                return value;
        }
    }

}
