/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api;

import com.jbatik.util.paper.PaperSize;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import static javax.measure.unit.NonSI.PIXEL;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;
import org.openide.util.Lookup;

/**
 *
 * @author RAPID02
 */
public class DocumentPaper {

    private Amount<Length> width;
    private Amount<Length> height;
    private int DPI;
    private Unit activeUnit;
    private PaperSize paperSize;

    public DocumentPaper(double w, double h, String u) {
        this(w, h, Toolkit.getDefaultToolkit().getScreenResolution(), u);
    }

    public DocumentPaper(double w, double h, int dpi, String u) {
        this.DPI = dpi;

        try {
            this.activeUnit = Unit.valueOf(u);
        } catch (IllegalArgumentException iae) {
            this.activeUnit = null;
            //TODO : throw Exception about unrecognized activeUnit
        } finally {
            if (activeUnit != null) {
                if (DPI != 72) {
                    //musti diatur2 sama DPI, karena PIXEL itu pakai DPI 72
                }
                width = Amount.valueOf(w, activeUnit);
                height = Amount.valueOf(h, activeUnit);
                PaperSize s = PaperSize.Factory.create(width, height, activeUnit, null);
                //compare these amounts to registered paper size
                for (PaperSize ps : Lookup.getDefault().lookupAll(PaperSize.class)) {
                    if (ps.equals(s)) {
                        //found an approximately matched paper size
                        setPaperSize(ps);
                        break;
                    }
                }
            }
        }
    }
    public static final String UNIT_PROP = "unit";

    public Unit getUnit() {
        return activeUnit;
    }

    public void setUnit(Unit u) {
        Unit o = activeUnit;
        this.activeUnit = u;
        pcs.firePropertyChange(UNIT_PROP, o, u);
    }

    public static final String PAPER_SIZE_PROP = "paper_size";

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public final void setPaperSize(PaperSize p) {
        PaperSize o = paperSize;
        this.paperSize = p;
        pcs.firePropertyChange(paperSize.getName(), o, p);
//        setDefinedSize(true);
    }

    public static final String WIDTH_PROP = "width";

    /**
     * Updating width using currently used unit.
     *
     * @param w
     */
    public void setWidth(double w) {
        Amount<Length> o = width;
        width = Amount.valueOf(w, activeUnit);
        pcs.firePropertyChange(WIDTH_PROP, o, width);
    }

    public double getWidth() {
        return width.getEstimatedValue();
    }

    public int getWidthInPixel() {
        if (activeUnit == PIXEL) {
            return (int) Math.round(getWidth());
        } else {
            return getLengthInPixel(width, DPI);
        }
    }
    public static final String HEIGHT_PROP = "height";

    /**
     * Updating width using currently used unit.
     *
     * @param w
     */
    public void setHeight(double w) {
        Amount<Length> o = height;
        height = Amount.valueOf(w, activeUnit);
        pcs.firePropertyChange(HEIGHT_PROP, o, height);
    }

    public int getHeightInPixel() {
        if (activeUnit == PIXEL) {
            return (int) Math.round(getHeight());
        } else {
            return getLengthInPixel(height, DPI);
        }
    }

    public static int getLengthInPixel(Amount<Length> n, int d) {
        if (d == 72) {
            return (int) Math.round(n.doubleValue(PIXEL));
        } else {
            Unit l = NonSI.INCH.divide(d);
            return (int) Math.round(n.doubleValue(l));
        }
    }

    public double getHeight() {
        return height.getEstimatedValue();
    }
    public static final String DPI_PROP = "DPI";

    /**
     * Unless activeUnit is pixel-based, this method will change the amount of
     * pixel used by the document.
     *
     * @param dpi
     */
    public void setDPI(int dpi) {
        int o = DPI;
        DPI = dpi;
        pcs.firePropertyChange(DPI_PROP, o, dpi);
    }

    public int getDPI() {
        return DPI;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
}
