/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.canting;

import java.util.ArrayList;
import java.util.HashMap;
import javax.vecmath.Color3f;

/**
 *
 * @author RAPID01
 */
public class CorakStructure {
    private ArrayList<Color3f> colorList;
    private ArrayList<String> textureList;
    private HashMap<Integer, ShapeInfo> colorInfoMap;
    //private final HashMap<Integer, ShapeInfo> textureInfoMap;
    
    public CorakStructure (ArrayList<Color3f> colorList, ArrayList<String> textureList) {
        this.colorList = colorList;
        this.textureList = textureList;
        colorInfoMap = new HashMap<>();
        for (int i=0; i<colorList.size(); i++) {
            colorInfoMap.put(i, new ShapeInfo());
        }
//        textureInfoMap = new HashMap<Integer, ShapeInfo>();
//        for (int i=0; i<textureList.size(); i++) {
//            textureInfoMap.put(i, new ShapeInfo());
//        }
    }
    
    public ArrayList<Color3f> getColorList() {
        return colorList;
    }
    
    public ArrayList<String> getTextureList() {
        return textureList;
    }
    
    public ShapeInfo getShapeInfoByColor(int index) {
        ShapeInfo info = colorInfoMap.get(index);
        return info;
    }
    
    public void setShapeInfoByColor(int index, ShapeInfo info) {
        colorInfoMap.replace(index, info);
    }
    
//    public ShapeInfo getShapeInfoByTexture(int index) {
//        ShapeInfo info = textureInfoMap.get(index);
//        return info;
//    }
}
