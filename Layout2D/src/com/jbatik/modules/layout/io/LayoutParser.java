/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.io;

/**
 *
 * @author RAPID02
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jbatik.filetype.lay.LayoutDataObject;
import com.jbatik.modules.layout.LayoutLSystem;
import com.jbatik.modules.layout.LayoutDocument;
import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A utility class to create a LayoutLSystem from a given LayoutDataObject
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class LayoutParser {

    private static final String LAYOUT_ROOT = "layout";
    //paper attributes
    private static final String PAPER_ELMT = "paper";
    private static final String LAYOUT_WIDTH_ELMT = "paper-width";
    private static final String LAYOUT_HEIGHT_ELMT = "paper-height";
    private static final String DPI_ELMT = "dpi";
    private static final String UNIT_ELMT = "unit";
    private static final String BACKGROUND_ELMT = "background";
    private static final String RED_COLOR_ATTR = "r";
    private static final String GREEN_COLOR_ATTR = "g";
    private static final String BLUE_COLOR_ATTR = "b";
    private static final String ALPHA_COLOR_ATTR = "a";
    //lsystem attributes
    private static final String ITERATION_ELMT = "iteration";
    private static final String ANGLE_ELMT = "angle";
    private static final String ANGLE_MULT_ELMT = "angle-mult";
    private static final String LENGTH_ELMT = "length";
    private static final String LENGTH_MULT_ELMT = "length-mult";
    private static final String WIDTH_ELMT = "width";
    private static final String WIDTH_MULT_ELMT = "width-mult";
    private static final String AXIOM_ELMT = "axiom";
    private static final String RULES_ELMT = "rules";
    private static final String RULE_ELMT = "rule";
    private static final String PREDECESSOR_ELMT = "pred";
    private static final String SUCCESSOR_ELMT = "succ";
    //sublayout attributes
    private static final String SUBLAYOUT_NAME = "name";
    private static final String X_LOCATION = "x";
    private static final String Y_LOCATION = "y";
    private static final String IMAGES_ELMT = "images";
    private static final String IMAGE_ELMT = "image";
    private static final String INDEX_IMAGE_ATTR = "index";
    private static final String FILE_IMAGE_ATTR = "file";
    private static final String LAYERS_ELMT = "layers";
    private static final String SQROT_ELMT = "sqrot";
    //layout layer and its attributes
    private static final String LAYER_ELMT = "layer";
    private static final String LAYER_TYPE = "type";
    private static final String LAYER_VISIBLE = "visible";
    private static final String LAYER_MODIFY = "modifiable";
    private static final String LAYER_LOCKED = "locked";
    private static final String LAYER_MAP = "mappable";

    private static void setAttribute(Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes();
        Attr attribute = xml.createAttribute(name);
        attribute.setValue(value);
        map.setNamedItem(attribute);
    }

    public static void save(LayoutLSystem ls, File file) {
        //preparing document
        Document document = XMLUtil.createDocument(LAYOUT_ROOT, null, null, null);
        Node layoutElement = document.getFirstChild();

        //preparing paper info
        Element paper = document.createElement(PAPER_ELMT);
        LayoutDocument lp = ls.getDocument();
        //background
        Element bg = document.createElement(BACKGROUND_ELMT);
        Color c = lp.getBackground();
        setAttribute(document, bg, RED_COLOR_ATTR, String.valueOf(c.getRed()));
        setAttribute(document, bg, GREEN_COLOR_ATTR, String.valueOf(c.getGreen()));
        setAttribute(document, bg, BLUE_COLOR_ATTR, String.valueOf(c.getBlue()));
        setAttribute(document, bg, ALPHA_COLOR_ATTR, String.valueOf(c.getAlpha()));
        paper.appendChild(bg);

        //paper dimension
        Element w = document.createElement(LAYOUT_WIDTH_ELMT);
        w.setTextContent(String.valueOf(lp.getWidth()));
        paper.appendChild(w);
        Element h = document.createElement(LAYOUT_HEIGHT_ELMT);
        h.setTextContent(String.valueOf(lp.getHeight()));
        paper.appendChild(h);
        Element dpi = document.createElement(DPI_ELMT);
        dpi.setTextContent(String.valueOf(lp.getDPI()));
        paper.appendChild(dpi);
        Element u = document.createElement(UNIT_ELMT);
        u.setTextContent(lp.getUnit().toString());
        paper.appendChild(u);

        layoutElement.appendChild(paper);
        //sublayouts
        Element sls = document.createElement(LAYERS_ELMT);
        for (LayoutLayer sl : ls.getLayers()) {
            Element slElement = createElementFromLayer(document, sl);

            sls.appendChild(slElement);
        }
        layoutElement.appendChild(sls);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            XMLUtil.write(document, fos, "UTF-8"); // NOI18N
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static LayoutLSystem parse(LayoutDataObject obj) {
        try {
            Document doc = XMLUtil.parse(
                    new InputSource(obj.getPrimaryFile().toURL().toString()),
                    false,
                    false,
                    null,
                    null);
            Element layout = doc.getDocumentElement();
            //paper
            Element paper = XMLUtil.findElement(layout, PAPER_ELMT, null);

            //background color
            Element background = XMLUtil.findElement(paper, BACKGROUND_ELMT, null);
            int r = Integer.valueOf(background.getAttribute(RED_COLOR_ATTR));
            int g = Integer.valueOf(background.getAttribute(GREEN_COLOR_ATTR));
            int b = Integer.valueOf(background.getAttribute(BLUE_COLOR_ATTR));
//            int a = Integer.valueOf(background.getAttribute(ALPHA_COLOR_ATTR));
            Color bg = new Color(r, g, b);

            //paper dimension
            Element width = XMLUtil.findElement(paper, LAYOUT_WIDTH_ELMT, null);
            double w = Double.parseDouble(XMLUtil.findText(width));
            Element height = XMLUtil.findElement(paper, LAYOUT_HEIGHT_ELMT, null);
            double h = Double.parseDouble(XMLUtil.findText(height));
            Element dpi = XMLUtil.findElement(paper, DPI_ELMT, null);
            int d = Integer.parseInt(XMLUtil.findText(dpi));
            Element unit = XMLUtil.findElement(paper, UNIT_ELMT, null);
            String u = XMLUtil.findText(unit);

            LayoutDocument p = new LayoutDocument(w, h, d, u, bg);

            //list of sublayout
            List<LayoutLayer> sls = new ArrayList<>();
            Element sublayouts = XMLUtil.findElement(layout, LAYERS_ELMT, null);

            Node childNode = sublayouts.getFirstChild();
            if (childNode != null) {
                while (childNode.getNextSibling() != null) {
                    childNode = childNode.getNextSibling();
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element layer = (Element) childNode;
                        LayoutLayer added = parseLayoutLayerElement(layer, null);
                        sls.add(added);
                    }
                }
            }

            return new LayoutLSystem(p, sls);
        } catch (IOException | SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static LayoutLayer parseLayoutLayerElement(Element layerElmt, LayoutLayer par1) {
        //check type first, layer or group
        String type = layerElmt.getAttribute(LAYER_TYPE);
        //semua layer pasti punya nama
        Element name = XMLUtil.findElement(layerElmt, SUBLAYOUT_NAME, null);
        String n = XMLUtil.findText(name);
        //atribut2
        String map = layerElmt.getAttribute(LAYER_MAP);
        boolean mappable = map.equals("") ? true : Boolean.parseBoolean(map);
        //migrating from modify to locked
        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
        String mod = layerElmt.getAttribute(LAYER_MODIFY);
        boolean moddable = mod.equals("") ? true : Boolean.parseBoolean(mod);
        String lock = layerElmt.getAttribute(LAYER_LOCKED);
        boolean locked = lock.equals("") ? false : Boolean.parseBoolean(lock);
        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
        String vis = layerElmt.getAttribute(LAYER_VISIBLE);
        boolean visible = vis.equals("") ? true : Boolean.parseBoolean(vis);
        if (type.equals("group")) //NOI18N
        {
            //populate anak, cari content dari layers
            Element children = XMLUtil.findElement(layerElmt, LAYERS_ELMT, null);

            Node childNode = children.getFirstChild();
            GroupLayer gl = new GroupLayer();
            List<LayoutLayer> c = new ArrayList<>();
            if (childNode != null) {
                //null means this group has no child
                while (childNode.getNextSibling() != null) {
                    childNode = childNode.getNextSibling();
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element layer = (Element) childNode;
                        LayoutLayer added = parseLayoutLayerElement(layer, null);
                        c.add(added);
                    }
                }
            }
            gl.setModel(c);
            //only have name and children
            gl.setName(n);
            gl.setVisible(visible);
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            if (!locked) {
                gl.setLocked(!moddable, false);
            } else {
                gl.setLocked(locked, false);
            }
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            gl.setMappable(mappable, false);

            return gl;
        } else if (type.equals("sublayout")) {
            Element iteration = XMLUtil.findElement(layerElmt, ITERATION_ELMT, null);
            int i = Integer.parseInt(XMLUtil.findText(iteration));
            Element angle = XMLUtil.findElement(layerElmt, ANGLE_ELMT, null);
            int a = Integer.parseInt(XMLUtil.findText(angle));
            Element angleMul = XMLUtil.findElement(layerElmt, ANGLE_MULT_ELMT, null);
            float aM = angleMul != null ? Float.parseFloat(XMLUtil.findText(angleMul)) : 1;
            Element sqrot = XMLUtil.findElement(layerElmt, SQROT_ELMT, null);
            float s = sqrot != null ? Float.parseFloat(XMLUtil.findText(sqrot)) : 0;
            Element width = XMLUtil.findElement(layerElmt, WIDTH_ELMT, null);
            int w = Integer.parseInt(XMLUtil.findText(width));
            Element widthMul = XMLUtil.findElement(layerElmt, WIDTH_MULT_ELMT, null);
            float wM = widthMul != null ? Float.parseFloat(XMLUtil.findText(widthMul)) : 1;
            Element length = XMLUtil.findElement(layerElmt, LENGTH_ELMT, null);
            int l = Integer.parseInt(XMLUtil.findText(length));
            Element lengthMul = XMLUtil.findElement(layerElmt, LENGTH_MULT_ELMT, null);
            float lM = lengthMul != null ? Float.parseFloat(XMLUtil.findText(lengthMul)) : 1;
            Element axiom = XMLUtil.findElement(layerElmt, AXIOM_ELMT, null);
            String ax = XMLUtil.findText(axiom);
            Element rules = XMLUtil.findElement(layerElmt, RULES_ELMT, null);
            NodeList ruleList = rules.getElementsByTagName(RULE_ELMT);
            HashMap m = new HashMap(0);
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
            //colorindex-image mapping
            HashMap imgs = new HashMap(0);
            Element images = XMLUtil.findElement(layerElmt, IMAGES_ELMT, null);
            NodeList imageList = images.getElementsByTagName(IMAGE_ELMT);
            for (int c = 0; c < imageList.getLength(); c++) {
                Element image = (Element) imageList.item(c);
                int colorIndex = Integer.valueOf(image.getAttribute(INDEX_IMAGE_ATTR));
                String file = image.getAttribute(FILE_IMAGE_ATTR);
                imgs.put(colorIndex, file);//will replace if it's already set
            }
            SubLayout sl = new SubLayout(ax == null ? "" : ax, m, i, a, l, w, s, aM, lM, wM);
            sl.setImageColorIndex(imgs);

            Element x_pos = XMLUtil.findElement(layerElmt, X_LOCATION, null);
            int x = Integer.parseInt(XMLUtil.findText(x_pos));
            sl.setX(x);

            Element y_pos = XMLUtil.findElement(layerElmt, Y_LOCATION, null);
            int y = Integer.parseInt(XMLUtil.findText(y_pos));
            sl.setY(y);

            SubLayoutLayer sll = new SubLayoutLayer(sl, (GroupLayer) par1);
            sll.setName(n);
            sll.setVisible(visible);
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            if (!locked) {
                sll.setLocked(!moddable);
            } else {
                sll.setLocked(locked);
            }
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            sll.setMappable(mappable);

            return sll;
        } else {
            return null;
        }
    }

//    public static void
    private static Element createElementFromLayer(Document document, LayoutLayer layer) {//for each sublayout
        Element slElement = document.createElement(LAYER_ELMT);
        //name
        Element name = document.createElement(SUBLAYOUT_NAME);
        name.setTextContent(layer.getName());
        slElement.appendChild(name);
        setAttribute(document, slElement, LAYER_VISIBLE, String.valueOf(layer.isVisible()));
        setAttribute(document, slElement, LAYER_LOCKED, String.valueOf(layer.isLocked()));
        setAttribute(document, slElement, LAYER_MAP, String.valueOf(layer.isMappable()));

        if (layer instanceof GroupLayer) {
            GroupLayer groupLayer = (GroupLayer) layer;
            //type
            setAttribute(document, slElement, LAYER_TYPE, "group");

            //anak2nya
            Element layers = document.createElement(LAYERS_ELMT);
            for (LayoutLayer ll : groupLayer.getModel().list()) {
                layers.appendChild(createElementFromLayer(document, ll));
            }
            slElement.appendChild(layers);
        } else if (layer instanceof SubLayoutLayer) {
            SubLayoutLayer subLayoutLayer = (SubLayoutLayer) layer;
            //type
            setAttribute(document, slElement, LAYER_TYPE, "sublayout");

            SubLayout sublayout = subLayoutLayer.getSublayout();

            //position
            Element xElmt = document.createElement(X_LOCATION);
            xElmt.setTextContent(String.valueOf(sublayout.getX()));
            slElement.appendChild(xElmt);
            Element yElmt = document.createElement(Y_LOCATION);
            yElmt.setTextContent(String.valueOf(sublayout.getY()));
            slElement.appendChild(yElmt);

            //iteration
            Element iter = document.createElement(ITERATION_ELMT);
            iter.setTextContent(String.valueOf(sublayout.getIteration()));
            slElement.appendChild(iter);
            //angle
            Element angle = document.createElement(ANGLE_ELMT);
            angle.setTextContent(String.valueOf(sublayout.getAngle()));
            slElement.appendChild(angle);
            //angleMult
            Element angleM = document.createElement(ANGLE_MULT_ELMT);
            angleM.setTextContent(String.valueOf(sublayout.getAngleMultiplier()));
            slElement.appendChild(angleM);
            //sqrot
            Element sqrot = document.createElement(SQROT_ELMT);
            sqrot.setTextContent(String.valueOf(sublayout.getSquareRotationAngle()));
            slElement.appendChild(sqrot);
            //width
            Element width = document.createElement(WIDTH_ELMT);
            width.setTextContent(String.valueOf(sublayout.getWidth()));
            slElement.appendChild(width);
            //widthMult
            Element widthM = document.createElement(WIDTH_MULT_ELMT);
            widthM.setTextContent(String.valueOf(sublayout.getWidthMultiplier()));
            slElement.appendChild(widthM);
            //length
            Element length = document.createElement(LENGTH_ELMT);
            length.setTextContent(String.valueOf(sublayout.getLength()));
            slElement.appendChild(length);
            //lengthM
            Element lengthM = document.createElement(LENGTH_MULT_ELMT);
            lengthM.setTextContent(String.valueOf(sublayout.getLengthMultiplier()));
            slElement.appendChild(lengthM);
            //axiom
            Element axiom = document.createElement(AXIOM_ELMT);
            axiom.setTextContent(sublayout.getAxiom());
            slElement.appendChild(axiom);
            //rules
            Element rules = document.createElement(RULES_ELMT);
            for (Map.Entry<Character, String> cursor : sublayout.getRules().entrySet()) {
                //for each rule
                Element rule = document.createElement(RULE_ELMT);
                Element pred = document.createElement(PREDECESSOR_ELMT);
                pred.setTextContent(String.valueOf(cursor.getKey()));
                Element succ = document.createElement(SUCCESSOR_ELMT);
                succ.setTextContent(cursor.getValue());
                rule.appendChild(pred);
                rule.appendChild(succ);
                rules.appendChild(rule);
            }
            slElement.appendChild(rules);
            //image lib mapping
            Element images = document.createElement(IMAGES_ELMT);
            for (Map.Entry<Integer, String> img : sublayout.getImageColorIndex().entrySet()) {
                Element image = document.createElement(IMAGE_ELMT);
                setAttribute(document, image, INDEX_IMAGE_ATTR, String.valueOf(img.getKey()));
                setAttribute(document, image, FILE_IMAGE_ATTR, img.getValue());
                images.appendChild(image);
            }
            slElement.appendChild(images);
        }
        return slElement;
    }
}
