/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.actions;

import com.jbatik.core.api.ExportAsPNG;
import com.jbatik.core.api.JBatikCanvas;
import com.jbatik.core.api.PNGExportConfiguration;
import com.jbatik.modules.layout.library.ExportableToLibCookie;
import com.jbatik.modules.layout.node.LayoutFileUtil;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
        category = "File",
        id = "com.jbatik.modules.layout.importexport.actions.ExportToLibAction"
)
@ActionRegistration(
        displayName = "#CTL_ExportToLibAction",
        lazy = false
)

@ActionReferences({
    @ActionReference(path = "Menu/File/Export", position = 201),
    @ActionReference(path = "Toolbars/File", position = 502)
})

@Messages("CTL_ExportToLibAction=To Lib...")
public final class ExportToLibAction extends AbstractAction implements LookupListener, ContextAwareAction {

    @StaticResource
    private static final String ICON_IMG = "com/jbatik/modules/layout/library/resources/toLIB24.png";
    private static Icon ICON = ImageUtilities.loadImageIcon(ICON_IMG, false);

    private Lookup context;
    Lookup.Result<ExportAsPNG> exportableAsImage;
    ExportAsPNG exportable;
    JBatikCanvas canvas;
    ExportableToLibCookie exporter;
    boolean libExporter;

    public ExportToLibAction() {
        this(Utilities.actionsGlobalContext());
    }

    public ExportToLibAction(Lookup context) {
        super(Bundle.CTL_ExportToLibAction(), ICON);
        this.context = context;
    }

    void init() {
        if (exportableAsImage != null) {
            return;
        }
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        //The thing we want to listen for the presence or absence of
        //on the global selection
        exportableAsImage = context.lookupResult(ExportAsPNG.class);
        exportableAsImage.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        init();
        //extract path to lib folder
        FileObject libFolder = LayoutFileUtil.getLibrariesFolder(canvas.getProject(), true);
        String ext = "png";
        String newName = FileUtil.findFreeFileName(libFolder, exportable.getDefaultName(), ext);
        File newFile = new File(FileUtil.toFile(libFolder), newName.concat(".").concat(ext));
        //since we use findFreeFileName, it's highly likely that newFile doesn't exist
        if (!libExporter) {
            exportable.writeImage(newFile.getPath(), new PNGExportConfiguration(true, true));
        } else {
            exporter.writeLib(newFile.getPath());
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends ExportAsPNG> c = exportableAsImage.allInstances();
        if (c.isEmpty()) {
            setEnabled(false);
        } else {
            Collection<? extends JBatikCanvas> hasProject = context.lookupResult(JBatikCanvas.class).allInstances();
            if (hasProject.isEmpty()) {
                setEnabled(false);
            } else {
                canvas = hasProject.iterator().next();
                exportable = c.iterator().next();
                Collection<? extends ExportableToLibCookie> real = context.lookupResult(ExportableToLibCookie.class).allInstances();
                if (!real.isEmpty()) {
                    libExporter = true;
                    exporter = real.iterator().next();
                } else {
                    libExporter = false;
                }
                setEnabled(true);
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ExportToLibAction(context);
    }

}
