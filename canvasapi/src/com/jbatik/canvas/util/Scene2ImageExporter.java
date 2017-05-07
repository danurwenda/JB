/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.widget.Scene;

/**
 * Modification of class SceneExporter to support antialiasing.
 *
 * @author Dimas Danurwenda
 */
public class Scene2ImageExporter {

    /**
     * Takes the Scene and writes an image file according to the constraints
     * defined by the caller. This returns a BufferedImage of the Scene even if
     * the file can not be written.
     *
     * @param scene The Scene to be exported as an image.
     * @param file The file used to store the exported image. If null, then it
     * is assumed that the raw image is to be returned only and not written to a
     * file.
     * @param imageType SceneExporter.ImageType The image type to be exported for the
     * image map.
     * @param zoomType SceneExporter.ZoomType Defines the strategy by which to set the
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
            Scene scene,
            File file,
            SceneExporter.ImageType imageType,
            SceneExporter.ZoomType zoomType,
            boolean visibleAreaOnly,
            boolean selectedOnly,
            boolean isTransparent,
            int quality,
            int width,
            int height) throws IOException {

        return createImage(scene, file, imageType, zoomType, visibleAreaOnly, selectedOnly, isTransparent, false, null, quality, width, height);
    }

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
    public static BufferedImage createImage(Scene scene, File file, SceneExporter.ImageType imageType, SceneExporter.ZoomType zoomType, boolean visibleAreaOnly, boolean selectedOnly, boolean isTransparent, boolean isPaper, Rectangle paperRect, int quality, int width, int height) throws IOException {
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
        image = s2i.createImage(imageType, zoomType, visibleAreaOnly, selectedOnly, isPaper, paperRect, isTransparent, quality, width, height);
        return image;
    }

}
