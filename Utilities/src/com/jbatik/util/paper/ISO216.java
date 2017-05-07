/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.util.paper;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author RAPID02
 */
public class ISO216 {

    @ServiceProvider(service = PaperSize.class)
    public static class A0 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(841, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(1189, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A0";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A1 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(594, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(841, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A1";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A2 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(420, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(594, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A2";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A3 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(297, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(420, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A3";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A4 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(210, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(297, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A4";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A5 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(148, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(210, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A5";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }

    @ServiceProvider(service = PaperSize.class)
    public static class A6 extends PaperSize {

        @Override
        public Amount<Length> getWidth() {
            return Amount.valueOf(105, SI.MILLIMETER);
        }

        @Override
        public Amount<Length> getHeight() {
            return Amount.valueOf(148, SI.MILLIMETER);
        }

        @Override
        public String getName() {
            return "A6";
        }

        @Override
        public Unit<Length> getDefaultUnit() {
            return SI.MILLIMETER;
        }
    }
}
