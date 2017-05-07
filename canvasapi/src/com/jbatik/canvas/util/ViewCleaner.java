/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.util.WeakHashMap;
import javax.swing.JComponent;

/**
 * Utility class to repaint a view component after a time interval.
 *
 * @author RAPID02
 */
public class ViewCleaner {

    private static final WeakHashMap<JComponent, ViewCleanerImpl> cleaners = new WeakHashMap<>();

    public static void schedule(JComponent view) {
        ViewCleanerImpl cleaner = cleaners.get(view);
        if (cleaner == null) {
            //create new cleaner
            cleaner = new ViewCleanerImpl(view);
            cleaners.put(view, cleaner);
        }
        cleaner.schedule();
    }

}
