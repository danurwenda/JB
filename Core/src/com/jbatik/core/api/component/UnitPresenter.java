/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import java.util.ArrayList;
import java.util.Arrays;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

/**
 * PaperPresenter, menghandle perubahan ukuran2 kertas.
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class UnitPresenter {

    private static final Object[] unit = {SI.MILLIMETER, SI.CENTIMETER, NonSI.INCH, NonSI.PIXEL};

    public static ArrayList getUnit() {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(unit));
        return arrayList;
    }

    /**
     * Added by Slurp 31/1/2014 A change in measurement unit should also change
     * the "step" of width/height spinners' model into suitable value. For
     * example, step of 1 will be used on millimeter, and step of 0.1 will be
     * used on centimeter. *
     *
     * Remarks on how unit in Corel steps : UNIT | STEP (millimeter,1),
     * (inch,0.1), (pixel,2), (cm,0.1)
     *
     * @param currentUnit
     * @return the step
     */
    public static Number getSpinnerStep(Object currentUnit) {
        if (currentUnit.equals(SI.MILLIMETER) || currentUnit.equals(NonSI.PIXEL)) {
            return 1;
        } else {
            return 0.1;
        }
    }

}
