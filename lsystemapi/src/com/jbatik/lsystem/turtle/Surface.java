/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.turtle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class Surface {

    private String name;
    private Point3d[] points;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point3d[] getPoints() {
        return points;
    }
    
    public Point3d getMidPoint() {
        double midx = 0;
        double midy = 0;
        double midz = 0;
        for (int i=0; i<pointsNum(); i++) {
            midx += points[i].x;
            midy += points[i].y;
            midz += points[i].z;
        }
        midx /= pointsNum();
        midy /= pointsNum();
        midz /= pointsNum();
        return new Point3d(midx, midy, midz);
    }

    public void setPoints(Point3d[] points) {
        this.points = points;
    }

    public int pointsNum() {
        return points == null ? 0 : points.length;
    }
    
    public Icon getPreviewIcon(int width) {
        Icon icon = new Icon() {

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.WHITE);
        
                int n = pointsNum();
                int mid = width/2;
                Point2d[] p2d = new Point2d[n];
                double maxX = 0;
                double maxY = 0;
                //find max
                for (int i=0; i<n; i++) {
                    if (Math.abs(points[i].x) > Math.abs(maxX)) maxX = Math.abs(points[i].x);
                    if (Math.abs(points[i].y) > Math.abs(maxY)) maxY = Math.abs(points[i].y);
                }
                double max = maxX > maxY ? maxX : maxY;
                double scale = width/max * 0.3;
                //scale, rotate, translate
                //awt graphics has different coordinate system
                //draw
                for (int i=0; i<n-1; i++) {
                    p2d[i] = new Point2d((points[i].y * scale) + mid, (points[i].x * scale) + mid);
                    p2d[i+1] = new Point2d((points[i+1].y * scale) + mid, (points[i+1].x * scale) + mid);
                    g.drawLine(Math.round((float) p2d[i].x + x), 
                            Math.round((float) p2d[i].y + y), 
                            Math.round((float) p2d[i+1].x + x), 
                            Math.round((float) p2d[i+1].y + y));
                }
                g.drawLine(Math.round((float) p2d[n-1].x + x), 
                            Math.round((float) p2d[n-1].y + y), 
                            Math.round((float) p2d[0].x + x), 
                            Math.round((float) p2d[0].y + y));
                
            }

            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return width;
            }
        };
                
        return icon;
    }
}
