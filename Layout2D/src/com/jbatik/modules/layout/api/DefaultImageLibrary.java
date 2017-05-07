/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.api;

import com.jbatik.util.ImageUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * LayoutLibrary implementation for image file on disk. If the whole image
 * object can be loaded onto memory at once, this class is a suitable tool.
 * Otherwise, a scaled-down image will be used instead.
 *
 * @author RAPID02
 */
public class DefaultImageLibrary implements LayoutLibrary {

    public static final int MAX_PIXEL_NUM = 4000000;
    BufferedImage image, mirrorimage;
    boolean scaledDown = false;
    FileObject source;
    String missingImage;

    public DefaultImageLibrary(FileObject fo) {
        assert fo != null;
        this.source = fo;
        missingImage=null;
        loadImage();
        listenTo(fo);
        listenToParent(fo.getParent());
    }

    @Override
    public boolean isMissing() {        
        return missingImage != null;
    }

    /**
     * This constructor is called only on missing image
     *
     * @param parent
     * @param name
     */
    public DefaultImageLibrary(FileObject parent, String name) {
        missingImage = name;
        listenToParent(parent);
    }

    private void listenTo(FileObject fo) {
        //Add ourselves as a weak listener to the file.  This way we can still
        //be garbage collected if the project is closed
        FileChangeListener stub = WeakListeners.create(
                FileChangeListener.class, forImage, fo);
        // adding FileChangeListener to a FileObject
        fo.addFileChangeListener(stub);
    }

    private void listenToParent(FileObject fo) {
        //Add ourselves as a weak listener to the file.  This way we can still
        //be garbage collected if the project is closed
        FileChangeListener stub = WeakListeners.create(
                FileChangeListener.class, forParent, fo);
        // adding FileChangeListener to a FileObject
        fo.addFileChangeListener(stub);
    }

    @Override
    public void drawLibrary(Graphics2D g2, double length, boolean mirror) {
        if (image != null) {
            int x, y, wres, hres;
            int oriw = image.getWidth(null);
            int orih = image.getHeight(null);
            double oriRat = (oriw * 1.0) / (orih * 1.0);
            double resRat = 1.0;
            if (oriRat == resRat) {
                x = 0;
                y = 0;
                wres = (int) length;
                hres = (int) length;
            } else if (resRat > oriRat) {
                y = 0;
                hres = (int) length;
                wres = oriw * (int) length / orih;
                x = ((int) length - wres) / 2;
            } else {
                x = 0;
                wres = (int) length;
                hres = orih * (int) length / oriw;
                y = ((int) length - hres) / 2;
            }
            //sometimes the Graphics2D.drawImage method throw OutOfMemoryError
            //just because of some buffering stuffs
            //so we just try again and again, hoping someday the memory will be sufficient
            boolean draw = false;
            while (!draw) {
                try {
                    if (mirror) {
                        if (mirrorimage == null) {
                            AffineTransform op = AffineTransform.getScaleInstance(1, -1);
                            op.rotate(-Math.PI / 2);
                            mirrorimage = new AffineTransformOp(op, null).filter(image, null);
                        }
                        g2.drawImage(mirrorimage, y, x, hres, wres, null);
                    } else {
                        g2.drawImage(image, x, y, wres, hres, null);
                    }
                    draw = true;
                } catch (OutOfMemoryError oom) {
                }
            }
        } else {
            //missing library
            g2.setPaint(Color.RED);
            Rectangle2D box = new Rectangle2D.Double(0, 0, length, length);
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, length, length);
            g2.draw(box);
            g2.draw(circle);
        }
    }

    private final ChangeSupport cs = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    FileChangeListener forParent = new FileChangeListener() {
        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (missingImage == null) {
                //when the source is deleted from the initial location (e.g into recycle bin)
                //and restored afterward, fileDataCreated will be triggered.
                String sp = source.getPath();
                String newPath = fe.getFile().getPath();
                if (sp.equals(newPath)) {
                    //a file has been created with the same path as our source
                    source = fe.getFile();
                    loadImage();
                    cs.fireChange();
                }
            } else {
                //cek siapa tau file yang created ini file yang tadi missing
                String newFile = fe.getFile().getNameExt();
                if (newFile.equals(missingImage)) {
                    //change mode from listening to filename to listening to fileobject
                    missingImage = null;
                    source = fe.getFile();
                    loadImage();
                    listenTo(source);
                    cs.fireChange();
                }
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {

        }

        @Override
        public void fileDeleted(FileEvent fe) {

        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {
            //event rename akan langsung manggil delete dan create. jadi do nothing here.
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {
        }
    };
    FileChangeListener forImage = new FileChangeListener() {
        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {

        }

        @Override
        public void fileChanged(FileEvent fe) {
            //update the image (if possible)
            loadImage();
            //and notify all listeners
            cs.fireChange();
//            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            loadImage();
            cs.fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {
            //event rename akan langsung manggil delete dan create. jadi do nothing here.
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {
        }
    };

    private void loadImage() {
        image = null;
        mirrorimage = null;
        if (source.isValid()) {
            try (InputStream is = source.getInputStream()) {
                if (is != null) {
                    image = ImageIO.read(is);
                } else {
                    image = null;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (image != null) {
            //check whether we need to save the scaled-down image on reference
            //sementara kita pakai dimensi aja dulu
            //max 2000x2000 = 4juta piksel
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            int pixel = w * h;
            if (pixel > MAX_PIXEL_NUM) {
                //create a scaled-down image
                double ratio = pixel / MAX_PIXEL_NUM;
                int ratiort = (int) Math.ceil(Math.sqrt(ratio));
                int neww = w / ratiort;
                int newh = h / ratiort;
                image = (BufferedImage) ImageUtil.getScaledImage(image, neww, newh);
                scaledDown = true;
            }
        }
    }

}
