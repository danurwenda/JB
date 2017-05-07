/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.component;

import javax.media.j3d.Node;
import javax.vecmath.Point3d;

/**
 *
 * @author RAPID01
 */
public class MyPickResult {
    private Point3d point;
    private Node node;

    public MyPickResult() {}

    public MyPickResult(Point3d point, Node node) {
        this.point = point;
        this.node = node;
    }

    public Point3d getPoint() {
        return point;
    }

    public void setPoint(Point3d point) {
        this.point = point;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
