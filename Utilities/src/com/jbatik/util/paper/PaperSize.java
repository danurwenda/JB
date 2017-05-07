/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.util.paper;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;

/**
 *
 * @author RAPID02
 */
public abstract class PaperSize {

    public abstract Unit<Length> getDefaultUnit();

    public abstract Amount<Length> getWidth();

    public abstract Amount<Length> getHeight();

    public abstract String getName();

    public boolean equals(PaperSize s) {
//        System.err.println("membandingkan width " + s.getWidth() + " to " + getWidth());
//        System.err.println("membandingkan width " + s.getHeight() + " to " + getHeight());
//        System.err.println("membandingkan width " + s.getDefaultUnit() + " to " + getDefaultUnit());
        if (!s.getDefaultUnit().equals(getDefaultUnit())) {
            return false;
        }
//        System.err.println("widgetes " + s.getWidth().getEstimatedValue());
//        System.err.println("heigtgetes " + s.getHeight().getEstimatedValue());
        double swesv = s.getWidth().getEstimatedValue() * 1000000;
        double shesv = s.getHeight().getEstimatedValue() * 1000000;

        double twesv = getWidth().getEstimatedValue() * 1000000;
        double thesv = getHeight().getEstimatedValue() * 1000000;

        if (Math.abs(thesv - shesv) < 1 && Math.abs(twesv - swesv) < 1) {
            return true;
        } else {
            return Math.abs(thesv - swesv) < 1 && Math.abs(twesv - shesv) < 1;
        }

//        if(s.getWidth().getExactValue())
//        if ((s.getWidth().approximates(getWidth())) && (s.getHeight().approximates(getHeight()))) {
//            return true;
//        } else {
//            return (s.getWidth().approximates(getHeight())) && (s.getHeight().approximates(getWidth()));
//        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public static class Factory {

        public static PaperSize create(Amount<Length> width, Amount<Length> height, Unit<Length> unit, String name) {
            return new PaperSize() {

                @Override
                public Unit<Length> getDefaultUnit() {
                    return unit;
                }

                @Override
                public Amount<Length> getWidth() {
                    return width;
                }

                @Override
                public Amount<Length> getHeight() {
                    return height;
                }

                @Override
                public String getName() {
                    return name;
                }
            };
        }
    }

}
