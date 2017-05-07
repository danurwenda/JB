/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem;

import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class InvalidableVisualLSystem extends VisualLSystem implements InvalidableLSystem {

    @StaticResource
    private static final String ERR_ICON_PATH = "com/jbatik/lsystem/err-icon.png";
    public static Icon ERR_ICON = ImageUtilities.image2Icon(ImageUtilities.loadImage(ERR_ICON_PATH));

    public InvalidableVisualLSystem(String axiom, Map<Character, String> rules, int iteration, int angle, int length, int width) {
        super(axiom, rules, iteration, angle, length, width);
        this.rawAxiom = axiom;
        this.rawDetails = getStringRules();
    }

    public InvalidableVisualLSystem(String ax, String d, int i, int a, int l, int w) {
        super("", new HashMap(), i, a, l, w);

        this.rawAxiom = ax;
        this.rawDetails = d;
        try {
            setAxiom(ax);
            setStringRules(d);
        } catch (ParseRuleException ex) {
        }
    }

    public void setStringRules(String s, boolean notif) throws ParseRuleException {
        invalid = false;
        try {
            super.setStringRules(s);
        } catch (ParseRuleException ex) {
            if (notif) {
                NotificationDisplayer.getDefault().notify("Parse error", ERR_ICON, ex.getMessage(), null, NotificationDisplayer.Priority.HIGH);
            }
            invalid = true;
            throw ex;
        }
    }

    @Override
    public void setStringRules(String s) throws ParseRuleException {
        invalid = false;
        try {
            super.setStringRules(s);
        } catch (ParseRuleException ex) {
            NotificationDisplayer.getDefault().notify("Parse error", ERR_ICON, ex.getMessage(), null, NotificationDisplayer.Priority.HIGH);
            invalid = true;
            throw ex;
        }
    }

    /**
     * Try to set Axiom of this LSystem with given string. This method will
     * display notification if parsing fails. In addition, invalid flag will be
     * correctly set.
     *
     * @param axiom
     * @throws ParseRuleException
     */
    @Override
    public void setAxiom(String axiom) throws ParseRuleException {
        invalid = false;
        try {
            super.setAxiom(axiom);
        } catch (ParseRuleException ex) {
            System.err.println("add notif!");
            NotificationDisplayer.getDefault().notify("Parse error", ERR_ICON, ex.getMessage(), null, NotificationDisplayer.Priority.HIGH);
            invalid = true;
            throw ex;
        }
    }

    public void setAxiom(String axiom, boolean notif) throws ParseRuleException {
        invalid = false;
        try {
            super.setAxiom(axiom);
        } catch (ParseRuleException ex) {
            if (notif) {
                NotificationDisplayer.getDefault().notify("Parse error", ERR_ICON, ex.getMessage(), null, NotificationDisplayer.Priority.HIGH);
            }
            invalid = true;
            throw ex;
        }
    }

    protected boolean invalid;

    /**
     * Get the value of invalid
     *
     * @return true if this lsystem is in invalid state.
     */
    public boolean isInvalid() {
        return invalid;
    }

    protected String rawAxiom;
    protected String rawDetails;

    @Override
    public String getRawAxiom() {
        return rawAxiom;
    }

    @Override
    public String getRawDetails() {
        return rawDetails;
    }
    
    /////////////////////SEMENTARA
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setRawAxiom(String x) {
        String old = getRawAxiom();
        this.rawAxiom = x;
        pcs.firePropertyChange(AXIOM_PROP, old, x);
    }
    ///////////////////END OF SEMENTARA
    @Override
    public void setRawDetails(String d) {
        this.rawDetails = d;
    }

}
