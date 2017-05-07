/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak;

import com.jbatik.lsystem.InvalidableVisualLSystem;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.modules.corak.canting.SurfaceParser;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;

/**
 * This class provides additional structure for displaying a VisualLSystem as a
 * 3-dimensional "Corak". The additional structure specifies color and texture
 * (or anything) used on a particular index.
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class CorakLSystem extends InvalidableVisualLSystem {

    public static Color DEFAULT_COLOR = Color.WHITE;
    public static String DEFAULT_TEXTURE = "blank.png";

    private Color3f background;
    private List<String> textures;
    private List<Color> colors;
    private List<Surface> surfaces;

    private final List<String> texturesUm;
    private final List<Color> colorsUm;
    private final List<Surface> surfacesUm;

    public CorakLSystem(String ax, String d, int i, int a, int l, int w, List cs, List ts, List ss) {
        //call invalidable constructor
        super(ax, d, i, a, l, w);

        this.colors = cs != null ? cs : getDefaultColors();
        this.textures = ts != null ? ts : getDefaultTextures();
        this.surfaces = ss != null ? ss : new ArrayList();
        this.surfacesUm = Collections.unmodifiableList(surfaces);
        this.colorsUm = Collections.unmodifiableList(colors);
        this.texturesUm = Collections.unmodifiableList(textures);
        background = new Color3f();
    }

    public CorakLSystem(String string, HashMap<Character, String> m, int i, int a, int l, int w, List cs, List ts, List ss) {
        super(string, m, i, a, l, w);
        this.colors = cs != null ? cs : getDefaultColors();
        this.textures = ts != null ? ts : getDefaultTextures();
        this.surfaces = ss != null ? ss : new ArrayList();
        this.surfacesUm = Collections.unmodifiableList(surfaces);
        this.colorsUm = Collections.unmodifiableList(colors);
        this.texturesUm = Collections.unmodifiableList(textures);

        background = new Color3f();
    }

    /**
     * Get the value of background
     *
     * @return the value of background
     */
    public Color3f getBackground() {
        return background;
    }

    /**
     * Set the value of background
     *
     * @param background new value of background
     */
    public void setBackground(Color3f background) {
        this.background = background;
    }

    // butuh getter buat tipe data Color juga biar gak bingung
    public List<Color> getColors() {
        return colorsUm;
    }

    public List<Color3f> getColor3fs() {
        List<Color3f> cs = new ArrayList<>();
        this.colors.stream().forEach((c) -> {
            cs.add(new Color3f(c));
        });
        return cs;
    }

    public void setColor(List<Color> color) {
        colors.clear();
        color.stream().forEach((c) -> {
            colors.add(c);
        });
    }

    public List<String> getTextures() {
        return texturesUm;
    }

    public void setTextures(List<String> t) {
        textures.clear();
        t.stream().forEach((c) -> {
            textures.add(c);
        });
    }

    public List<Surface> getSurfaces() {
        return surfacesUm;
    }

    public void setSurfaces(List<Surface> s) {
        surfaces.clear();
        s.stream().forEach((c) -> {
            surfaces.add(c);
        });
    }

    @Override
    public void setRules(Map<Character, String> rules) throws ParseRuleException {
        super.setRules(rules);
        if (surfaces != null) {
            try {
                //special handle FUnit
                SurfaceParser sp = new SurfaceParser(this);
                sp.parseRules(rules);
            } catch (ParseRuleException ex) {
                throw ex;
            } finally {
                this.rules = rules;
            }
        }
    }

    @Override
    public void setAxiom(String axiom) throws ParseRuleException {
        super.setAxiom(axiom);
        if (surfaces != null) {
            try {
                //special handle FUnit
                SurfaceParser sp = new SurfaceParser(this);
                sp.parseAxiom(axiom);
            } catch (ParseRuleException ex) {
                throw ex;
            } finally {
                this.axiom = axiom;
            }
        }
    }

    private List<Color> getDefaultColors() {
        List<Color> l = new ArrayList<>();
        l.add(new Color(0, 128, 0, 255));
        l.add(new Color(0, 128, 128, 255));
        l.add(new Color(0, 255, 0, 255));
        l.add(new Color(0, 255, 255, 255));
        l.add(new Color(0, 0, 255, 255));
        l.add(new Color(128, 0, 128, 255));
        l.add(new Color(255, 0, 0, 255));
        l.add(new Color(128, 128, 0, 255));
        l.add(new Color(255, 0, 255, 255));
        l.add(new Color(255, 255, 0, 255));
        l.add(new Color(128, 128, 128, 255));
        l.add(new Color(255, 255, 255, 255));
        return l;
    }

    private List<String> getDefaultTextures() {
        List<String> l = new ArrayList<>();
        l.add("Text00.jpg");
        l.add("Text01.jpg");
        l.add("Text02.jpg");
        l.add("Text03.jpg");
        l.add("Text04.jpg");
        l.add("Text05.jpg");
        l.add("Text06.jpg");
        l.add("Text07.jpg");
        l.add("Text08.jpg");
        l.add("Text09.jpg");
        l.add("Text10.jpg");
        l.add("Text11.jpg");
        return l;
    }
}
