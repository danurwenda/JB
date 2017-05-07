/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import com.jbatik.modules.layout.node.LayoutFileUtil;
import com.jbatik.core.project.JBatikProject;
import com.jbatik.filetype.lay.LayoutDataObject;
import com.jbatik.imageeditor.ImageLibEditorTopComponent;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author RAPID02
 */
public class LibraryPalette {

    public static PaletteController createPalette(JBatikProject project) {
        //get Libraries directory
        FileObject libdir = LayoutFileUtil.getLibrariesFolder(project, true);
        /**
         * Refresh content of libdir. The FileObject was init'ed when
         * constructing the project tree, where a snapshot of the directory was
         * taken. This snapshot will be used forever until next refresh(), that
         * is, if we create any lib USING the application (not using external
         * app s.t Windows explorer) AFTER opening the project and BEFORE
         * opening any of .lay file, that new library won't be displayed since
         * it didn't exist yet when the snapshot is taken.
         */
        libdir.refresh();

        final Node libDirNode = DataFolder.findFolder(libdir).getNodeDelegate();
        Node paletteRoot = new PaletteRoot(Children.create(
                new ChildFactory<String>() {

                    @Override
                    protected boolean createKeys(List<String> toPopulate) {
                        toPopulate.add("Eraser");
                        toPopulate.add("PNG Files");
                        return true;
                    }

                    @Override
                    protected Node createNodeForKey(final String key) {
                        //for "Image Files" category, we will look under the
                        //libs directory and find all PNG files inside it
                        switch (key) {
                            case "PNG Files":
                                return new LibraryPNGCategoryNode(libDirNode);
                            case "Eraser":
                                return new LibraryEraserCatNode();
                        }
                        return null;
                    }
                }, true)
        );
        paletteRoot.setName("Neverseen");
//        return PaletteFactory.createPalette(paletteRoot, new MyActions());
        return PaletteFactory.createPalette(
                paletteRoot, //root node
                new MyActions(), //list of action
                null, //palettefilter
                new MyDnDHandler());//dnd handler
    }

    public static PaletteController createPalette(LayoutDataObject obj) {
        return createPalette(FileOwnerQuery.getOwner(obj.getPrimaryFile()).getLookup().lookup(JBatikProject.class));
    }

    private static class MyActions extends PaletteActions {

        @Override
        public Action[] getImportActions() {
            return null;
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return null;
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup lookup) {
            return null;
        }

        @Override
        public Action[] getCustomItemActions(Lookup lookup) {
            DataObject maybe = lookup.lookup(DataObject.class);
            if (maybe != null) {
//                Action ori = Actions.forID("Edit", "pixelhead.OpenEditorAction");
//                if (ori instanceof ContextAwareAction) {
//                    ContextAwareAction caa = (ContextAwareAction) ori;
//                    Action a = caa.createContextAwareInstance(lookup);
//                    return new Action[]{a};
//                }
                return new Action[]{new OpenImageEditorAction2(maybe), new DeleteLibraryAction(maybe)};
            }
            return null;
        }

        @Override
        public Action getPreferredAction(Lookup lookup) {
            DataObject maybe = lookup.lookup(DataObject.class);
            if (maybe != null) {
//                Action ori = Actions.forID("Edit", "pixelhead.OpenEditorAction");
//                if (ori instanceof ContextAwareAction) {
//                    ContextAwareAction caa = (ContextAwareAction) ori;
//                    Action a = caa.createContextAwareInstance(lookup);
//                    return new Action[]{a};
//                }
                return new OpenImageEditorAction2(maybe);
            }
            return null;
        }
    }

    private static class OpenImageEditorAction2 extends AbstractAction {

        private final DataObject context;

        public OpenImageEditorAction2(DataObject context) {
            super("Edit Image");
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ImageLibEditorTopComponent display = new ImageLibEditorTopComponent();
            try {
                display.setEditedImage(context);
                display.open();
                display.requestActive();
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private static class MyDnDHandler extends DragAndDropHandler {

        @Override
        public boolean canDrop(Lookup targetCategory, DataFlavor[] flavors, int dndAction) {
            for (DataFlavor flavor : flavors) {
                if (DataFlavor.imageFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void customize(ExTransferable exTransferable, Lookup lookup) {
            final Node node = lookup.lookup(Node.class);
            final Image draggedSnippet = node.getIcon(BeanInfo.ICON_COLOR_16x16);
            exTransferable.put(new ExTransferable.Single(DataFlavor.imageFlavor) {
                @Override
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return draggedSnippet;
                }
            });
            exTransferable.put(new ExTransferable.Single(DataFlavor.stringFlavor) {
                //returns path to image
                @Override
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return node.getShortDescription();
                }
            });
        }
    }

    private static class DeleteLibraryAction extends AbstractAction {

        private final DataObject context;

        public DeleteLibraryAction(DataObject maybe) {
            super("Delete Library");
            this.context = maybe;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                context.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
