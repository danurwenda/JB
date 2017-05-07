/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Scene2Image with antialiasing.
 *
 * @author Dimas Danurwenda
 */
public class PaperScene2Image {

    private final static RequestProcessor RP = new RequestProcessor(PaperScene2Image.class.getName(), 1, true);
    private final File file;
    private final Scene scene;

    /**
     * Creates an instance of a Scene2Image object.
     *
     * @param scene the Scene to be exported as an image.
     * @param file the file to which the image is to be saved. There is no
     * extension check done on the file name. Meaning that a "png" image may be
     * saved with a "txt" extension.
     */
    public PaperScene2Image(Scene scene, File file) {
        this.scene = scene;
        this.file = file;
    }

    /**
     * Takes the Scene and writes an image file according to the constraints
     * defined by the caller. This returns a BufferedImage of the Scene even if
     * the file can not be written.
     *
     * @param imageType The image type to be exported for the image map.
     * @param zoomType Defines the strategy by which to set the exporting scale
     * factor. Note that certain parameters are nullified by the choice of
     * ZoomType. For instance, if ZoomType.CUSTOM_SIZE is not chosen, then the
     * width and height parameters are not used. is assumed that the raw image
     * is to be returned only and not written to a file.
     * @param onPaperOnly
     * @param visibleAreaOnly Eliminates all zoom features. If true, the
     * exported image will be a created from the visible area of the scene.
     * @param paper
     * @param selectedOnly Create an image including only the objects selected
     * on the scene. Note that this feature requires that the scene is an
     * instance of an ObjectScene since it is the implementation that allows for
     * object selection.
     * @param isTransparent
     * @param quality And integer value between 0-100. This is for JPG images
     * only. Parameter is not used if an image type other than jpg is selected.
     * @param width Directly sets the horizontal dimension of the exported
     * image. This is only used when the zoomType is ZoomType.CUSTOM_SIZE
     * @param height Directly sets the vertical dimension of the exported image.
     * This is only used when the zoomType is ZoomType.CUSTOM_SIZE.
     * @return image The raw image that was written to the file.
     * @throws java.io.IOException If for some reason the file cannot be
     * written, an IOExeption will be thrown.
     */
    public BufferedImage createImage(
            SceneExporter.ImageType imageType,
            SceneExporter.ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            boolean onPaperOnly,
            Rectangle paper,
            boolean isTransparent,
            int quality,
            int width,
            int height) throws IOException {

        double _scale = scene.getZoomFactor();

        Rectangle sceneRec = scene.getPreferredBounds();
        Rectangle viewRect = scene.getView() != null ? scene.getView().getVisibleRect() : sceneRec;
        Rectangle paperRect = paper;
        Rectangle selectedRect = new Rectangle();
        Rectangle selectedRectOnView = new Rectangle();

        BufferedImage bufferedImage;
        Graphics2D g;
        ArrayList<Widget> hiddenWidgets = new ArrayList<>();

        int _imageWidth = sceneRec.width;
        int _imageHeight = sceneRec.height;

        Set _selectedObjects;

        if (selectedOnly) {
            //in order to use getSelectedObject the scene must be an ObjectScene
            if (scene instanceof ObjectScene) {
                ObjectScene gScene = (ObjectScene) scene;
                // hide unselected widget
                HashSet<Object> invisible = new HashSet<>();
                //add all object...
                invisible.addAll(gScene.getObjects());
                _selectedObjects = gScene.getSelectedObjects();
                //except the selected
                invisible.removeAll(_selectedObjects);

                //return a rectangle that contains all selected object
                //get the widget of the first selected object
                Rectangle f = null;
                Iterator it = _selectedObjects.iterator();
                while (f == null && it.hasNext()) {
                    Widget first = gScene.findWidget(it.next());
                    if (!(first instanceof LayerWidget)) {
                        f = first.getPreferredBounds();
                        Point fp = first.getPreferredLocation();
                        if (f != null && fp != null) {
                            f.translate(fp.x, fp.y);
                        }
                    }
                }
                if (f != null) {
                    while (it.hasNext()) {
                        Widget w = gScene.findWidget(it.next());
                        if (!(w instanceof LayerWidget)) {
                            Rectangle r = w.getPreferredBounds();
                            Point p = w.getPreferredLocation();
                            if (r != null && p != null) {
                                r.translate(p.x, p.y);
                                f.add(r);
                            }
                        }
                    }

                    selectedRect = new Rectangle(f);
                    selectedRectOnView = scene.convertSceneToView(selectedRect);
                }
                //hide the rest
                //THE PROBLEM IS : invisible array may contain layerWidget that is parent of one of selectedWidget
                //MAKE THEM INVISIBLE WILL MAKE ALL THEIR CHILDREN INVISIBLE
                //that's why we exclude those layerwidgets
                for (Object o : invisible) {
                    Widget widget = gScene.findWidget(o);
                    if (widget != null && widget.isVisible() && (!(widget instanceof LayerWidget))) {
                        widget.setVisible(false);
                        hiddenWidgets.add(widget);
                    }
                }
            }
        }

        if (visibleAreaOnly) {
            _imageWidth = viewRect.width;
            _imageHeight = viewRect.height;
        } else if (onPaperOnly) {
            switch (zoomType) {
                case CUSTOM_SIZE:
                    break;
                case FIT_IN_WINDOW:
                    break;
                case CURRENT_ZOOM_LEVEL:
                    _imageWidth = (int) (paperRect.width * scene.getZoomFactor());
                    _imageHeight = (int) (paperRect.height * scene.getZoomFactor());
                    break;
                case ACTUAL_SIZE:
                    _imageWidth = paperRect.width;
                    _imageHeight = paperRect.height;
                    _scale = 1.0;
                    break;
            }
        } else {
            switch (zoomType) {
                case CUSTOM_SIZE:
                    _imageWidth = width;
                    _imageHeight = height;
                    _scale = Math.min((double) width / (double) sceneRec.width,
                            (double) height / (double) sceneRec.height);
                    break;
                case FIT_IN_WINDOW:
                    _scale = Math.min((double) viewRect.width / (double) sceneRec.width,
                            (double) viewRect.height / (double) sceneRec.height);
                    _imageWidth = (int) ((double) sceneRec.width * _scale);
                    _imageHeight = (int) ((double) sceneRec.height * _scale);
                    break;
                case CURRENT_ZOOM_LEVEL:
                    _imageWidth = (int) (sceneRec.width * scene.getZoomFactor());
                    _imageHeight = (int) (sceneRec.height * scene.getZoomFactor());
                    break;
                case ACTUAL_SIZE:
                    //to lib?
                    _imageWidth = selectedRect.width;
                    _imageHeight = selectedRect.height;
                    _scale = 1.0;
                    break;
            }
        }

        if (_imageWidth <= 0 || _imageHeight <= 0) {
            bufferedImage = null;
        } else {
            if (!isTransparent) {
                // bi creation below might cause OutOfMemory
                bufferedImage = new BufferedImage(_imageWidth, _imageHeight, BufferedImage.TYPE_INT_RGB);
                g = bufferedImage.createGraphics();
            } else {
                bufferedImage = new BufferedImage(_imageWidth, _imageHeight, BufferedImage.TYPE_INT_ARGB);
                g = bufferedImage.createGraphics();
                Composite ori = g.getComposite();
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, _imageWidth, _imageHeight);
                g.setComposite(ori);
                scene.setOpaque(false);
            }
            //these next two lines create a smooth result
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (visibleAreaOnly) {
                g.translate(-viewRect.x, -viewRect.y);
            } else if (onPaperOnly) {
                switch (zoomType) {
                    case CUSTOM_SIZE:
                        break;
                    case FIT_IN_WINDOW:
                        break;
                    case CURRENT_ZOOM_LEVEL:
                        Point p = scene.convertSceneToView(paperRect.getLocation());
                        g.translate(-p.x, -p.y);
                        break;
                    case ACTUAL_SIZE:
                        Point sceneLoc = scene.getLocation();
                        g.translate(-paperRect.x - sceneLoc.x, -paperRect.y - sceneLoc.y);
                        break;
                }
            } else if (selectedOnly) {
                //assume it's ACTUAL_SIZE, for exporting to lib
                double xt = -selectedRectOnView.x / scene.getZoomFactor();
                double yt = -selectedRectOnView.y / scene.getZoomFactor();
                g.translate(xt, yt);
            } else {
                g.translate(0, 0);
            }
            g.scale(_scale, _scale);
            scene.paint(g);
            g.dispose();
        }

        // restore selected widget position
//            for (Widget w : selectedWidgets) {
//                Point l = w.getPreferredLocation();
//                l.translate(pack.x, pack.y);
//                w.setPreferredLocation(l);
//            }
        // restore widget visibility
        for (Widget w : hiddenWidgets) {
            w.setVisible(true);
        }

        if (bufferedImage != null) {
            if (file != null) {
                //this part may take a long time

                if (imageType == SceneExporter.ImageType.PNG) {
                    FileObject fobj = FileUtil.createData(file);
                    //try to lock the file
                    FileLock fLock = fobj.lock();
                    FileImageOutputStream fo = new FileImageOutputStream(file);
                    final RequestProcessor.Task theTask = RP.create(() -> {
                        try {
                            ImageIO.write(bufferedImage, "" + imageType, fo);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });

                    final ProgressHandle ph = ProgressHandleFactory.createHandle("write image to " + file.getName(), theTask);
                    theTask.addTaskListener((Task task) -> {
                        //make sure that we get rid of the ProgressHandle
                        //when the task is finished
                        ph.finish();
                        try {
                            fo.close();
                        } catch (IOException ioe) {
                        } finally {
                            fLock.releaseLock();
                        }
                    });

                    //start the progresshandle the progress UI will show 500s after
                    ph.start();
                    //this actually start the task
                    theTask.schedule(0);
                }

            }
        }
// restore scene opacity
        scene.setOpaque(true);
        return bufferedImage;

    }

}
