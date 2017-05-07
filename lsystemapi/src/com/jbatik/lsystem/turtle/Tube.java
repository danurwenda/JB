/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.lsystem.turtle;

import com.jbatik.lsystem.util.VectorUtil;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class Tube extends Surface {

    public int fSides;

    public Tube() {
        this(8);
        this.setName("Octagon");
    }

    public Tube(int s) {
        this.fSides = s;
        //creating vRound
        setPoints(vRound(fSides));
    }

    //say FSides=8, vRound turns the v1 (360/8) degrees, and create a new
    //point for each turn based on radius (r)
    private Point3d[] vRound(int FSidesNum) {
        Vector3d v1 = new Vector3d(-1.0, 0.0, 0.0);
        Vector3d dir = new Vector3d(0.0,0.0, 1.0);
        Matrix3d m = VectorUtil.v3Rot(dir, 360.0 / FSidesNum);
        Vector3d temp = new Vector3d(v1);
        
        //rotate s.t the first point is not on y+
        //this way we'll have a square instead of a rhombus
        (VectorUtil.v3Rot(dir, (180.0 / FSidesNum) + 180)).transform(temp);

        Point3d basePoints[] = new Point3d[FSidesNum];
        for (int i = 0; i < FSidesNum; i++) {
            basePoints[i] = new Point3d(temp);
            m.transform(temp);
        }
        
        return basePoints;
    }
}