/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.io;

import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.modules.corak.CorakLSystem;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A utility class to create a CorakLSystem from a given CorakDataObject
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class CorakSerializer {

    private static final String CORAK_ROOT = "corak";
    private static final String ITERATION_ELMT = "iteration";
    private static final String ANGLE_ELMT = "angle";
    private static final String LENGTH_ELMT = "length";
    private static final String WIDTH_ELMT = "width";
    private static final String RAW_AXIOM_ELMT = "rawaxiom";
    private static final String RAW_RULES_ELMT = "rawrules";
    private static final String COLORS_ELMT = "colors";
    private static final String COLOR_ELMT = "color";
    private static final String BG_COLOR_ELMT = "bgcolor";
    private static final String RED_COLOR_ATTR = "r";
    private static final String GREEN_COLOR_ATTR = "g";
    private static final String BLUE_COLOR_ATTR = "b";
    private static final String TEXTURES_ELMT = "textures";
    private static final String TEXTURE_ELMT = "texture";
    private static final String FILE_TEXTURE_ATTR = "file";
    private static final String SURFACES_ELMT = "surfaces";
    private static final String SURFACE_ELMT = "surface";
    private static final String SURFACE_NAME_ATTR = "name";
    private static final String SURFACE_POINTS_ELMT = "points";
    private static final String SURFACE_POINT_ELMT = "point";
    private static final String SURFACE_POINT_X_ATTR = "x";
    private static final String SURFACE_POINT_Y_ATTR = "y";
    private static final String SURFACE_POINT_Z_ATTR = "z";
    //pre-invalidable syntax

    private static final String AXIOM_ELMT = "axiom";
    private static final String RULES_ELMT = "rules";
    private static final String RULE_ELMT = "rule";
    private static final String PREDECESSOR_ELMT = "pred";
    private static final String SUCCESSOR_ELMT = "succ";

    public static CorakLSystem deserialize(CorakDataObject obj) {
        return deserialize(obj.getPrimaryFile().toURL().toString());
    }

    public static CorakLSystem deserialize(String corFullPath) {
        try {
            Document doc = XMLUtil.parse(
                    new InputSource(corFullPath),
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
            CorakLSystem cls = null;
            //parse textures
            Element textures = XMLUtil.findElement(corak, TEXTURES_ELMT, null);
            NodeList textureList = textures.getElementsByTagName(TEXTURE_ELMT);
            ArrayList<String> texFileNameList = new ArrayList<>();
            for (int t = 0; t < textureList.getLength(); t++) {
                Element texElement = (Element) textureList.item(t);
                texFileNameList.add(texElement.getAttribute(FILE_TEXTURE_ATTR));
            }

            //parse surfaces
            Element surfaces = XMLUtil.findElement(corak, SURFACES_ELMT, null);
            NodeList surfaceList = surfaces.getElementsByTagName(SURFACE_ELMT);
            ArrayList<Surface> sList = new ArrayList<>();
            for (int t = 0; t < surfaceList.getLength(); t++) {
                Element surfElement = (Element) surfaceList.item(t);
                sList.add(getSurface(surfElement));
            }
            //cek dulu apakah ini versi baru apa lama
            if (XMLUtil.findElement(corak, RAW_AXIOM_ELMT, null) == null) {
                //syntax lama
                Element axiom = XMLUtil.findElement(corak, AXIOM_ELMT, null);
                String ax = XMLUtil.findText(axiom);
                Element rules = XMLUtil.findElement(corak, RULES_ELMT, null);
                NodeList ruleList = rules.getElementsByTagName(RULE_ELMT);
                HashMap<Character, String> m = new HashMap<>(0);
                for (int c = 0; c < ruleList.getLength(); c++) {
                    Element rule = (Element) ruleList.item(c);
                    Element succ = XMLUtil.findElement(rule, SUCCESSOR_ELMT, null);
                    Element pred = XMLUtil.findElement(rule, PREDECESSOR_ELMT, null);
                    String p = XMLUtil.findText(pred);
                    if (p.length() != 1) {
                        //throw Exception?
                    } else {
                        m.put(Character.valueOf(p.charAt(0)), XMLUtil.findText(succ));
                    }
                }
                cls = new CorakLSystem(ax == null ? "" : ax, m, i, a, l, w, getColorList(corak), texFileNameList, sList);
            } else {
                Element rawaxiom = XMLUtil.findElement(corak, RAW_AXIOM_ELMT, null);
                String rawax = XMLUtil.findText(rawaxiom);
                Element rawrules = XMLUtil.findElement(corak, RAW_RULES_ELMT, null);
                String rawrls = XMLUtil.findText(rawrules);
                cls = new CorakLSystem(rawax == null ? "" : rawax, rawrls == null ? "" : rawrls, i, a, l, w, getColorList(corak), texFileNameList, sList);
            }
            //background color
            Element bgelmt = XMLUtil.findElement(corak, BG_COLOR_ELMT, null);
            if (bgelmt != null) {
                Color c = new Color(
                        Integer.valueOf(bgelmt.getAttribute(RED_COLOR_ATTR)),
                        Integer.valueOf(bgelmt.getAttribute(GREEN_COLOR_ATTR)),
                        Integer.valueOf(bgelmt.getAttribute(BLUE_COLOR_ATTR)));
                cls.setBackground(new Color3f(c));
            }
            return cls;
        } catch (IOException iex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    "File can't be found: " + corFullPath,
                    "Notice",
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE));
        } catch (SAXException sex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    "Syntax error in corak File: " + corFullPath,
                    "Notice",
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    }

    private static Surface getSurface(Element e) {
        //ambil nama
        Surface s = new Surface();
        s.setName(e.getAttribute(SURFACE_NAME_ATTR));
        Element points = XMLUtil.findElement(e, SURFACE_POINTS_ELMT, null);
        NodeList pointList = points.getElementsByTagName(SURFACE_POINT_ELMT);
        ArrayList<Point3d> ps = new ArrayList<>();
        for (int c = 0; c < pointList.getLength(); c++) {
            Element point = (Element) pointList.item(c);
            ps.add(new Point3d(
                    Double.valueOf(point.getAttribute(SURFACE_POINT_X_ATTR)),
                    Double.valueOf(point.getAttribute(SURFACE_POINT_Y_ATTR)),
                    Double.valueOf(point.getAttribute(SURFACE_POINT_Z_ATTR))
            ));
        }
        Point3d[] p3ds = new Point3d[ps.size()];
        p3ds = ps.toArray(p3ds);
        s.setPoints(p3ds);
        return s;
    }

    private static ArrayList<Color> getColorList(Element root) {
        //parse color
        Element colors = XMLUtil.findElement(root, COLORS_ELMT, null);
        NodeList colorList = colors.getElementsByTagName(COLOR_ELMT);
        ArrayList<Color> cl = new ArrayList<>();
        for (int c = 0; c < colorList.getLength(); c++) {
            Element color = (Element) colorList.item(c);
            cl.add(
                    new Color(
                            Integer.valueOf(color.getAttribute(RED_COLOR_ATTR)),
                            Integer.valueOf(color.getAttribute(GREEN_COLOR_ATTR)),
                            Integer.valueOf(color.getAttribute(BLUE_COLOR_ATTR)))
            );
        }
        return cl;
    }

    public static void serialize(CorakLSystem cls, File file) {
        Document document = XMLUtil.createDocument(CORAK_ROOT, null, null, null);

        Node corakElmt = document.getFirstChild();
        //iterasi
        Element iter = document.createElement(ITERATION_ELMT);
        iter.setTextContent(String.valueOf(cls.getIteration()));
        corakElmt.appendChild(iter);

        //angle
        Element angle = document.createElement(ANGLE_ELMT);
        angle.setTextContent(String.valueOf(cls.getAngle()));
        corakElmt.appendChild(angle);

        //length
        Element length = document.createElement(LENGTH_ELMT);
        length.setTextContent(String.valueOf(cls.getLength()));
        corakElmt.appendChild(length);

        //width
        Element width = document.createElement(WIDTH_ELMT);
        width.setTextContent(String.valueOf(cls.getWidth()));
        corakElmt.appendChild(width);

        //axiom
        Element axiom = document.createElement(RAW_AXIOM_ELMT);
        axiom.setTextContent(cls.getRawAxiom());
        corakElmt.appendChild(axiom);

        //rules
        Element rules = document.createElement(RAW_RULES_ELMT);
        rules.setTextContent(cls.getRawDetails());
        corakElmt.appendChild(rules);

        //bg
        Element bgelmt = document.createElement(BG_COLOR_ELMT);
        Color bg = cls.getBackground().get();
        bgelmt.setAttribute(RED_COLOR_ATTR, String.valueOf(bg.getRed()));
        bgelmt.setAttribute(GREEN_COLOR_ATTR, String.valueOf(bg.getGreen()));
        bgelmt.setAttribute(BLUE_COLOR_ATTR, String.valueOf(bg.getBlue()));
        corakElmt.appendChild(bgelmt);

        //colors
        Element colors = document.createElement(COLORS_ELMT);

        for (Color3f color3f : cls.getColor3fs()) {
            Element colorElmt = document.createElement(COLOR_ELMT);
            Color c = color3f.get();
            colorElmt.setAttribute(RED_COLOR_ATTR, String.valueOf(c.getRed()));
            colorElmt.setAttribute(GREEN_COLOR_ATTR, String.valueOf(c.getGreen()));
            colorElmt.setAttribute(BLUE_COLOR_ATTR, String.valueOf(c.getBlue()));
            colors.appendChild(colorElmt);
        }
        corakElmt.appendChild(colors);

        //textures
        Element textures = document.createElement(TEXTURES_ELMT);

        //list of texture
        for (String f : cls.getTextures()) {
            Element ruleElmt = document.createElement(TEXTURE_ELMT);
            ruleElmt.setAttribute(FILE_TEXTURE_ATTR, f);
            textures.appendChild(ruleElmt);
        }
        corakElmt.appendChild(textures);

        //surfaces
        Element surfaces = document.createElement(SURFACES_ELMT);
        //list of surface
        for (Surface s : cls.getSurfaces()) {
            Element points = document.createElement(SURFACE_POINTS_ELMT);
            for (Point3d p : s.getPoints()) {
                Element pointElement = document.createElement(SURFACE_POINT_ELMT);
                pointElement.setAttribute(SURFACE_POINT_X_ATTR, String.valueOf(p.x));
                pointElement.setAttribute(SURFACE_POINT_Y_ATTR, String.valueOf(p.y));
                pointElement.setAttribute(SURFACE_POINT_Z_ATTR, String.valueOf(p.z));
                points.appendChild(pointElement);
            }
            Element surfaceElement = document.createElement(SURFACE_ELMT);
            surfaceElement.setAttribute(SURFACE_NAME_ATTR, s.getName());
            surfaceElement.appendChild(points);
            surfaces.appendChild(surfaceElement);
        }
        corakElmt.appendChild(surfaces);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

}
