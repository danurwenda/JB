/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Muhammad Ghifary
 */
public class ImageUtil {

    public static void changeColorOnImage(BufferedImage image, Color target, Color replacement, int toleranceVal) {
//        System.err.println("masup");
        int rr = target.getRed();
        int gr = target.getGreen();
        int br = target.getBlue();
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                int c = image.getRGB(i, j);
                int a = (c >> 24) & 0xFF;
                if (a > 0) {
//                    System.err.println("masup a >0");
                    int r = (c >> 16) & 0xFF;
                    int g = (c >> 8) & 0xFF;
                    int b = (c) & 0xFF;
                    if ((Math.abs(r - rr) <= toleranceVal)
                            && (Math.abs(g - gr) <= toleranceVal)
                            && (Math.abs(b - br) <= toleranceVal)) {
                        //a suitable target
//                        System.err.println("setRGB");
                        image.setRGB(i, j, replacement.getRGB());
                    }
                }
            }
        }
    }

    public static Image getScaledImage(Image srcImg, int w, int h) {
        return getScaledImage(srcImg, w, h, false);
    }

    /**
     * TODO : consider Image.getScaledInstance();
     *
     * @param srcImg
     * @param w
     * @param h
     * @param maintainRatio
     * @return
     */
    public static Image getScaledImage(Image srcImg, int w, int h, boolean maintainRatio) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int x, y, wres, hres;
        int oriw = srcImg.getWidth(null);
        int orih = srcImg.getHeight(null);
        double oriRat = (oriw * 1.0) / (orih * 1.0);
        double resRat = (w * 1.0) / (h * 1.0);
        if (!maintainRatio || oriRat == resRat) {
            x = 0;
            y = 0;
            wres = w;
            hres = h;
        } else if (resRat > oriRat) {
            y = 0;
            hres = h;
            wres = oriw * h / orih;
            x = (w - wres) / 2;
        } else {
            x = 0;
            wres = w;
            hres = orih * w / oriw;
            y = (h - hres) / 2;
        }
        g2.drawImage(srcImg, x, y, wres, hres, null);
        g2.dispose();
        return resizedImg;
    }

    /**
     * Bikin image ukuran s x s dari image ori. Jika ori dimensi awalnya bukan
     * square, maka empty space akan diisi dengan transparent pixel biar jadi s
     * x s.
     *
     * @param ori Original image to square
     * @param s Side length of resulted image
     * @return Image of original image, fitted into s x s
     * @throws java.io.IOException
     */
    public static Image getSquaredImage(InputStream ori, int s) throws IOException {
        return getScaledImage(ori, s, s, true);
    }

    public static Image getSquaredImage(Image ori, int s) {
        return getScaledImage(ori, s, s, true);
    }

    /**
     * Creates image from given InputStream and dimension. If the boolean
     * parameter is set to false, then resulted image will be the original image
     * that is packed into w x h frame, sheared if necessary.
     *
     * @param s InputStream of an image file
     * @param w resulting width
     * @param h resulting height
     * @param maintainRatio true if we must preserve width/height ratio of
     * original image
     * @return a scaled image
     * @throws IOException
     */
    public static Image getScaledImage(InputStream s, int w, int h, boolean maintainRatio) throws IOException {
        int unitLength = 2500;
        Image image = null;
        //daripada jebol pake yang sebelumnya, kita pakai InputStream nya saja trus baca region dibagi2
        try (ImageInputStream stream = ImageIO.createImageInputStream(s)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                int oriw = reader.getWidth(reader.getMinIndex());
                int orih = reader.getHeight(reader.getMinIndex());
                if (oriw < unitLength && orih < unitLength) {
                    //ga bakal jebol lah ini, pake yang biasa aja
                    image = getScaledImage(reader.read(0), w, h, maintainRatio);
                } else {
                    //di sini, kita akan scaling oriw*orih menjadi w*h
                    //padahal scale nya dilakukan per unitLength*unitLength
                    //sementara ini kita tiling unitLengthxunitLength aja
                    int row = orih / unitLength;
                    int rowplus = orih - unitLength * row;
                    if (rowplus > 0) {
                        row++;
                    }
                    int col = oriw / unitLength;
                    int colplus = oriw - unitLength * col;
                    if (colplus > 0) {
                        col++;
                    }
                    //siapkan array of Image sebanyak row*col
                    int imgNum = row * col;
                    Image[] images = new Image[imgNum];
                    int imgIndex = 0;
                    ImageReadParam param = reader.getDefaultReadParam();
                    int wregion, hregion;

                    int xScaleNum, xScaleDenum, yScaleNum, yScaleDenum;
                    //bandingin ratio original image sama created image
                    double oriRat = (oriw * 1.0) / orih;
                    double resRat = (w * 1.0) / h;
                    boolean transNeeded = true;
                    if (!maintainRatio || oriRat == resRat) {
                        xScaleNum = w;
                        xScaleDenum = oriw;
                        yScaleNum = h;
                        yScaleDenum = orih;
                        transNeeded = false;
                    } else //jadi misalnya image asli 4:3 mau discale jadi 16:9. Karena 4/3<16/9, berarti bakal ada empty pixels area di kanan dan kiri
                    if (resRat > oriRat) {
                        //transparent pixels on right/left, orih->h
                        yScaleNum = h;
                        yScaleDenum = orih;
                        xScaleNum = yScaleNum;
                        xScaleDenum = yScaleDenum;
                    } else {
                        //transparent pixels on top/bottom, oriw->w
                        xScaleNum = w;
                        xScaleDenum = oriw;
                        yScaleNum = xScaleNum;
                        yScaleDenum = xScaleDenum;
                    }

                    for (int r = 1; r <= row; r++) {
                        for (int c = 1; c <= col; c++) {
                            //tentukan area
                            hregion = unitLength;
                            wregion = unitLength;
                            if (r == row && rowplus > 0) {
                                hregion = rowplus;
                            }
                            if (c == col && colplus > 0) {
                                wregion = colplus;
                            }

                            param.setSourceRegion(new Rectangle(unitLength * (c - 1), unitLength * (r - 1), wregion, hregion)); // Set region
                            int wres = wregion * xScaleNum / xScaleDenum;
                            int hres = hregion * yScaleNum / yScaleDenum;
                            images[imgIndex] = getScaledImage(reader.read(0, param), wres>0?wres:1, hres>0?hres:1);
                            imgIndex++;
                        }
                    }
                    //create merge image from tiles
                    image = mergeImage(images, row, col, unitLength * xScaleNum / xScaleDenum, unitLength * yScaleNum / yScaleDenum);
                    //if perlu transparent pixels
                    if (transNeeded) {
                        //yang pasti kita bikin gambar ukuran w*h
                        Image transparent = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        //sekarang tentukan xoffset,yoffsetnya
                        //kalau perlu transparent di kanan kiri, berarti kan yoffset=0, xoffset=sesuatu
                        int xoffset, yoffset;
                        if (resRat > oriRat) {
                            //filler kanan kiri
                            yoffset = 0;
                            xoffset = (w - oriw * xScaleNum / xScaleDenum) / 2;
                        } else {
                            //filler atas bawah
                            xoffset = 0;
                            yoffset = (h - orih * yScaleNum / yScaleDenum) / 2;
                        }
                        image = ImageUtilities.mergeImages(transparent, image, xoffset, yoffset);
                    }
                }

            } else {
                System.err.println("masup sini kah");
            }
        }
        return image;
    }

    public static Image mergeImage(Image[] images, int row, int col, int xperiod, int yperiod) {
        if (images.length != row * col) {
            return null;
        } else {
            Image merged = null;
            int imageIndex = 0;
            for (int r = 0; r < row; r++) {
                for (int c = 0; c < col; c++) {
                    if (merged == null && r == 0 && c == 0) {
                        merged = images[imageIndex];
                    } else {
                        merged = ImageUtilities.mergeImages(merged, images[imageIndex], xperiod * c, yperiod * r);
                    }
                    imageIndex++;
                }
            }
            return merged;
        }
    }

    public static void copy(String fromFileName, String toFileName)
            throws IOException {
        File fromFile = new File(fromFileName);
        File toFile = new File(toFileName);

        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFileName);
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFileName);
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFileName);
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFileName);
            }
            System.out.print("Overwrite existing file " + toFile.getName()
                    + "? (Y/N): ");
            System.out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            String response = in.readLine();
            if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException("FileCopy: "
                        + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        try (
                FileInputStream from = new FileInputStream(fromFile);
                FileOutputStream to = new FileOutputStream(toFile);) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        }
    }

    public static BufferedImage createClearImage(int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = dest.createGraphics();
        //fill all with transparent pixels
        Composite ori = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);
        g2.setComposite(ori);
        return dest;
    }

    /**
     * TODO : Consider using ImageFilter or ColorConvertOp or any better algo
     *
     * @param source
     * @param hue
     * @param s
     * @param v
     * @return
     */
    public static BufferedImage shiftHSV(BufferedImage source, int hue, int s, int v) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage dest = createClearImage(width, height);
//        Raster sourceRaster =source.getData();
//        sourceRaster.
        float hShift = ((float) hue) / 360.0f;//returns -0.5 to 0.5
        float sShift = ((float) s) / 100.0f;//returns -1 to 1
        float vShift = ((float) v) / 100.0f;//returns -1 to 1
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                int srcRGB = source.getRGB(w, h);
                if (((srcRGB >> 24) & 0xFF) > 0) {
                    //only apply the transformation on non fully-transparent pixels

                    Color oriColor = new Color(srcRGB, true);
                    float[] hsbvals = Color.RGBtoHSB(oriColor.getRed(), oriColor.getGreen(), oriColor.getBlue(), null);
                    float hu = hsbvals[0];
                    float sa = hsbvals[1];
                    float va = hsbvals[2];
                    float hShifted = hu + hShift;
                    float sShifted = s > 0 ? sa + (sShift * (1.0f - sa)) : sa * (1.0f + sShift);
                    float vShifted = v > 0 ? va + (vShift * (1.0f - va)) : va * (1.0f + vShift);

                    dest.setRGB(w, h, Color.HSBtoRGB(hShifted, sShifted, vShifted));
                }
            }
        }
        return dest;
    }

    public static void shiftHSV(BufferedImage source) {
    }
}
