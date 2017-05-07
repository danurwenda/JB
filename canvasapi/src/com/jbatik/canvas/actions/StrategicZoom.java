/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.actions;

import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author RAPID02
 */
public abstract class StrategicZoom {

    /**
     * Human readable display name to be shown on dropdown menu
     *
     * @return a string
     */
    public abstract String getDisplayName();

    /**
     * Zoom factor for this strategy.
     *
     */
    public abstract void zoom();

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * This kind of zoom is dependent to current rectangle view dimension.
     */
    public static abstract class ViewDependent extends StrategicZoom {

        Scene s;

        public Scene getScene() {
            return s;
        }

        public ViewDependent(Scene scene) {
            this.s = scene;

        }
        private Scene.SceneListener sl;

        @Override
        public final void zoom() {
            if (s != null) {
                boolean sizeKnown = !s.getView().getVisibleRect().isEmpty();
                if (!sizeKnown) {
                    // put sceneListener
//                    System.err.println("add sl..");
                    s.addSceneListener(getSceneListener());
                    s.setZoomFactor(0.01);// HINT : trigger scene.revalidate to invoke sl.sceneValidated
                    // FIXME : why revalidate() won't do the work
                } else {
                    s.removeSceneListener(getSceneListener());
//                    System.err.println("nahloh");
                    doZoom(s.getView().getVisibleRect());
                }
            }
        }

        private Scene.SceneListener getSceneListener() {
            if (sl == null) {
                this.sl = new Scene.SceneListener() {

                    @Override
                    public void sceneRepaint() {
                    }

                    @Override
                    public void sceneValidating() {
                    }

                    @Override
                    public void sceneValidated() {
                        //normalnya, pas validated ini visible rect sudah non empty
                        if (!s.getView().getVisibleRect().isEmpty()) {
                            s.removeSceneListener(sl);
//                            System.err.println("terpanggil zoom dari sl");
                            zoom();
                        }
                    }
                };
            }
            return sl;
        }

        public abstract void doZoom(Rectangle r);
    }
}
