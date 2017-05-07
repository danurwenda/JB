/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.filetype.lay;

import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

@Messages({
    "LBL_Lay_LOADER=Files of Lay"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Lay_LOADER",
        mimeType = "text/lay+xml",
        extension = {"lay", "LAY"}
)
@DataObject.Registration(
        mimeType = "text/lay+xml",
        iconBase = "com/jbatik/filetype/lay/lay-icon.png",
        displayName = "#LBL_Lay_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/lay+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    )
})
public class LayoutDataObject extends MultiDataObject {

    private Lookup lookup;
    private InstanceContent ic = new InstanceContent();
    LayoutEditorSupport support = new LayoutEditorSupport(this);

    public LayoutDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        lookup = new ProxyLookup(new AbstractLookup(ic), getCookieSet().getLookup());
        //the hack below is necessary. See LayoutScene.addSavable() and LayoutEditorSupport.saveAs()
        ic.add(ic);
        registerEditor("text/lay+xml", true);
        assignSAC();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
    @StaticResource
    private static final String ICON_PATH = "com/jbatik/filetype/lay/lay-icon.png";

    public static Icon getIcon() {
        return ImageUtilities.loadImageIcon(ICON_PATH, true);
    }

    private void assignSAC() {
        //save the original OpenCookie (actually we get SimpleES here)
        OpenCookie oc  = getCookie(OpenCookie.class);
        getCookieSet().assign(SaveAsCapable.class, support);
        getCookieSet().assign(OpenCookie.class, oc);
    }
}
