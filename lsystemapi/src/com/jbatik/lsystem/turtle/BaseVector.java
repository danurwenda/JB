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
 * turtle position and directions
 *
 * @author RAPID01
 */
public class BaseVector {

    public static class Two extends BaseVector {

        public Two() {
            this.center = new Point3d();
            this.v1 = new Vector3d(-1.0, 0.0, 0.0); //start from left
            this.dir = new Vector3d(0.0, -1.0, 0.0); //facing up in 2d
            this.norm = new Vector3d();
            this.norm.cross(dir, v1);
        }

    }
    public Point3d center;
    public Vector3d dir; //index finger in the right hand rule, to which the turtle is facing
    public Vector3d v1; //middle finger in the right hand rule 
    public Vector3d norm; //thumb in the right hand rule, dir * v1

    public BaseVector(BaseVector newBaseVector) {
        this.center = new Point3d(newBaseVector.center);
        this.dir = new Vector3d(newBaseVector.dir);
        this.v1 = new Vector3d(newBaseVector.v1);
        this.norm = new Vector3d(newBaseVector.norm);
    }

    public BaseVector() {
        this.center = new Point3d();
        this.v1 = new Vector3d(-1.0, 0.0, 0.0); //start from left
        this.dir = new Vector3d(0.0, 1.0, 0.0); //facing up
        this.norm = new Vector3d();
        this.norm.cross(dir, v1);
    }

    public void set(BaseVector newBaseVector) {
        this.center.set(newBaseVector.center);
        this.dir.set(newBaseVector.dir);
        this.v1.set(newBaseVector.v1);
        this.norm.set(newBaseVector.norm);
    }

    public void Move(double length) {
        center.x += dir.x * length;
        center.y += dir.y * length;
        center.z += dir.z * length;
    }

    public void Move(Point3d p) {
        center.set(p);
    }

    public void turn(double angle) {
        Matrix3d m = VectorUtil.v3Rot(norm, angle);
        m.transform(dir);
        v1.cross(norm, dir);
        normalizeAll();
    }

    public void pitch(double angle) {
        Matrix3d m = VectorUtil.v3Rot(v1, angle);
        m.transform(dir);
        norm.cross(dir, v1);
        normalizeAll();
    }

    public void roll(double angle) {
        Matrix3d m = VectorUtil.v3Rot(dir, angle);
        m.transform(v1);
        norm.cross(dir, v1);
        normalizeAll();
    }

    public void normalizeAll() {
        dir.normalize();
        v1.normalize();
        norm.normalize();
    }

    /**
     * given kondisi baseVector saat ini, dan suatu surface, kembalikan titik2
     * surface tersebut jika center of surface adalah center of baseVector.
     *
     * TODO : move to implementor module. Class Surface/Tube should not be in LSystem
     * API. Lindenmayer himself had no idea about the concept of Surface.
     *
     * @param t
     * @param s
     * @return
     */
    public Point3d[] vRound(float t, Surface s) {
        if (s.pointsNum() < 1) {
            return null;
        } else {
            //base for kanan-kiri
            Vector3d baseV1 = new Vector3d(this.v1.x * t, this.v1.y * t, this.v1.z * t);
            //base for atas-bawah
            Vector3d baseNorm = new Vector3d(this.norm.x * t, this.norm.y * t, this.norm.z * t);
            //base for depan-belakang
            Vector3d baseDir = new Vector3d(this.dir.x * t, this.dir.y * t, this.dir.z * t);
            Point3d basePoints[] = new Point3d[s.pointsNum()];
            Point3d cur;
            Point3d[] points = s.getPoints();
            for (int i = 0; i < s.pointsNum(); i++) {
                cur = points[i];
                Vector3d xbp = new Vector3d(baseV1);
                xbp.scale(cur.x);
                Vector3d ybp = new Vector3d(baseNorm);
                ybp.scale(cur.y);
                Vector3d zbp = new Vector3d(baseDir);
                zbp.scale(cur.z);
//            System.err.println("xbp " + xbp);
//            System.err.println("ybp " + ybp);
                xbp.add(ybp);
                xbp.add(zbp);
                //translate semua ke center
                basePoints[i] = new Point3d(xbp);
                basePoints[i].add(this.center);
            }
            return basePoints;
        }
    }

    /**
     * Mengembalikan matriks rotasi M yang setara dengan kondisi base vektor
     * saat ini dengan asumsi titik pusat base vektor tetap di O Dengan aturan
     * seperti ini, maka berlaku MB=C dimana B adalah 0 -1 0 1 0 0 0 0 1 dan C
     * adalah dir.x p1.x norm.x dir.y p1.y norm.y dir.z p1.z norm.z
     *
     * Matriks M sendiri didapat dari rumus M = C(B)^{-1} dimana (B)^{-1}
     * menyatakan invers dari B
     *
     * @return Matrix3f : matriks rotasi
     */
    public Matrix3d getMatRot() {
        Matrix3d mat = new Matrix3d();
        Matrix3d B = new Matrix3d(
                0, -1, 0,
                1, 0, 0,
                0, 0, 1);
        B.invert();
        Matrix3d C = new Matrix3d(
                dir.x, v1.x, norm.x,
                dir.y, v1.y, norm.y,
                dir.z, v1.z, norm.z);
        mat.mul(C, B);
        return mat;
    }

}
