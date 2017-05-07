/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.project.converter;

import com.jbatik.core.project.JBatikProjectFactory;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.io.CorakSerializer;
import com.jbatik.modules.corak.node.CorakFileUtil;
import com.jbatik.modules.layout.LayoutDocument;
import com.jbatik.modules.layout.LayoutLSystem;
import com.jbatik.modules.layout.io.LayoutParser;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.node.LayoutFileUtil;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class Converter implements Runnable {

    SyncCounter counter;
    FileObject oldProject;
    FileObject newProjectDir;
    FileObject destination;

    Converter(FileObject fo, FileObject dest, SyncCounter counter) {
        this.oldProject = fo;
        this.destination = dest;
        this.counter = counter;
    }

    @Override
    public void run() {
        try {
            doConvert();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        counter.increment();
    }

    private void doConvert() throws IOException {
        //cek apakah ada folder "corak3d" di dalam oldProject
        FileObject corak3dFolder = oldProject.getFileObject("corak3D");
        if (corak3dFolder != null && corak3dFolder.isFolder()) {
            createProjectDirOnDestination();
            //copy textures since it will be used by all created cors
            FileObject texturesFolder = oldProject.getFileObject("textures");
            copyTextures(texturesFolder);
            convertCors(corak3dFolder);
        }

        FileObject layout2dFolder = oldProject.getFileObject("layout2D");
        if (layout2dFolder != null && layout2dFolder.isFolder()) {
            createProjectDirOnDestination();
            convertLays(layout2dFolder);
        }
        FileObject libsFolder = oldProject.getFileObject("lib");
        if (libsFolder != null && libsFolder.isFolder()) {
            createProjectDirOnDestination();
            copyLibs(libsFolder);
        }
    }
    private static final String ITERATION_ELMT = "iter";
    private static final String ANGLE_ELMT = "angle";
    private static final String LENGTH_ELMT = "length";
    private static final String WIDTH_ELMT = "thickness";
    private static final String RAW_AXIOM_ELMT = "axiom";
    private static final String RAW_RULES_ELMT = "detail";
    private static final String BG_COLOR_ELMT = "bgColor";
    private static final String RED_COLOR_ELMT = "red";
    private static final String GREEN_COLOR_ELMT = "green";
    private static final String BLUE_COLOR_ELMT = "blue";
    private static final String ALPHA_COLOR_ELMT = "alpha";

    private void convertCors(FileObject corak3dFolder) throws IOException {
        FileObject newCorDir = CorakFileUtil.getCoraksFolder(FileOwnerQuery.getOwner(newProjectDir), true);
        //masuk, search for old .cor files 
        for (FileObject maybecor : corak3dFolder.getChildren()) {
            //process only those end with .cor extension
            if (!maybecor.isFolder() && maybecor.getExt().equalsIgnoreCase("cor")) {
                //parse old .cor
                try {
                    Document doc = XMLUtil.parse(
                            new InputSource(maybecor.getInputStream()),
                            false,
                            false,
                            null,
                            null);

                    Element corak = doc.getDocumentElement();
                    Element iteration = XMLUtil.findElement(corak, ITERATION_ELMT, null);
                    int i = Integer.parseInt(XMLUtil.findText(iteration));
                    Element angle = XMLUtil.findElement(corak, ANGLE_ELMT, null);
                    int a = Integer.parseInt(XMLUtil.findText(angle));
                    Element width = XMLUtil.findElement(corak, WIDTH_ELMT, null);
                    int w = Integer.parseInt(XMLUtil.findText(width));
                    Element length = XMLUtil.findElement(corak, LENGTH_ELMT, null);
                    int l = Integer.parseInt(XMLUtil.findText(length));

                    Element rawaxiom = XMLUtil.findElement(corak, RAW_AXIOM_ELMT, null);
                    String rawax = XMLUtil.findText(rawaxiom);
                    Element rawrules = XMLUtil.findElement(corak, RAW_RULES_ELMT, null);
                    String rawrls = XMLUtil.findText(rawrules);

                    Element bgcol = XMLUtil.findElement(corak, BG_COLOR_ELMT, null);
                    Color bg = Color.BLACK;
                    if (bgcol != null) {
                        Element r = XMLUtil.findElement(bgcol, RED_COLOR_ELMT, null);
                        Element b = XMLUtil.findElement(bgcol, BLUE_COLOR_ELMT, null);
                        Element g = XMLUtil.findElement(bgcol, GREEN_COLOR_ELMT, null);
                        Element al = XMLUtil.findElement(bgcol, ALPHA_COLOR_ELMT, null);
                        bg = new Color(
                                Integer.valueOf(XMLUtil.findText(r)),
                                Integer.valueOf(XMLUtil.findText(g)),
                                Integer.valueOf(XMLUtil.findText(b)),
                                Integer.valueOf(XMLUtil.findText(al))
                        );
                    }

                    //create object and serialize on new location
                    CorakLSystem cls = new CorakLSystem(
                            rawax == null ? "" : rawax,
                            rawrls == null ? "" : rawrls,
                            i,
                            a,
                            l,
                            w,
                            null, null,//use default colors & textures
                            new ArrayList<>());
                    cls.setBackground(new Color3f(bg));
                    FileObject newCorFile = newCorDir.createData(maybecor.getNameExt());
                    CorakSerializer.serialize(cls, FileUtil.toFile(newCorFile));
                } catch (SAXException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                            "Syntax error in corak File: " + maybecor.getPath(),
                            "Notice",
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            }
        }
    }

    private void copyTextures(FileObject texturesFolder) throws IOException {
        FileObject newTexturesDir = CorakFileUtil.getTexturesFolder(FileOwnerQuery.getOwner(newProjectDir), true);
        if (texturesFolder != null && texturesFolder.isFolder()) {
            for (FileObject f : texturesFolder.getChildren()) {
                //copy
                f.copy(newTexturesDir, f.getName(), f.getExt());
            }
        }
    }
    private static final String MAPS_ELMT = "map";
    private static final String LAYERS_ELMT = "lay__list";
    private static final String LAYER_ELMT = "modeller.layout2D.LayoutLS";
    private static final String MOD_ELMT = "mod";
    private static final String IMAGEMAP_ELMT = "modeller.layout2D.ImageMap";
    private static final String IMAGEMAP_IDX_ELMT = "idx";
    private static final String IMAGEMAP_NAME_ELMT = "img__file__name";
    private static final String LAYER_NAME_ELMT = "lay__name";
    private static final String POS_ELMT = "tile__offset";
    private static final String X_ELMT = "x";
    private static final String Y_ELMT = "y";
    private static final String ROTATION_ELMT = "groupRotation";

    private void convertLays(FileObject oldLayFolder) throws IOException {
        FileObject newLaysDir = LayoutFileUtil.getLayoutsFolder(FileOwnerQuery.getOwner(newProjectDir), true);
        for (FileObject maybeLay : oldLayFolder.getChildren()) {
            if (!maybeLay.isFolder() && maybeLay.hasExt("lay")) {
                //parse old .cor
                try {
                    Document doc = XMLUtil.parse(
                            new InputSource(maybeLay.getInputStream()),
                            false,
                            false,
                            null,
                            null);

                    Element dataModel2D = doc.getDocumentElement();
                    //parse image map first because the map will be used on all sublayoutlayer (old style!)
                    Element maps = XMLUtil.findElement(dataModel2D, MAPS_ELMT, null);
                    NodeList mapList = maps.getElementsByTagName(IMAGEMAP_ELMT);
                    Map<Integer, String> imageMap = new HashMap<>();
                    for (int m = 0; m < mapList.getLength(); m++) {
                        Element mapElmt = (Element) mapList.item(m);
                        Element idx = XMLUtil.findElement(mapElmt, IMAGEMAP_IDX_ELMT, null);
                        Element name = XMLUtil.findElement(mapElmt, IMAGEMAP_NAME_ELMT, null);
                        imageMap.put(1+Integer.valueOf(XMLUtil.findText(idx)), XMLUtil.findText(name));
                    }
                    //parse all sublayoutlayer
                    List<LayoutLayer> ls = new ArrayList<>();
                    Element mod = XMLUtil.findElement(dataModel2D, MOD_ELMT, null);
                    Element lays = XMLUtil.findElement(mod, LAYERS_ELMT, null);
                    NodeList layerList = lays.getElementsByTagName(LAYER_ELMT);
                    for (int li = 0; li < layerList.getLength(); li++) {
                        Element layout = (Element) layerList.item(li);
                        //create a new sublayoutlayer
                        Element iteration = XMLUtil.findElement(layout, ITERATION_ELMT, null);
                        int i = Integer.parseInt(XMLUtil.findText(iteration));
                        Element angle = XMLUtil.findElement(layout, ANGLE_ELMT, null);
                        int a = Integer.parseInt(XMLUtil.findText(angle));
                        Element width = XMLUtil.findElement(layout, WIDTH_ELMT, null);
                        int w = Integer.parseInt(XMLUtil.findText(width));
                        Element length = XMLUtil.findElement(layout, LENGTH_ELMT, null);
                        int l = Integer.parseInt(XMLUtil.findText(length));

                        Element rawaxiom = XMLUtil.findElement(layout, RAW_AXIOM_ELMT, null);
                        String rawax = XMLUtil.findText(rawaxiom);
                        Element rawrules = XMLUtil.findElement(layout, RAW_RULES_ELMT, null);
                        String rawrls = XMLUtil.findText(rawrules);

                        SubLayout sl = new SubLayout(rawax, rawrls, i, a, l, w);
                        sl.setImageColorIndex(imageMap);

                        Element offset = XMLUtil.findElement(layout, POS_ELMT, null);
                        Element xpos = XMLUtil.findElement(offset, X_ELMT, null);
                        int x = (int) Float.parseFloat(XMLUtil.findText(xpos));
                        sl.setX(x);
                        Element ypos = XMLUtil.findElement(offset, Y_ELMT, null);
                        int y = (int) Float.parseFloat(XMLUtil.findText(ypos));
                        sl.setY(y);
                        Element rotElmt = XMLUtil.findElement(layout, ROTATION_ELMT, null);
                        if (rotElmt != null) {
                            float rot = Float.parseFloat(XMLUtil.findText(rotElmt));//[-pi/2,pi/2]
                            sl.setSquareRotationAngle((float) Math.toDegrees(rot));
                        }
                        Element name = XMLUtil.findElement(layout, LAYER_NAME_ELMT, null);
                        String layerName = XMLUtil.findText(name);
                        SubLayoutLayer sll = new SubLayoutLayer(sl,null);
                        sll.setName(layerName);
                        ls.add(sll);
                    }
                    //create paper, old lay file has no paper information
                    //so by default we give em A4 paper (210x297 mm) on 72 dpi
                    Element bgcol = XMLUtil.findElement(dataModel2D, BG_COLOR_ELMT, null);
                    Color bg = Color.WHITE;
                    if (bgcol != null) {
                        Element r = XMLUtil.findElement(bgcol, RED_COLOR_ELMT, null);
                        Element b = XMLUtil.findElement(bgcol, BLUE_COLOR_ELMT, null);
                        Element g = XMLUtil.findElement(bgcol, GREEN_COLOR_ELMT, null);
                        Element al = XMLUtil.findElement(bgcol, ALPHA_COLOR_ELMT, null);
                        bg = new Color(
                                Integer.valueOf(XMLUtil.findText(r)),
                                Integer.valueOf(XMLUtil.findText(g)),
                                Integer.valueOf(XMLUtil.findText(b)),
                                Integer.valueOf(XMLUtil.findText(al))
                        );
                    }
                    LayoutDocument ld = new LayoutDocument(210, 297, 72, "mm", bg);

                    //create object and serialize on new location
                    LayoutLSystem lls = new LayoutLSystem(ld, ls);
                    FileObject newLayFile = newLaysDir.createData(maybeLay.getNameExt());
                    LayoutParser.save(lls, FileUtil.toFile(newLayFile));
                } catch (SAXException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                            "Syntax error in corak File: " + maybeLay.getPath(),
                            "Notice",
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            }
        }
    }

    private void copyLibs(FileObject oldLibsFolder) throws IOException {
        FileObject newLibsDir = LayoutFileUtil.getLibrariesFolder(FileOwnerQuery.getOwner(newProjectDir), true);
        for (FileObject f : oldLibsFolder.getChildren()) {
            //copy
            f.copy(newLibsDir, f.getName(), f.getExt());
        }
    }

    private void createProjectDirOnDestination() throws IOException {
        if (newProjectDir == null) {
            String validName = FileUtil.findFreeFolderName(destination, oldProject.getNameExt());
            newProjectDir = destination.createFolder(validName);
            //create PROJECT_DIR in it so FileOwnerQuery works
            newProjectDir.createFolder(JBatikProjectFactory.PROJECT_DIR);
        }
    }

}
