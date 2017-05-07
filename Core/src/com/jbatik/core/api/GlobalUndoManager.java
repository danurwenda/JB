/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api;

import org.openide.awt.UndoRedo;

/**
 * Shared UndoRedo.Manager instance.
 * @author Dimas Y. Danurwenda
 */
public class GlobalUndoManager {
    private static UndoRedo.Manager activeManager;
    public static UndoRedo.Manager getManager(){
        return activeManager;
    }
    
    public static void setManager(UndoRedo.Manager m){
        activeManager = m;
    }
}
