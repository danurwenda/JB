/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.event.KeyEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Slurp
 */
public class ScrollAction extends WidgetAction.Adapter {

    private boolean isADown //angle
            , isWDown //width
            , isEDown //length
            , isWMDown //width multiplier
            , isAMDown //angle multiplier
            , isLMDown //length multiplier
            ;
    private final LayoutScene scene;

    public ScrollAction(LayoutScene s) {
        this.scene = s;
        isADown = false;
        isWDown = false;
        isEDown = false;
        isWMDown = false;
        isAMDown = false;
        isLMDown = false;
    }

    @Override
    public State keyReleased(Widget widget, WidgetKeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_A) {
            isADown = false;
        } else if (event.getKeyCode() == KeyEvent.VK_E) {
            isEDown = false;
        } else if (event.getKeyCode() == KeyEvent.VK_W) {
            isWDown = false;
        } else if (event.getKeyCode() == KeyEvent.VK_SLASH) {
            isWMDown = false;
        } else if (event.getKeyCode() == KeyEvent.VK_SEMICOLON) {
            isAMDown = false;
        } else if (event.getKeyCode() == KeyEvent.VK_QUOTE) {
            isLMDown = false;
        }
        return super.keyReleased(widget, event); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_A) {
            isADown = true;
        } else if (event.getKeyCode() == KeyEvent.VK_E) {
            isEDown = true;
        } else if (event.getKeyCode() == KeyEvent.VK_W) {
            isWDown = true;
        } else if (event.getKeyCode() == KeyEvent.VK_SLASH) {
            isWMDown = true;
        } else if (event.getKeyCode() == KeyEvent.VK_SEMICOLON) {
            isAMDown = true;
        } else if (event.getKeyCode() == KeyEvent.VK_QUOTE) {
            isLMDown = true;
        }
        return super.keyPressed(widget, event); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
        int amount = event.getWheelRotation();

        //filter before for-loop
        if (isADown || isWDown || isEDown || isWMDown || isAMDown || isLMDown) { //mod structure
            for (Object o : scene.getSelectedObjects()) {
                if (o instanceof SubLayoutLayer) {
                    SubLayoutLayer sll = (SubLayoutLayer) o;
                    if (isADown) {
                        sll.setAngle(sll.getAngle() - amount);
                    }
                    if (isAMDown) {
                        if (amount < 0 || sll.getAngleMultiplier() > 0.1) {
                            float c = sll.getAngleMultiplier() - amount * 0.1f;
                            sll.setAngleMultiplier(c < 0.1 ? 0.1f : c > 6 ? 6 : c);
                        }
                    }
                    if (isWDown) {
                        if (amount < 0 || sll.getWidth() > 1) {
                            sll.setWidth(sll.getWidth() - amount);
                        }
                    }
                    if (isWMDown) {
                        if (amount < 0 || sll.getWidthMultiplier() > 0.1) {
                            float c = sll.getWidthMultiplier() - amount * 0.1f;
                            sll.setWidthMultiplier(c < 0.1 ? 0.1f : c > 6 ? 6 : c);
                        }
                    }
                    if (isEDown) {
                        if (amount < 0 || sll.getLength() > 1) {
                            sll.setLength(sll.getLength() - amount);
                        }
                    }
                    if (isLMDown) {
                        if (amount < 0 || sll.getLengthMultiplier() > 0.1) {
                            float c = sll.getLengthMultiplier() - amount * 0.1f;
                            sll.setLengthMultiplier(c < 0.1 ? 0.1f : c > 6 ? 6 : c);
                        }
                    }
                    //done changing structure
                    //redraw
                    sll.getSublayout().getRenderer().render();
                }
            }

            return State.CONSUMED;
        }
        return super.mouseWheelMoved(widget, event); //To change body of generated methods, choose Tools | Templates.
    }

}
