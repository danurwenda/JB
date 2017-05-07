/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.nodes.Node;

/**
 *
 * @author RAPID02
 */
public class NodeUtil {

    /**
     * Make sure ga ada cylic di sini. Soalnya kalo cyclic ini ga bakal beres.
     *
     * @param n
     * @return
     */
    public static int getNodeLevel(Node n) {
        assert n != null;
        if (n.getParentNode() == null) {
            //topmost
            return 0;
        } else {
            return 1 + getNodeLevel(n.getParentNode());
        }
    }

    /**
     * Given a Node, calculate how many LEAF node are on its descendant
     * (including itself).
     *
     * @return the number of leaf
     */
    public static int getLeafNum(Node n) {
        if (n.isLeaf()) {
            return 1;
        } else {
            int sum = 0;
            for (Node c : n.getChildren().getNodes()) {
                sum += getLeafNum(c);
            }
            return sum;
        }
    }

    /**
     * Given a Node, return a set containing all LEAF node on its descendant
     * (including itself).
     *
     * @return the set of leaf nodes
     */
    public static Set getLeafSet(Node n) {
        if (n.isLeaf()) {
            return Collections.singleton(n);
        } else {
            Set sum = new HashSet();
            for (Node c : n.getChildren().getNodes()) {
                sum.addAll(getLeafSet(c));
            }
            return sum;
        }
    }

    /**
     * Given a Node, traverse thru its ancestor to find the root node, that is,
     * node without parent.
     *
     * @param n
     * @return
     */
    public static Node getRoot(Node n) {
        Node p = n.getParentNode();
        if (p == null) {
            return n;
        } else {
            return getRoot(p);
        }
    }
}
