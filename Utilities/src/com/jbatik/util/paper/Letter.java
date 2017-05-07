/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.util.paper;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author RAPID02
 */
@ServiceProvider(service = PaperSize.class)
public class Letter extends PaperSize{

    @Override
    public Unit<Length> getDefaultUnit() {
        return NonSI.INCH;
    }

    @Override
    public Amount<Length> getWidth() {
        return Amount.valueOf(8.5, NonSI.INCH);
    }

    @Override
    public Amount<Length> getHeight() {
        return Amount.valueOf(11, NonSI.INCH);
    }

    @Override
    public String getName() {
        return "Letter";
    }
    
}
