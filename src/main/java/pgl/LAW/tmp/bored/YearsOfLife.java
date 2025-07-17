package pgl.LAW.tmp.bored;

import pgl.infra.utils.PArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class YearsOfLife {
    public static void main(String[] args) {
        List<Double> yearValues = new ArrayList<>();
        double thisYear = 1.0;
        double sumYearValue = 0.0;
        List<Double> sumYearValues = new ArrayList<>();
        List<Integer> ages = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            yearValues.add(thisYear);
            sumYearValue += thisYear;
            sumYearValues.add(sumYearValue);
            ages.add(i);
            thisYear = 1/sumYearValue;
        }
        System.out.println("All of our life year value is: " + sumYearValue);
        for (int i = 0; i < ages.size(); i++) {
            if (sumYearValues.get(i) > sumYearValue/2) {
                System.out.println("The mid-point of our life is the age of: " + ages.get(i));
                break;
            }
        }
    }
}
