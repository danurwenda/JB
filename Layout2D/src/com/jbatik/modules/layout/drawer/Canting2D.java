/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.drawer;

import com.jbatik.lsystem.turtle.BaseVector;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.visual.widgets.SquareWidget;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class Canting2D {

    //active parameters for drawing
    private boolean isMirror;
    private float currentAngle;
    private float currentLength;
    private float currentWidth;
    private int currentColorIdx;
    private BaseVector bv;//turtle utilities for drawing

    //sublayout scoped parameters
    private Map<Character, String> rules;
    private SubLayout sublayout;
    private LinkedList<Canting2DState> cantingStack;
    private float currentSqRot;
    private float globalSqRot;
    private Scene scene;
    //list of SquareWidget grouped by its colorindex
    private HashMap<Integer, ArrayList<SquareWidget>> colorSquareMap;

    public HashMap<Integer, ArrayList<SquareWidget>> getColorSquareMap() {
        return colorSquareMap;
    }

    /**
     * Path (on disk) of the project containing the SubLayout that will be drawn
     *
     * @param projectPath
     */
    public Canting2D(SubLayout s) {
        this.sublayout = s;
        cantingStack = new LinkedList<>();
    }

    public void render(Scene s) {
        this.scene = s;
        this.colorSquareMap = new HashMap<>();
        this.rules = sublayout.getRules();
        //create the square widgets based on the LSystem parameters
        this.currentAngle = sublayout.getAngle();
        this.currentLength = sublayout.getLength();
        this.currentWidth = sublayout.getWidth();
        this.currentSqRot = 0;
        this.isMirror = false;
        this.globalSqRot = sublayout.getSquareRotationAngle();
        this.currentColorIdx = 1;
        this.bv = new BaseVector.Two();

//        System.err.println("position " + bv.center.toString());
//        System.err.println("direction " + bv.dir.toString());
        generate(sublayout.getAxiom(), sublayout.getIteration());
    }

    private void generate(String cmds, int iter) {
        char current;
        boolean valued;
        float val = 0;
        int closeIdx = 0;
        String sVal;
        for (int i = 0; i < cmds.length(); i++) {
            current = cmds.charAt(i);
            String detail = rules.get(current);
            if (detail != null && iter > 0) {
                generate(detail, iter - 1);
            } else {
                /**
                 * TODO : gimana ini caranya biar list of terminal symbols ini
                 * bisa expanded/overriden by another module. Perlu konsul ke yg
                 * ngerti design Netbeans Platform :| sementara gini aja dulu
                 */

                /**
                 * Should be a terminal symbol, langsung cek value nya
                 */
                valued = false;
                if (i + 1 < cmds.length()) {
                    if (cmds.charAt(i + 1) == '(')//get the value
                    {
                        closeIdx = cmds.indexOf(')', i);//get the ) char idx
                        sVal = cmds.substring(i + 2, closeIdx);//get the string of value

                        if (current != '*') {
                            val = Float.parseFloat(sVal);//persentase
                        }

                        valued = true;
                        //////System.out.println("i v.s closeIdx "+i+" "+closeIdx);
                        i = closeIdx;
                    }
                }
                switch (current) {
                    case '#': {
                        i = cmds.length();
                        break;
                    }
                    case '*': {
//                        System.out.println("generate(external .lay file)");
//                        generate(sVal);
                        break;
                    }
                    case 'F': {
//                        System.out.println("draw F");
//                        if (valued) {
//                            drawF(val);
//                        } else {
                        drawF(currentLength);
//                        }
                        break;
                    }
                    case 'g':
                    case 'f': {
//                        System.out.println("move, record vertex");
//                        if (valued) {
//                            move(val);
//                        } else {
                        move(currentLength);
//                        }
                        break;
                    }
                    case 'Z': {
//                        System.out.println("draw Z");
//                        if (valued) {
//                            drawF(val);
//                        } else {
                        drawF(currentLength / 2);
//                        }
                        break;
                    }
                    case 'z': {
//                        System.out.println("move halfway, record vertex");
//                        if (valued) {
//                            move(val);
//                        } else {
                        move(currentLength / 2);
//                        }
                        break;
                    }
                    case 'c': {
//                        System.out.println("change color");
                        if (valued) {
                            setColorIndex(val);
                        } else {
                            currentColorIdx++;
                        }
                        break;
                    }

                    case '+': {
//                        System.out.println("turn right");
                        if (valued) {
                            turn(isMirror ? val : -val);
                        } else {
                            turn(isMirror ? currentAngle : -currentAngle); //true = turn right
                        }
                        break;
                    }
                    case '-': {
//                        System.out.println("turn left");
                        if (valued) {
                            turn(isMirror ? -val : val);
                        } else {
                            turn(isMirror ? -currentAngle : currentAngle); //true = turn right
                        }
                        break;
                    }

                    case '|': {
//                        System.out.println("mirror");
                        isMirror = !isMirror;
                        break;
                    }

                    case '~':
//                        System.out.println("reflection");
                        break;

                    case '[': {
//                        System.out.println("start branch");
                        startBranch();
                        break;
                    }
                    case ']': {
//                        System.out.println("end branch");
                        endBranch();
                        break;
                    }
                    case '<': {
                        if (valued) {
                            roll(val);
                        } else {
                            roll(currentAngle);
                        }
                        break;
                    }
                    case '>': {
                        if (valued) {
                            roll(-val);
                        } else {
                            roll(-currentAngle);
                        }
                        break;
                    }
                    /**
                     * INCREMENT / DECREMENT
                     */
                    case '\'': {
//                        System.out.println("decrease length");
                        if (valued) {
                            currentLength *= val;
                        } else {
                            currentLength /= sublayout.getLengthMultiplier();
                        }
                        break;
                    }
                    case '"': {
//                        System.out.println("increase length");
                        if (valued) {
                            currentLength *= val;
                        } else {
                            currentLength *= sublayout.getLengthMultiplier();
                        }
                        break;
                    }
                    case '!': {
//                        System.out.println("decrease thickness");
                        if (valued) {
                            currentWidth *= val;
                        } else {
                            currentWidth /= sublayout.getWidthMultiplier();
                        }
                        break;
                    }
                    case '?': {
//                        System.out.println("increase thickness");
                        if (valued) {
                            currentWidth *= val;
                        } else {
                            currentWidth *= sublayout.getWidthMultiplier();
                        }
                        break;
                    }
                    case ':': {
//                        System.out.println("decrease angle");
                        if (valued) {
                            currentAngle *= val;
                        } else {
                            currentAngle /= sublayout.getAngleMultiplier();
                        }
                        break;
                    }
                    case ';': {
//                        System.out.println("increase angle");
                        if (valued) {
                            currentAngle *= val;
                        } else {
                            currentAngle *= sublayout.getAngleMultiplier();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    private void drawF(float v) {
        move(v);
        createSquare();
    }

    private void move(float v) {
        bv.Move(v);
        bv.normalizeAll();
    }

    private void roll(float v) {
        this.currentSqRot += v;
    }

    private void createSquare() {
        SquareWidget child = new SquareWidget(scene, currentWidth, currentColorIdx);
        child.setSquareRotationAngle(globalSqRot + currentSqRot);
        child.setPreferredLocation(new Point((int) bv.center.x, (int) bv.center.y));
        child.setDirection(new Point2D.Double(bv.dir.x, bv.dir.y));
        child.setMirrored(isMirror);
        addSquareWidget(currentColorIdx, child);
    }

    public void addSquareWidget(int idx, SquareWidget child) {
        getSquareWidgetList(idx, true).add(child);
    }

    private ArrayList<SquareWidget> getSquareWidgetList(int idx, boolean create) {
        ArrayList<SquareWidget> list = colorSquareMap.get(idx);
        if (list == null && create) {
            list = new ArrayList<>();
            colorSquareMap.put(idx, list);
        }
        return list;
    }

    private void setColorIndex(float val) {
        this.currentColorIdx = (int) Math.floor(val);
    }

    private void turn(float f) {
        bv.turn(f);
    }

    private void startBranch() {
        cantingStack.push(new Canting2DState(this));
    }

    private void endBranch() {
        Canting2DState state = cantingStack.pop();
        this.bv.set(state.v);
        this.currentAngle = state.a;
        this.currentColorIdx = state.c;
        this.currentLength = state.l;
        this.currentWidth = state.w;
        this.currentSqRot = state.s;
    }

    /**
     * Special parameters that should be saved before starting a branch
     */
    private static class Canting2DState {

        float a, l, w, s;
        int c;
        BaseVector v;

        public Canting2DState(Canting2D c2d) {
            this.s = c2d.currentSqRot;
            this.a = c2d.currentAngle;
            this.l = c2d.currentLength;
            this.w = c2d.currentWidth;
            this.c = c2d.currentColorIdx;
            this.v = new BaseVector();
            this.v.set(c2d.bv);
        }
    }
}
