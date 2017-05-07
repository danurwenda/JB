/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.util;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author RAPID02
 */
public class VectorUtil {

    /**
     * http://inside.mines.edu/fs_home/gmurray/ArbitraryAxisRotation/
     *
     * @param p original point of axis
     * @param axis vector of rotation
     * @param angle rotation angle in radians
     * @return
     */
    public static Transform3D rotAroundAxis(Point3d p, Vector3d axis, double angle) {
        //step 1 : Translate space so that the rotation axis passes through the origin.
        Transform3D transToOrigin = null;
        if (!p.equals(new Point3d())) {
            //only if p is not Origin
            transToOrigin = new Transform3D();
            transToOrigin.setTranslation(new Vector3d(-p.x, -p.y, -p.z));
        }
        //step 2 : Rotate space about the z axis so that the rotation axis lies in the xz plane.
//        System.err.println("initial axis " + axis);
        double ang1 = angleRot(axis, 'z', 'x');
        Transform3D rotateZAxis = null;
        if (ang1 != 0) {
            //only if rotation axis does not lie on xz
            rotateZAxis = new Transform3D();
            rotateZAxis.rotZ(ang1);
        }
        //step 3 : Rotate space about the y axis so that the rotation axis lies along the z axis.
        Vector3d axisOnXZ;
        if (rotateZAxis != null) {
            axisOnXZ = new Vector3d();
            rotateZAxis.transform(axis, axisOnXZ);
        } else {
            axisOnXZ = new Vector3d(axis);
        }
//        System.err.println("axisOnXZ " + axisOnXZ);
        double ang2 = angleRot(axisOnXZ, 'y', 'z');
        Transform3D rotateToZ = null;
        if (ang2 != 0) {
            //only if that projection vector does not lie on z axis
            rotateToZ = new Transform3D();
            rotateToZ.rotY(ang2);
        }
        //step 4 : Perform the desired rotation by θ about the z axis.
        Transform3D rotateByTheta = null;
        if (angle != 0) {
            //only if angle of rotation is not zero
            rotateByTheta = new Transform3D();
            rotateByTheta.rotZ(angle);
        }
        //step 5 : Apply the inverse of step (3).
        Transform3D inverse3 = null;
        if (rotateToZ != null) {
            inverse3 = new Transform3D(rotateToZ);
            inverse3.invert();
        }
        //step 6 : Apply the inverse of step (2).
        Transform3D inverse2 = null;
        if (rotateZAxis != null) {
            inverse2 = new Transform3D(rotateZAxis);
            inverse2.invert();
        }
        //step 7 : Apply the inverse of step (1).
        Transform3D inverse1 = null;
        if (transToOrigin != null) {
            inverse1 = new Transform3D(transToOrigin);
            inverse1.invert();
        }

        //creating full transform, reversing the order of multiplying
        Transform3D full = new Transform3D();
        if (inverse1 != null) {
            full.mul(inverse1);
        }
        if (inverse2 != null) {
            full.mul(inverse2);
        }
        if (inverse3 != null) {
            full.mul(inverse3);
        }
        if (rotateByTheta != null) {
            full.mul(rotateByTheta);
        }
        if (rotateToZ != null) {
            full.mul(rotateToZ);
        }
        if (rotateZAxis != null) {
            full.mul(rotateZAxis);
        }
        if (transToOrigin != null) {
            full.mul(transToOrigin);
        }

        return full;
    }

    /**
     * How many radians should we rotate about axisRot such that vector v lies
     * on plane axisRot.anotherAxis
     *
     * @param v
     * @param axisRot
     * @param anotherAxis
     * @return
     */
    public static double angleRot(Vector3d v, char axisRot, char anotherAxis) {
        assert axisRot != anotherAxis;

        Vector3d proj;
        if (axisRot == 'z') {
            proj = new Vector3d(v.x, v.y, 0);
        } else if (axisRot == 'y') {
            proj = new Vector3d(v.x, 0, v.z);
        } else {
            proj = new Vector3d(0, v.y, v.z);
        }
        if (proj.equals(new Vector3d())) {
            //proj = 0,0,0
            return 0;
        } else {
            return method1(proj, axisRot, anotherAxis);
        }
    }

    private static double method1(Vector3d proj, char axisRot, char anotherAxis) {
        double a;
        Vector3d cross = new Vector3d();
        Vector3d x = new Vector3d(1, 0, 0);
        Vector3d y = new Vector3d(0, 1, 0);
        Vector3d z = new Vector3d(0, 0, 1);
        if (anotherAxis == 'x') {
            a = proj.angle(x);
            //cek apakah ini sudah bener arahnya atau musti diminus
            cross.cross(x, proj);
            cross.normalize();
            if ((axisRot == 'z' && cross.angle(z) == 0d)
                    || (axisRot == 'y' && cross.angle(y) == 0d)) {
                a = -a;
            }
        } else if (anotherAxis == 'y') {
            a = proj.angle(y);
            //cek apakah ini sudah bener arahnya atau musti diminus
            cross.cross(y, proj);
            cross.normalize();
            if ((axisRot == 'z' && cross.angle(z) == 0d)
                    || (axisRot == 'x' && cross.angle(x) == 0d)) {
                a = -a;
            }
        } else {
            a = proj.angle(z);
            //cek apakah ini sudah bener arahnya atau musti diminus
            cross.cross(z, proj);
            cross.normalize();
            if ((axisRot == 'y' && cross.angle(y) == 0d)
                    || (axisRot == 'x' && cross.angle(x) == 0d)) {
                a = -a;
            }
        }
        return a;
    }

    /**
     * Returns matrix transformation that correspond to a rotation around an
     * vector with a given angle
     *
     * @param axis rotation axis in the form of a vector from O
     * @param angle rotation angle
     * @return matrix
     */
    public static Matrix3d v3Rot(Vector3d axis, double angle) {
        Matrix3d m = new Matrix3d();
        double x = axis.x;
        double y = axis.y;
        double z = axis.z;
        double a = Math.toRadians(angle);

        double c = Math.cos(a);
        double s = Math.sin(a);
        double t = 1 - c;

        m.setRow(0,
                t * x * x + c,
                t * x * y - s * z,
                t * x * z + s * y);
        m.setRow(1,
                t * x * y + s * z,
                t * y * y + c,
                t * y * z - s * x);
        m.setRow(2,
                t * x * z - s * y,
                t * y * z + s * x,
                t * z * z + c);

        return m;
    }
}
