/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor;

import com.jbatik.canvas.util.GeomUtil;
import com.jbatik.canvas.visual.CanvasWidget;
import com.jbatik.util.ImageUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import org.netbeans.api.visual.widget.Widget;

/**
 * Widget represent the edited image. Bisa digeser, diresize, diputar2.
 *
 * @author Dimas Y. Danurwenda
 */
public class ImageLibraryWidget extends Widget {

    private final BufferedImage image;
    private BufferedImage tempImage;
    //rotasi dan resize mengubah transformasi
    //tapi geser engga, cuma ngubah posisi
    private double rotationAngle;//clockwise

    ImageLibraryWidget(LibraryScene aThis, BufferedImage image) {
        super(aThis);
        assert image != null;
        this.image = image;
        this.rotationAngle = 0;
        this.xScale = 1;
        this.yScale = 1;
        this.yFlip = false;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public double getxScale() {
        return xScale;
    }

    public void setxScale(double xScale) {
        this.xScale = xScale;
    }

    public double getyScale() {
        return yScale;
    }

    public void setyScale(double yScale) {
        this.yScale = yScale;
    }
    private boolean yFlip;
    private double xScale, yScale;
//    private double xShear, yShear;

    @Override
    protected void paintWidget() {
        paintOnGraphics(getGraphics(), true);
    }

    /**
     * Returns the smallest rectangle containing the getImage() in current
     * transformation.
     *
     * @return the calculated client area
     */
    @Override
    protected Rectangle calculateClientArea() {
        Rectangle2D.Double ori = new Rectangle2D.Double(0, 0, xScale * getImage().getWidth(null), yScale * getImage().getHeight(null));
        Point2D.Double oriTL = new Point2D.Double(-0.5 * ori.getWidth(), -0.5 * ori.getHeight());
        Point2D.Double rotatedTopLeft = GeomUtil.rotate_point(oriTL.x, oriTL.y, Math.toRadians(rotationAngle), 0, 0);
        AffineTransform trans = AffineTransform.getTranslateInstance(rotatedTopLeft.x, rotatedTopLeft.y);
        trans.rotate(Math.toRadians(rotationAngle));
        return trans.createTransformedShape(ori).getBounds();
    }

    void doXFlip() {
        yFlip = !yFlip;
        rotationAngle = 180 - rotationAngle;
    }

    void doYFlip() {
        yFlip = !yFlip;
        rotationAngle *= -1;
    }

    public BufferedImage getSnapshot() {
        Rectangle area = calculateClientArea();
        BufferedImage temp = ImageUtil.createClearImage(area.width, area.height);
        Graphics2D g2 = (Graphics2D) temp.getGraphics();
        //then paint the image using its transformation
        g2.translate(area.width / 2, area.height / 2);
        paintOnGraphics(g2, false);
        g2.dispose();
        return temp;
    }

    /**
     * Urutan transformasi adalah xFlip & yFlip (yang tidak mengubah bounds)
     * lalu xScale & yScale (yang mengakibatkan width/height berubah) baru
     * kemudian rotate.
     */
    private void paintOnGraphics(Graphics2D g2, boolean transOut) {
        //FLIP OPERATION
        BufferedImage drawn = getImage();
        if (yFlip) {
            //so far it's the fastest algo to flip
            AffineTransform op = AffineTransform.getScaleInstance(1, -1);
            op.translate(0, -getImage().getHeight(null));
            drawn = new AffineTransformOp(op, null).filter(getImage(), null);
        }
        AffineTransform prev = g2.getTransform();
        if (!transOut) {
            //SCALE OPERATION
            g2.scale(xScale, yScale);
            //ROTATE OPERATION
            Point2D.Double oriTL = new Point2D.Double(-0.5 * xScale * getImage().getWidth(null), -0.5 * yScale * getImage().getHeight(null));
            Point2D.Double rotatedTopLeft = GeomUtil.rotate_point(oriTL.x, oriTL.y, Math.toRadians(rotationAngle), 0, 0);
            g2.translate(rotatedTopLeft.x, rotatedTopLeft.y);
            g2.rotate(Math.toRadians(rotationAngle));
            //DRAW THE IMAGE
            g2.drawImage(drawn, 0, 0, null);
        } else {
            CanvasWidget paper = ((LibraryScene) getScene()).getCanvasWidget();
            Point paperOnScene = paper.convertLocalToScene(new Point());
            Point paperOnThis = convertSceneToLocal(paperOnScene);
            Shape clip = g2.getClip();
            //draw inner, clip to inner only
            g2.clipRect(paperOnThis.x, paperOnThis.y, paper.getWidth(), paper.getHeight());
            g2.scale(xScale, yScale);
            Point2D.Double oriTL = new Point2D.Double(-0.5 * xScale * getImage().getWidth(null), -0.5 * yScale * getImage().getHeight(null));
            Point2D.Double rotatedTopLeft = GeomUtil.rotate_point(oriTL.x, oriTL.y, Math.toRadians(rotationAngle), 0, 0);
            g2.translate(rotatedTopLeft.x, rotatedTopLeft.y);
            g2.rotate(Math.toRadians(rotationAngle));
            g2.drawImage(drawn, 0, 0, null);
            g2.setTransform(prev);
            g2.setClip(clip);
           //done inner

            //draw masked outer
            Rectangle area = calculateClientArea();
            BufferedImage out = ImageUtil.createClearImage(area.width, area.height);
            Graphics2D gbo = out.createGraphics();
            gbo.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
            gbo.fill(new Rectangle2D.Double(paperOnThis.x + area.width / 2, paperOnThis.y + area.height / 2, paper.getWidth(), paper.getHeight()));
            gbo.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.25f));
            gbo.scale(xScale, yScale);
            gbo.translate(rotatedTopLeft.x + area.width / 2, rotatedTopLeft.y + area.height / 2);
            gbo.rotate(Math.toRadians(rotationAngle));
            gbo.drawImage(drawn, 0, 0, null);
            gbo.dispose();
            g2.translate(-area.width / 2, -area.height / 2);
            g2.drawImage(out, 0, 0, null);
            //done outer
        }
        //RESTORE TRANSFORMATION
        g2.setTransform(prev);
    }

    public BufferedImage getImage() {
        if (tempImage == null) {
            return image;
        } else {
            return tempImage;
        }
    }

    public BufferedImage getOriginalImage() {
        return image;
    }

    public void setTempImage(BufferedImage bufferedImage) {
        this.tempImage = bufferedImage;
        repaint();
    }

    public void setImage(BufferedImage temp) {
        image.setData(temp.getData());
        repaint();
    }
}
