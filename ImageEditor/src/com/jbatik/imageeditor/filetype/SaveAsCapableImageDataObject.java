/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.filetype;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.LibraryScene;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * DataObject with SaveAsCapable to replace the default ImageDataObject.
 * Recognize PNG files only (so far).
 *
 * @author Dimas Y. Danurwenda
 */
@NbBundle.Messages("CTL_SaveAsCapableImageFiles=Image Files with SaveAs")
@MIMEResolver.ExtensionRegistration(
        displayName = "#CTL_SaveAsCapableImageFiles",
        mimeType = "image/x-saveascapableimagefiles",
        extension = {
            "png",
            //            "bmp",
            //            "jpg",
            "PNG", //            "BMP",
        //            "JPG",
        //            "jpeg",
        //            "JPEG",
        //            "TGA",
        //            "tga",
        //            "svg",
        //            "SVG"
        })
@DataObject.Registration(iconBase = "com/jbatik/imageeditor/resources/image_editor.png", displayName = "#CTL_SaveAsCapableImageFiles", mimeType = "image/x-saveascapableimagefiles")
public class SaveAsCapableImageDataObject extends MultiDataObject {

    public SaveAsCapableImageDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {

            @Override
            public void saveAs(FileObject folder, String name) throws IOException {
                LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
                scene.saveAs(folder, name);
            }
        });
    }

    @Override
    public int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

}
