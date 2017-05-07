/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor;

import com.jbatik.canvas.util.PaperScene2Image;
import com.jbatik.canvas.visual.CanvasWidget;
import com.jbatik.canvas.visual.DesktopScene;
import com.jbatik.canvas.visual.actions.OnAxisMoveStrategy;
import com.jbatik.canvas.visual.actions.RotateSquareMouseAction;
import com.jbatik.canvas.visual.actions.RotateProvider;
import com.jbatik.util.ImageUtil;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class LibraryScene extends DesktopScene {

    DataObject dataObject;
    ImageCanvasWidget paperWidget;
    LayerWidget mainLayer;
    ImageLibraryWidget imageWidget;

    public ImageLibraryWidget getImageWidget() {
        return imageWidget;
    }

    LibraryScene(DataObject file) throws IOException {
        super();
        BufferedImage image;
        try (InputStream is = file.getPrimaryFile().getInputStream()) {
            image = ImageIO.read(is);
        }
        assert image != null;

        this.dataObject = file;
        getPriorActions().addAction(new Shift());

        //transparent paper layer
        this.paperWidget = new ImageCanvasWidget(this, image.getWidth(), image.getHeight());
        paperLayer.addChild(paperWidget);
        //main layer, will contain at most one image widget
        mainLayer = new LayerWidget(this);
        addChild(mainLayer);
        imageWidget = new ImageLibraryWidget(this, image);
        //it is the one and only widget that must be in selected state
        imageWidget.setState(imageWidget.getState().deriveSelected(true));
        //add actions, these actions might trigger savable
        imageWidget.getActions().addAction(new RotateSquareMouseAction(new ILWRotator(), false));
        //note that we set the initial location of imageWidget to be at the center
        //of canvas
        imageWidget.setPreferredLocation(new Point(image.getWidth() / 2, image.getHeight() / 2));
        mainLayer.addChild(imageWidget);
        ic.add(new FixCenterZoom(1));
        createView();
    }

    public static final String MOVE_TOOL = "Move";
    public static final String MOVE_AXIS_TOOL = "MoveAxis";

    /**
     * On Test. Conflicting with Shift/Ctrl gesture on selecting multiple layer
     * on navigator panel.
     */
    private final class Shift extends WidgetAction.Adapter {

        @Override
        public WidgetAction.State keyPressed(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                switch (getActiveTool()) {
                    case MOVE_TOOL:
                        setActiveTool(MOVE_AXIS_TOOL);
                        break;
                }
            }
            return WidgetAction.State.REJECTED;
        }

        @Override
        public WidgetAction.State keyReleased(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                switch (getActiveTool()) {
                    case MOVE_AXIS_TOOL:
                        setActiveTool(MOVE_TOOL);
                        break;
                }
            }
            return WidgetAction.State.REJECTED;
        }
    }

    private WidgetAction moveAction = ActionFactory.createMoveAction(ActionFactory.createFreeMoveStrategy(), new MoveWithSavable());
    private WidgetAction moveAxisAction = ActionFactory.createMoveAction(new OnAxisMoveStrategy(), new MoveWithSavable());

    public WidgetAction getMoveAction() {
        return moveAction;
    }

    public WidgetAction getMoveAxisAxtion() {
        return moveAxisAction;
    }

    private AbstractLookup al = new AbstractLookup(ic);
    private Lookup lookup;

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new ProxyLookup(Lookups.singleton(this), al, dataObject.getLookup());
        }
        return lookup;
    }

    public void addSavable() {
        if (null == getLookup().lookup(MySavable.class)) {
            ic.add(new MySavable(this));
        }
    }

    @Override
    protected Rectangle getWorkingRectangle(boolean withPaper) {
        Rectangle working = null;
        if (withPaper) {
            working = paperWidget.getPreferredBounds();
        }
        for (Widget w : mainLayer.getChildren()) {
            Point wLoc = w.getPreferredLocation();
            Rectangle wRect = w.getBounds();
            if (wRect != null) {
                wRect.translate(wLoc.x, wLoc.y);
                if (working == null) {
                    working = wRect;
                }
                working.add(wRect);
            }
        }
        return working;
    }

    public void setPaperDimension(int newWidth, int newHeight) {
        Dimension old = paperWidget.getDimension();
        Point oldCenter = new Point(old.width / 2, old.height / 2);
        Point newCenter = new Point(newWidth / 2, newHeight / 2);
        Point oldImagePos = imageWidget.getPreferredLocation();
        Point newImagePos = new Point(oldImagePos.x + newCenter.x - oldCenter.x, oldImagePos.y + newCenter.y - oldCenter.y);
        //translate imageWidget
        imageWidget.setPreferredLocation(newImagePos);
        paperWidget.setDimension(newWidth, newHeight);
        //trigger savable
        addSavable();
    }

    @Override
    protected CanvasWidget getCanvasWidget() {
        return paperWidget;
    }

    public void saveAs(FileObject folder, String fileName) throws IOException {

        FileObject currentFile = dataObject.getPrimaryFile();
        String newExt = FileUtil.getExtension(fileName);
        if (!newExt.equals(currentFile.getExt())) {
            newExt = currentFile.getExt();
        } else {
            newExt = "";
        }
        //check apakah target already exists
        FileObject writingTarget = folder.getFileObject(fileName, newExt);

        //check apakah ada savable
        if (null != getLookup().lookup(MySavable.class)) {
            //ada savable
            paperWidget.setVisible(false);
            Border prev = imageWidget.getBorder();
            imageWidget.setBorder(BorderFactory.createEmptyBorder());
            if (!isValidated()) {
                if (getView() != null) {
                    validate();
                } else {
                    BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D emptyGraphics = emptyImage.createGraphics();
                    validate(emptyGraphics);
                }
            }
            File file;
            if (writingTarget != null) {
                //create stream from existing file
                file = FileUtil.toFile(writingTarget);
            } else {
                //create new file object
                writingTarget = folder.createData(fileName, newExt);
                file = FileUtil.toFile(writingTarget);
            }
            PaperScene2Image s2i = new PaperScene2Image(this, file);
            s2i.createImage(SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, false, false, true, paperWidget.getPreferredBounds(), true, 0, 0, 0);

            //restore conditions
            imageWidget.setBorder(prev);
            paperWidget.setVisible(true);
        } else {
            if (writingTarget != null) {
                try (
                        OutputStream output = writingTarget.getOutputStream();
                        InputStream input = currentFile.getInputStream()) {
                    byte[] buffer = new byte[4096];

                    while (input.available() > 0) {
                        output.write(buffer, 0, input.read(buffer));
                    }
                }
            } else {
                //just copy current file as another file
                writingTarget = currentFile.copy(folder, fileName, newExt);
            }
        }
        while (writingTarget.isLocked()) {
            //wait until finish
        }
        folder.refresh(true);
    }
    /////////////////COLOR SHIFTING
    private BufferedImage shifted;
    private BufferedImage temp;

    public void colorShifting(int h, int s, int v) {
        if (temp == null) {
            temp = imageWidget.getOriginalImage();
        }
        shifted = ImageUtil.shiftHSV(temp, h, s, v);
        imageWidget.setTempImage(shifted);
        validate();
    }

    public void revertColorShifting() {
        imageWidget.setTempImage(null);
        temp = null;
        shifted = null;
    }

    public void commitColorShifting() {
        //TODO : create undo-redo
        //inject color from temp to imageWidget
        imageWidget.setImage(shifted);
        //flush temp image
        revertColorShifting();
    }

    /**
     * Savable ini diinject ke lookup nya scene, which in turn bakal included di
     * lookup nya TC
     */
    public class MySavable extends AbstractSavable {

        LibraryScene ls;

        private MySavable(LibraryScene s) {
            this.ls = s;
            register();
        }

        @Override
        protected String findDisplayName() {
            return dataObject.getPrimaryFile().getNameExt();
        }

        @Override
        protected void handleSave() throws IOException {
            //write to file
            ls.paperWidget.setVisible(false);
            Border prev = ls.imageWidget.getBorder();
            ls.imageWidget.setBorder(BorderFactory.createEmptyBorder());
            if (!ls.isValidated()) {
                if (ls.getView() != null) {
                    ls.validate();
                } else {
                    BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D emptyGraphics = emptyImage.createGraphics();
                    ls.validate(emptyGraphics);
                }
            }
            FileObject target = dataObject.getPrimaryFile();
            File file = FileUtil.toFile(target);
            PaperScene2Image s2i = new PaperScene2Image(ls, file);
            s2i.createImage(SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, false, false, true, ls.paperWidget.getPreferredBounds(), true, 0, 0, 0);
            while (target.isLocked()) {
                //wait until writing to disk task is finished
            }
            target.refresh(true);
            //restore conditions
            ls.imageWidget.setBorder(prev);
            ls.paperWidget.setVisible(true);
            //remove savable, since we're done using it
            ls.ic.remove(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MySavable) {
                MySavable imageSavable = (MySavable) obj;
                return imageSavable.ls == ls;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return ls.hashCode();
        }

        void discard() {
            unregister();
        }
    }

    /**
     * Rotate provider ini gunanya cuma mengubah rotationAngle
     */
    private class ILWRotator implements RotateProvider {

        private double originalAngle;

        public ILWRotator() {
        }

        @Override
        public void rotateStarted(Widget w) {
            originalAngle = ((ImageLibraryWidget) w).getRotationAngle();
        }

        @Override
        public void rotateFinished(Widget w) {
            if (originalAngle != ((ImageLibraryWidget) w).getRotationAngle()) {
                addSavable();
            }
        }

        @Override
        public Point getAnchorPointOnScene(Widget w, Point p) {
            return w.convertLocalToScene(new Point());
        }

        @Override
        public void applyRotation(Widget w, double d) {
            ImageLibraryWidget ilw = (ImageLibraryWidget) w;
            ilw.setRotationAngle(originalAngle + d);
            ilw.revalidate();
        }
    }

    private class MoveWithSavable implements MoveProvider {

        private Point original;

        @Override
        public void movementStarted(Widget widget) {
            original = widget.getPreferredLocation();
        }

        @Override
        public void movementFinished(Widget widget) {
            if (!original.equals(widget.getPreferredLocation())) {
                addSavable();
            }
        }

        @Override
        public Point getOriginalLocation(Widget widget) {
            return widget.getPreferredLocation();
        }

        @Override
        public void setNewLocation(Widget widget, Point location) {
            widget.setPreferredLocation(location);
        }
    }

}
