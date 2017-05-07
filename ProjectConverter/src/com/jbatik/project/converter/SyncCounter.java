/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.project.converter;

import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class SyncCounter {

    private int c = 0;
    ProgressHandle handle;
    int target;

    SyncCounter(ProgressHandle ph, int size) {
        this.handle = ph;
        this.target = size;
    }

    public synchronized void increment() {
        c++;
        handle.progress(c);
        if (c >= target) {
            handle.finish();
        }
    }
}
