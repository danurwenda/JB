/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.visual.widget.Scene;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Creates some PNG files as tiling then use PNGJ to merge the images into a big
 * PNG image.
 *
 * @author Dimas Y. Danurwenda
 */
public class TilingScene2Image {

    private final static RequestProcessor RP = new RequestProcessor(TilingScene2Image.class.getName(), 4, true);
    private final File file;
    private final Scene scene;
    private static final int tileSize = 9000000;//each tile will contain this number of pixels, at most
    private int dpi = -1;

    public TilingScene2Image(File file, Scene scene) {
        this.file = file;
        this.scene = scene;
    }

    public void createImage(Rectangle p) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("write image to " + file.getName());
        final RequestProcessor.Task theTask = RP.create(() -> {
            try {
                createImageImpl(p, ph);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        theTask.addTaskListener((Task task) -> {
            //make sure that we get rid of the ProgressHandle
            //when the task is finished
            ph.finish();
        });

        //this actually start the task
        theTask.schedule(0);
    }

    public void createImageImpl(Rectangle paperRect, ProgressHandle ph) throws IOException {
        //validate the scene first
        if (scene == null) {
            return;
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

        //done validating
        //preparing temp dir
        File tempDir = new File(System.getProperty("java.io.tmpdir"));//NOI18N
        tempDir = FileUtil.normalizeFile(tempDir);
        FileObject tempDirO = FileUtil.toFileObject(tempDir);
        String availDirName = FileUtil.findFreeFolderName(tempDirO, file.getName().replaceAll(".".concat(FileUtil.getExtension(file.getName())), ""));
        FileObject tilesDir = tempDirO.createFolder(availDirName);
        //start creating tiles
        int width = paperRect.width;
        int height = paperRect.height;
        //width dan height ini adalah ukuran dari image akhir
        //terkadang ini terlalu besar untuk dirender langsung dan outofmemory error
        //jadi kita render beberapa tile, write setiap tile ke sebuah file png
        //lalu kita merge file pngs tersebut pakai library PNGJ

        //dalam loop, kita bikin proses2
        //di mana setiap proses akan merender sebuah tile
        //dan menuliskan tile tersebut ke sebuah file png
        //dan mengcopy ke sebuah PngWriter
        //perhatikan bahwa satu row di file tujuan pasti masuk ke dalam satu tile
        //namun satu tile bisa memuat banyak row
        int tileHeight = tileSize / width;//max height of each tile
        int nTilesY = 1 + (height - 1) / tileHeight;
        //now we know how much tiles we will have, we can start the progress handle
        ph.start(nTilesY * 2);
        int step = 0;
        //start the first loop, creating tiles
        //not using multithread since widgets in scene can't be drawn
        //by more than 1 graphic in the same time
        //we can work this around by creating multiple clone of scene
        //and send it to each thread
        //but that will consume a huge memory allocation
        File tileFiles[] = new File[nTilesY];
        for (int ty = 0; ty < nTilesY; ty++) {
            //prepare bufferedImage width x tileHeight
            int curTileHeight
                    = ty < nTilesY - 1 ?//not on the bottomest tile?
                            tileHeight : //full tile height
                            height - tileHeight * (nTilesY - 1);//remaining height
            BufferedImage bi = new BufferedImage(width, curTileHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bi.createGraphics();
            //these next two lines create a smooth result
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            //translate the graphic to suitable location based on current ty
            Point sceneLoc = scene.getLocation();
            g.translate(-paperRect.x - sceneLoc.x, -paperRect.y - sceneLoc.y - ty * tileHeight);
            //draw to g
            scene.paint(g);
            //write to tiling file (temporary)
            FileObject pngTemp;
            pngTemp = tilesDir.createData("tileY" + ty, "png");

            File fileTemp = FileUtil.toFile(pngTemp);
            try (FileImageOutputStream fo = new FileImageOutputStream(fileTemp)) {
                ImageIO.write(bi, "PNG", fo);
            }
            tileFiles[ty] = fileTemp;
            //done write to file
            //release resources
            g.dispose();
            bi.flush();
            ph.progress(++step);
        }
        //read all temp file
        //cant use multi thread since writeRow method has to be called
        //sequentially
        for (File f : tileFiles) {
            //read the file using pngreader
            PngReader pngr = new PngReader(f);
            //init destination
            getDestImageInfo(width, height, pngr);
            //copy row by row from source
            for (int rowOnTemp = 0; rowOnTemp < pngr.imgInfo.rows; rowOnTemp++) {
                ImageLineInt lineSource = (ImageLineInt) pngr.readRow(rowOnTemp);
                System.arraycopy(
                        lineSource.getScanline(), 0,
                        lineDest.getScanline(), 0,
                        lineSource.getScanline().length);
                pngw.writeRow(lineDest);
            }
            pngr.end();//close reader
            ph.progress(++step);
        }
        tilesDir.delete();//delete temporary folder that contains all tiling files
        pngw.end();//close writer
    }
    private ImageInfo destination;
    private PngWriter pngw;
    private ImageLineInt lineDest;

    private ImageInfo getDestImageInfo(int wDest, int hDest, PngReader pngr) {
        if (destination == null) {
            //init writer classes
            ImageInfo imi1 = pngr.imgInfo;
            destination = new ImageInfo(wDest, hDest, imi1.bitDepth, imi1.alpha, imi1.greyscale,
                    imi1.indexed);
            pngw = new PngWriter(file, destination, true);
            // copy palette and transparency if necessary (more chunks?)
            pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_PALETTE
                    | ChunkCopyBehaviour.COPY_TRANSPARENCY);
            if (dpi != -1) {
                pngw.getMetadata().setDpi(dpi);
            }
            lineDest = new ImageLineInt(destination);
        }
        return destination;
    }

    public void setDPI(int dpi) {
        this.dpi = dpi;
    }
}
