/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import com.jbatik.util.ImageUtil;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Here we override the way PNGLibNode is presented, from a default file Node to
 * a more representative palette item node.
 *
 * @author RAPID02
 */
public class PNGLibNode extends AbstractNode implements Runnable, FileChangeListener {

    private final String libDirPath;
    private boolean cached;
    private static final RequestProcessor THREAD_POOL = new RequestProcessor("PNGLibNode Image Generator", 3);
    private final RequestProcessor.Task task = THREAD_POOL.create(this);
    private DataObject source;

    public PNGLibNode(FileObject libDir, Node original) {
        //we put the original DataObject from this PNG file into lookup
        //biar bisa di reorder
        super(org.openide.nodes.Children.LEAF, Lookups.fixed(original.getLookup().lookup(DataObject.class)));
        source = getLookup().lookup(DataObject.class);
        listenTo(source.getPrimaryFile());
        listenTo(libDir);
        //set display name even tho it's not displayed
        //for the sake of searching
        setDisplayName(original.getDisplayName());
        setShortDescription(original.getDisplayName());
        this.libDirPath = libDir.getPath();
        this.cached = iconCache.get(libDirPath + getShortDescription()) != null;
    }

    private void listenTo(FileObject fo) {
        //Add ourselves as a weak listener to the file.  This way we can still
        //be garbage collected if the project is closed
        FileChangeListener stub = WeakListeners.create(
                FileChangeListener.class, this, fo);
        // adding FileChangeListener to a FileObject
        fo.addFileChangeListener(stub);
    }

    @Override
    public Image getIcon(int t) {
        return cached ? iconCache.get(libDirPath + getShortDescription()) : getWaitIcon();
    }

    //caching image
    private static final Map<String, Image> iconCache = new HashMap<>(128);
    //TODO : listen when file changed using FileChangedListener

    private Image getImageFromPath() throws IOException {
        File libDir = new File(libDirPath);

        FileObject libDirFO = FileUtil.toFileObject(libDir);
        FileObject imageFO = libDirFO.getFileObject(
                // the hell man, I don wanna put another field
                getShortDescription()
        );
        Image image = null;
        if (imageFO != null) {
            while (imageFO.isLocked()) {
                //wait until it's available
            }
            try (InputStream inputStream = imageFO.getInputStream()) {
                return ImageUtil.getSquaredImage(inputStream, 80);
            }
        }
        return image;
    }

    @StaticResource
    private final String WAIT_ICON = "com/jbatik/modules/layout/library/palette/resources/loading.png";
    private Image waitIcon;

    private Image getWaitIcon() {
        if (waitIcon == null) {
            waitIcon = ImageUtilities.loadImage(WAIT_ICON);
        }
        task.schedule(0);
        return waitIcon;
    }

    /**
     * Start a thread to create library icon image from given path. The image
     * will then inserted into iconCache.
     */
    private void generateImage() {
        Image cachedImg = null;
        try {
            //could be expensive and takes a looong time
            cachedImg = getImageFromPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (cachedImg != null) {
            synchronized (iconCache) {
                iconCache.put(libDirPath + getShortDescription(), cachedImg);
                cached = true;
                fireIconChange();
            }
        }
    }

    @Override
    public void run() {
        if (!cached) {
            generateImage();
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        //if the file that has just created is our file..
        if (fe.getFile().getPath().equals(source.getPrimaryFile().getPath())) {
            cached = false;
            fireIconChange();
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        //if the file that has just changed is our file..
        if (fe.getFile().getPath().equals(source.getPrimaryFile().getPath())) {
            cached = false;
            fireIconChange();
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        //if the file that has just deleted is our file..
        if (fe.getFile().getPath().equals(source.getPrimaryFile().getPath())) {
            //delete from cache
            synchronized (iconCache) {
                iconCache.remove(libDirPath + getShortDescription());
            }
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
