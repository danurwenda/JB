/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.awt.Rectangle;
import javax.swing.JComponent;
import org.openide.util.RequestProcessor;

/**
 *
 * @author RAPID02
 */
public class ViewCleanerImpl implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(ViewCleanerImpl.class);
    private final RequestProcessor.Task UPDATE = RP.create(this);
    private JComponent view;

    public ViewCleanerImpl(JComponent view) {
        this.view = view;
    }

    void schedule() {
        UPDATE.schedule(100);
    }

    @Override
    public void run() {
        Rectangle visRect = view.getVisibleRect();
        view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
    }
}
