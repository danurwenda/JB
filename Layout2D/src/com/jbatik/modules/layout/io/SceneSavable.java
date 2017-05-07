/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.io;

import com.jbatik.filetype.lay.LayoutDataObject;
import com.jbatik.modules.layout.LayoutLSystem;
import java.awt.Component;
import java.awt.Graphics;
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author RAPID02
 */
public class SceneSavable extends AbstractSavable implements Icon {

    private LayoutLSystem lSystem;
    private LayoutDataObject dataObject;

    public void discard() {
        //omitting the line below will let the Save action enabled eventho there's no registered savable
        dataObject.getLookup().lookup(InstanceContent.class).remove(SceneSavable.this);
        dataObject.setModified(false);
        //omitting the line below will let the Save All enabled
        unregister();
    }

    public SceneSavable(LayoutLSystem lSystem, LayoutDataObject dataObject) {
        this.lSystem = lSystem;
        this.dataObject = dataObject;
        register();
    }

    @Override
    protected String findDisplayName() {
        return dataObject.getPrimaryFile().getNameExt();
    }

    @Override
    protected void handleSave() throws IOException {
        //write to file
        LayoutParser.save(lSystem, FileUtil.toFile(dataObject.getPrimaryFile()));
        dataObject.getLookup().lookup(InstanceContent.class).remove(SceneSavable.this);
        dataObject.setModified(false);
    }

    private String getFullPath() {
        return FileOwnerQuery.getOwner(dataObject.getPrimaryFile()).getProjectDirectory().getPath().concat(findDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SceneSavable) {
            SceneSavable sav = (SceneSavable) obj;
            return sav.getFullPath().equals(getFullPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }
    private static final Icon ICON = LayoutDataObject.getIcon();

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
