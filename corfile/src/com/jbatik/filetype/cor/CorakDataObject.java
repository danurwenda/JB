/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.filetype.cor;

import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

@Messages({
    "LBL_Cor_LOADER=Files of Cor"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Cor_LOADER",
        mimeType = "text/cor+xml",
        extension = {"cor", "COR"}
)
@DataObject.Registration(
        mimeType = "text/cor+xml",
        iconBase = "com/jbatik/filetype/cor/cor-icon.png",
        displayName = "#LBL_Cor_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/cor+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    )
})
public class CorakDataObject extends MultiDataObject {

    private Lookup lookup;
    private CorakEditorSupport editorSupport;
    private InstanceContent ic = new InstanceContent();

    public CorakDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        lookup = new ProxyLookup(new AbstractLookup(ic), getCookieSet().getLookup());
        ic.add(ic);
        registerEditor("text/cor+xml", true);
        //we still need to add our own implementation to save the data object
        //to the cookieset
//        registerSecondaryEditor();

    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private void registerSecondaryEditor() {
        if (editorSupport == null) {
            editorSupport = new CorakEditorSupport(this);
        }
        ic.add(editorSupport);
    }

    public void addLookUp(Object cor) {
        ic.add(cor);
    }

    public void removeLookUp(Object cor) {
        ic.remove(cor);
    }
    
    @StaticResource
    private static final String ICON_PATH = "com/jbatik/filetype/cor/cor-icon.png";
    public static Icon getIcon(){
        return ImageUtilities.loadImageIcon(ICON_PATH, true);
    }

}
