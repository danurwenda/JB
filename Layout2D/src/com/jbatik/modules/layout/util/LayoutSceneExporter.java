/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.util;

import com.jbatik.canvas.util.PaperScene2Image;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.visual.export.SceneExporter;

/**
 * Modification of class SceneExporter to support antialiasing.
 *
 * @author Dimas Danurwenda
 */
public class LayoutSceneExporter {

    /**
     * Takes the Scene and writes an image file according to the constraints
     * defined by the caller. This returns a BufferedImage of the Scene even if
     * the file can not be written.
     *
     * @param scene The Scene to be exported as an image.
     * @param file The file used to store the exported image. If null, then it
     * is assumed that the raw image is to be returned only and not written to a
     * file.
     * @param SceneExporter.ImageType The image type to be exported for the
     * image map.
     * @param SceneExporter.ZoomType Defines the strategy by which to set the
     * exporting scale factor. Note that certain parameters are nullified by the
     * choice of ZoomType. For instance, if ZoomType.CUSTOM_SIZE is not chosen,
     * then the width and height parameters are not used.
     * @param visibleAreaOnly Eliminates all zoom features. If true, the
     * exported image will be a created from the visible area of the scene.
     * @param selectedOnly Create an image including only the objects selected
     * on the scene. Note that this feature requires that the scene is an
     * instance of an ObjectScene since it is the implementation that allows for
     * object selection.
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
    public static BufferedImage createImage(
            LayoutScene scene,
            File file,
            SceneExporter.ImageType imageType,
            SceneExporter.ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            boolean paperOnly,
            boolean isTransparent,
            int quality, int width,
            int height) throws IOException {

        if (scene == null) {
            return null;
        }
        if (!scene.isValidated()) {
            if (scene.getView() != null) {
                scene.validate();
            } else {
                BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D emptyGraphics = emptyImage.createGraphics();
                scene.validate(emptyGraphics);
            }
        }
        BufferedImage image;

        PaperScene2Image s2i = new PaperScene2Image(scene, file);
        image = s2i.createImage(imageType, zoomType, visibleAreaOnly, selectedOnly, paperOnly, scene.getPaperBounds(), isTransparent, quality, width, height);

        return image;
    }

    /**
     * What you see is what printed. Tested.
     *
     * @param layersOnly if true, then all remarks (e.g selected rectangular
     * remark) will be excluded
     * @return
     */
    public static BufferedImage asIs(LayoutScene s, String path, boolean layersOnly) throws IOException {
        BufferedImage bi = null;
        if (layersOnly) {
            s.showRemarks(false);
            try {
                bi = createImage(s, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL, true, false, false, false, 0, 0, 0);
            } catch (IOException ex) {
                throw ex;
            } finally {
                s.showRemarks(true);
            }
        } else {
            bi = createImage(s, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL, true, false, false, false, 0, 0, 0);
        }
        return bi;
    }

    /**
     * Only print those object above the paper. Tested.
     *
     * @param currentZoom whether to use current zoom level or actual size
     * @return
     */
    public static BufferedImage paperOnly(LayoutScene s, String path, boolean currentZoom) throws IOException {
        BufferedImage bi = null;
        s.showRemarks(false);
        try {
            if (currentZoom) {
                bi = createImage(s, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL, false, false, true, false, 0, 0, 0);
            } else {
                bi = createImage(s, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, false, false, true, false, 0, 0, 0);
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            s.showRemarks(true);
        }
        return bi;
    }

    /**
     * Selected layer only, whole part of it (without paper) will be printed.
     *
     * @param currentZoom whether to use current zoom level or actual size
     * @return
     */
    public BufferedImage selectedOnly(LayoutScene s, String path, boolean currentZoom) {
        return null;
    }

}
