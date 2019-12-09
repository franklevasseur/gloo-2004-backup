package utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class NumberUtils {
    public String Pied;
    public String Pouce;
    public String fractionFin;

    public NumberUtils(){ }
    public void ImperialFormatParse(String pValue){
        String[] temp = pValue.split("\'", 2);
        String[] temp2;

        if(temp.length > 1){
            Pied = temp[0];
            temp2 = temp[1].split("\"", 2);
        }else if (pValue.contains("\'") && temp.length == 0){
            Pied = temp[0];
            return;
        }else {
            temp2 = temp[0].split("\"", 2);
            Pied = "0";
        }

        if(temp2.length > 1){
            Pouce = temp2[0];
            fractionFin = temp2[1];
        }else if (pValue.contains("\"") && temp2.length == 0){
            Pouce = temp2[0];
            fractionFin = "";
        }else {
            Pouce = "";
            fractionFin = temp2[0];
        }
        return;
    }

    //la sortie cest des pouce en chiffre a virgule
    public double ImperialToDecimal(String pValue){
        double piedD;
        double pouceD;
        double fractionFinD;
        double out;
        ImperialFormatParse(pValue);
        piedD = fractionToDouble(Pied);
        if(isFraction(Pouce)){
            pouceD = fractionToDouble(Pouce);
        }else {
            if(Pouce.length() == 0){
                pouceD = 0;
            }else {
                pouceD = Double.parseDouble(Pouce);
            }

        }

        fractionFinD = fractionToDouble(fractionFin);

        out = (piedD * 12) + pouceD + fractionFinD;

        return out;
    }

    public Double fractionToDouble(String fraction){
        Double value = null;
        if (fraction.length() > 0) {
            if (fraction.contains("/")) {
                String[] numbers = fraction.split("/");
                if (numbers.length == 2) {
                    BigDecimal d1 = BigDecimal.valueOf(Double.valueOf(numbers[0]));
                    BigDecimal d2 = BigDecimal.valueOf(Double.valueOf(numbers[1]));
                    BigDecimal response = d1.divide(d2, MathContext.DECIMAL128);
                    value = response.doubleValue();
                }
            }
            else {
                value = Double.parseDouble(fraction);
            }
        }else {
            value = 0.0;
        }
        return value;
    }

    public String DecimalToImperialFormat(double pValue){
        String out = "";
        double modulo = pValue % 12;
        double newValue = pValue - modulo;
        int pied = (int)(newValue)/12;

        double old = modulo;
        modulo = modulo % 1;
        int pouce = (int)(old - modulo);

        String fraction = DecimalToFraction(modulo);

        if(pValue == 0){
            return "0";
        }
        if(pied != 0 ){
            out = Integer.toString(pied) + "\'";
        }

        if(pouce != 0){
            out = out + Integer.toString(pouce) + "\"";
        }

        if(pouce == 0 & fraction.length() > 0){
            out = out + fraction + "\"";
        }else if (fraction.length() > 0 ){
            out = out + fraction;
        }

        return out;
    }

    private String DecimalToFraction(double pValue) {
        if(pValue > 0){
            String s = String.valueOf(pValue);
            int digitsDec = s.length() - 1 - s.indexOf('.');
            float denom = 1;
            for (int i = 0; i < digitsDec; i++) {
                pValue *= 10;
                denom *= 10;
            }

            float num = (float) Math.round(pValue);
            float g = getGCD(num, denom);
            num = num / g;
            denom = denom /g;
            int a = (int)num;
            int b = (int)denom;
            String[] tempNum = (Float.toString(num)).split("\\.",2);
            String[] tempDenum = (Float.toString(denom)).split("\\.",2);

            return Integer.toString(a) + "/" + Integer.toString(b);
            //return tempNum[0] + "/" + tempDenum[0];
        }
        return "";

    }

    private float getGCD(float n1, float n2) {
        int gcd = 1;
        while(n1 != n2)
        {
            if(n1 > n2)
                n1 -= n2;
            else
                n2 -= n1;
        }
        return n1;
    }

    private Boolean isFraction(String pValue){
        if(pValue.indexOf("/") >= 0){
            return true;
        }
        return false;
    }

}
