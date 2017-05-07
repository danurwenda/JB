/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.io;

import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.modules.corak.CorakLSystem;
import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author RAPID02
 */
public class CorakSavable extends AbstractSavable implements Icon {

    private CorakLSystem cor;
    private CorakDataObject obj;

    public CorakSavable(CorakLSystem cor, CorakDataObject obj) {
        this.cor = cor;
        this.obj = obj;
        register();
    }

    @Override
    protected String findDisplayName() {
        return obj.getPrimaryFile().getNameExt();
    }

    @Override
    protected void handleSave() throws IOException {
        CorakSerializer.serialize(cor, FileUtil.toFile(obj.getPrimaryFile()));
        obj.getLookup().lookup(InstanceContent.class).remove(CorakSavable.this);
        obj.setModified(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CorakSavable) {
            CorakSavable sav = (CorakSavable) obj;
            return sav.getFullPath().equals(getFullPath());
        }
        return false;
    }

    private String getFullPath() {
        return FileUtil.toFile(obj.getPrimaryFile()).getPath();
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }

    private static final Icon ICON = CorakDataObject.getIcon();

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        ICON.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return ICON.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return ICON.getIconHeight();
    }

}
