/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author RAPID02
 */
public class PhotoshopOutlineCellRenderer extends DefaultTableCellRenderer {

    private static int expansionHandleWidth = 0;
    private static int expansionHandleHeight = 0;
    private boolean expanded = false;
    private boolean leaf = true;
    private boolean showHandle = true;
    private int nestingDepth = 0;
    private final TristateCheckBox theCheckBox;
    private final CellRendererPane fakeCellRendererPane;
    private JCheckBox checkBox;
    private Reference<RenderDataProvider> lastRendererRef = new WeakReference<>(null); // Used by lazy tooltip
    private Reference<Object> lastRenderedValueRef = new WeakReference<>(null);                    // Used by lazy tooltip
    private static final Border expansionBorder = new ExpansionHandleBorder();
    private static final Class htmlRendererClass = useSwingHtmlRendering() ? null : HtmlRenderer.getDelegate();
    private final HtmlRenderer.Renderer htmlRenderer = (htmlRendererClass != null) ? HtmlRenderer.createRenderer(htmlRendererClass) : null;
    private final boolean swingRendering = htmlRenderer == null;

    private static boolean useSwingHtmlRendering() {
        try {
            return Boolean.getBoolean("nb.useSwingHtmlRendering");              // NOI18N
        } catch (SecurityException se) {
            return false;
        }
    }

    /**
     * Creates a new instance of DefaultOutlineTreeCellRenderer
     */
    public PhotoshopOutlineCellRenderer() {
        theCheckBox = new TristateCheckBox();
        theCheckBox.setSize(theCheckBox.getPreferredSize());
        theCheckBox.setBorderPainted(false);
        theCheckBox.setOpaque(false);
        // In order to paint the check-box correctly, following condition must be true:
        // SwingUtilities.getAncestorOfClass(CellRendererPane.class, theCheckBox) != null
        // (See e.g.: paintSkin() method in com/sun/java/swing/plaf/windows/XPStyle.java)
        fakeCellRendererPane = new CellRendererPane();
        fakeCellRendererPane.add(theCheckBox);
    }

    /**
     * Overridden to combine the expansion border (whose insets determine how
     * much a child tree node is shifted to the right relative to the ancestor
     * root node) with whatever border is set, as a CompoundBorder. The
     * expansion border is also responsible for drawing the expansion icon.
     *
     * @param b the border to be rendered for this component
     */
    @Override
    public final void setBorder(Border b) {
        if (!swingRendering) {
            super.setBorder(b);
            return;
        }
        if (b == expansionBorder) {
            super.setBorder(b);
        } else {
            super.setBorder(BorderFactory.createCompoundBorder(b, expansionBorder));
        }
    }

    @Override
    protected void setValue(Object value) {
        if (swingRendering) {
            super.setValue(value);
        }
    }

    private static Icon getDefaultOpenIcon() {
        return UIManager.getIcon("Tree.openIcon"); //NOI18N
    }

    private static Icon getDefaultClosedIcon() {
        return UIManager.getIcon("Tree.closedIcon"); //NOI18N
    }

    private static Icon getDefaultLeafIcon() {
        return UIManager.getIcon("Tree.leafIcon"); //NOI18N
    }

    static Icon getExpandedIcon() {
        return UIManager.getIcon("Tree.expandedIcon"); //NOI18N
    }

    static Icon getCollapsedIcon() {
        return UIManager.getIcon("Tree.collapsedIcon"); //NOI18N
    }

    static int getNestingWidth() {
        return getExpansionHandleWidth();
    }

    static int getExpansionHandleWidth() {
        if (expansionHandleWidth == 0) {
            expansionHandleWidth = getExpandedIcon().getIconWidth();
        }
        return expansionHandleWidth;
    }

    static int getExpansionHandleHeight() {
        if (expansionHandleHeight == 0) {
            expansionHandleHeight = getExpandedIcon().getIconHeight();
        }
        return expansionHandleHeight;
    }

    private void setNestingDepth(int i) {
        nestingDepth = i;
    }

    private void setExpanded(boolean val) {
        expanded = val;
    }

    private void setLeaf(boolean val) {
        leaf = val;
    }

    private void setShowHandle(boolean val) {
        showHandle = val;
    }

    private void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

    private boolean isLeaf() {
        return leaf;
    }

    private boolean isExpanded() {
        return expanded;
    }

    private boolean isShowHandle() {
        return showHandle;
    }

    /**
     * Set the nesting depth - the number of path elements below the root. This
     * is set in getTableCellEditorComponent(), and retrieved by the expansion
     * border to determine how far to the right to indent the current node.
     */
    private int getNestingDepth() {
        return nestingDepth;
    }

    private JCheckBox getCheckBox() {
        return checkBox;
    }

    int getTheCheckBoxWidth() {
        return theCheckBox.getSize().width;
    }

    /**
     * Get a component that can render cells in an Outline. If
     * <code>((Outline) table).isTreeColumnIndex(column)</code> is true, it will
     * paint as indented and with an expansion handle if the Outline's model
     * returns false from <code>isLeaf</code> for the passed value.
     * <p>
     * If the column is not the tree column, its behavior is the same as
     * DefaultTableCellRenderer.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row,
            int column) {

        super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        JLabel label = null;
        if (!swingRendering) {
            label = (JLabel) htmlRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        PhotoshopOutline tbl = (PhotoshopOutline) table;
        if (tbl.isTreeColumnIndex(column)) {
            AbstractLayoutCache layout = tbl.getLayoutCache();
            row = tbl.convertRowIndexToModel(row);
            boolean isleaf = tbl.getOutlineModel().isLeaf(value);
            setLeaf(isleaf);
            setShowHandle(true);
            TreePath path = layout.getPathForRow(row);
            boolean isExpanded = layout.isExpanded(path);
            setExpanded(isExpanded);
            int nd = path == null ? 0 : path.getPathCount() - (tbl.isRootVisible() ? 1 : 2);
            if (nd < 0) {
                nd = 0;
            }
            setNestingDepth(nd);
            RenderDataProvider rendata = tbl.getRenderDataProvider();
            Icon icon = null;
            if (rendata != null && value != null) {
                String displayName = rendata.getDisplayName(value);
                if (displayName != null) {
                    if (rendata.isHtmlDisplayName(value) && !(displayName.startsWith("<html") || displayName.startsWith("<HTML"))) {
                        if (swingRendering) {
                            setText("<html>" + displayName.replaceAll(" ", "&nbsp;") + "</html>"); // NOI18N
                        } else {
                            label.setText("<html>" + displayName.replaceAll(" ", "&nbsp;") + "</html>"); // NOI18N
                        }
                    } else {
                        if (swingRendering) {
                            setText(displayName);
                        } else {
                            label.setText(displayName);
                        }
                    }
                }
                lastRendererRef = new WeakReference<RenderDataProvider>(rendata);
                lastRenderedValueRef = new WeakReference<Object>(value);
                Color bg = rendata.getBackground(value);
                Color fg = rendata.getForeground(value);
                if (bg != null && !isSelected) {
                    if (swingRendering) {
                        setBackground(bg);
                    } else {
                        label.setBackground(bg);
                    }
                } else {
                    if (!swingRendering) {
                        label.setBackground(getBackground());
                    }
                }
                if (fg != null && !isSelected) {
                    if (swingRendering) {
                        setForeground(fg);
                    } else {
                        label.setForeground(fg);
                    }
                } else {
                    if (!swingRendering) {
                        label.setForeground(getForeground());
                    }
                }
                icon = rendata.getIcon(value);

                TristateCheckBox cb = null;
                if (rendata instanceof CheckRenderDataProvider) {
                    CheckRenderDataProvider crendata = (CheckRenderDataProvider) rendata;
                    if (crendata.isCheckable(value)) {
                        cb = theCheckBox;
                        Boolean chSelected = crendata.isSelected(value);
                        cb.setEnabled(true);
                        cb.setSelected(!Boolean.FALSE.equals(chSelected));
                        // Third state is "selected armed" to be consistent with org.openide.explorer.propertysheet.ButtonModel3Way
                        // using tristate cb, the possible states are 
                        // selected (chSelected = TRUE), half-selected (chSelected = null), and unselected (chSelected = FALSE)
                        cb.setHalfSelected(chSelected == null);
                        cb.getModel().setArmed(chSelected == null);
                        cb.getModel().setPressed(chSelected == null);
                        cb.setEnabled(crendata.isCheckEnabled(value));
                        cb.setBackground(getBackground());
                    }
                }
                setCheckBox(cb);
            } else {
                setCheckBox(null);
            }
            if (icon == null) {
                if (!isleaf) {
                    if (isExpanded) {
                        icon = getDefaultOpenIcon();
                    } else { // ! expanded
                        icon = getDefaultClosedIcon();
                    }
                } else { // leaf
                    icon = getDefaultLeafIcon();
                }
            }
            if (swingRendering) {
                setIcon(icon);
            } else {
                label.setIcon(icon);
            }

        } else { // ! tbl.isTreeColumnIndex(column)
            setCheckBox(null);
            if (swingRendering) {
                setIcon(null);
            } else {
                label.setIcon(null);
            }
            setShowHandle(false);
            lastRendererRef = new WeakReference<RenderDataProvider>(null);
            lastRenderedValueRef = new WeakReference<Object>(null);
        }

        if (swingRendering) {
            return this;
        } else {
            Border b = getBorder();
            if (b == null) {
                label.setBorder(expansionBorder);
            } else {
                label.setBorder(BorderFactory.createCompoundBorder(b, expansionBorder));
            }
            label.setOpaque(true);

            label.putClientProperty(PhotoshopOutlineCellRenderer.class, this);
            return label;
        }
    }

    @Override
    public String getToolTipText() {
        // Retrieve the tooltip only when someone asks for it...
        RenderDataProvider rendata = lastRendererRef.get();
        Object value = lastRenderedValueRef.get();
        if (rendata != null && value != null) {
            String toolT = rendata.getTooltipText(value);
            if (toolT != null && (toolT = toolT.trim()).length() > 0) {
                return toolT;
            }
        }
        return super.getToolTipText();
    }

    private static class ExpansionHandleBorder implements Border {

        private static final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

        private Insets insets = new Insets(0, 0, 0, 0);
        private static JLabel lExpandedIcon = null;
        private static JLabel lCollapsedIcon = null;

        {
            if (isGtk) {
                lExpandedIcon = new JLabel(getExpandedIcon(), SwingUtilities.TRAILING);
                lCollapsedIcon = new JLabel(getCollapsedIcon(), SwingUtilities.TRAILING);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            PhotoshopOutlineCellRenderer ren = (PhotoshopOutlineCellRenderer) ((JComponent) c).getClientProperty(PhotoshopOutlineCellRenderer.class);
            if (ren == null) {
                ren = (PhotoshopOutlineCellRenderer) c;
            }
            if (ren.isShowHandle()) {
                //isShowHandle == true means first column
                //in this photoshop style, getCheckBox can't be null
                //since the check box represents visibility state
                insets.left = ren.getNestingDepth() * getNestingWidth() + (ren.isLeaf() ? 0 : getExpansionHandleWidth());
                //Defensively adjust all the insets fields
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            } else {
                //Defensively adjust all the insets fields
                insets.left = 1;
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            }
            if (ren.getCheckBox() != null) {
                insets.left += ren.getCheckBox().getSize().width;
            }
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            PhotoshopOutlineCellRenderer ren = (PhotoshopOutlineCellRenderer) ((JComponent) c).getClientProperty(PhotoshopOutlineCellRenderer.class);
            if (ren == null) {
                ren = (PhotoshopOutlineCellRenderer) c;
            }
            JCheckBox chBox = ren.getCheckBox();
            if (chBox != null) {
                //draw check box as leftmost column.
                Dimension chDim = chBox.getSize();
                java.awt.Graphics gch = g.create(0, 0, chDim.width, chDim.height);
                chBox.paint(gch);
            }
            if (ren.isShowHandle() && !ren.isLeaf()) {
                //isShowHandle == true means first column which displays nodes
                //isLeaf == false means it's a node with children (may be empty)
                Icon handleIcon = ren.isExpanded() ? getExpandedIcon() : getCollapsedIcon();
                int iconY;
                int iconX = ren.getNestingDepth() * getNestingWidth() + (chBox == null ? 0 : chBox.getSize().width);
                if (handleIcon.getIconHeight() < height) {
                    iconY = (height / 2) - (handleIcon.getIconHeight() / 2);
                } else {
                    iconY = 0;
                }
                if (isGtk) {
                    JLabel lbl = ren.isExpanded() ? lExpandedIcon : lCollapsedIcon;
                    lbl.setSize(Math.max(getExpansionHandleWidth(), iconX + getExpansionHandleWidth()), height);
                    lbl.paint(g);
                } else {
                    handleIcon.paintIcon(c, g, iconX, iconY);
                }
            }
        }
    }

    /**
     * Use reflection to access org.openide.awt.HtmlRenderer class so that we do
     * not have to have a dependency on org.openide.awt module.
     */
    private static final class HtmlRenderer {

        private static final String HTML_RENDERER_CLASS = "org.openide.awt.HtmlRenderer";   // NOI18N

        static Class getDelegate() {
            Class delegate;
            try {
                delegate = ClassLoader.getSystemClassLoader().loadClass(HTML_RENDERER_CLASS);
            } catch (ClassNotFoundException ex) {
                try {
                    delegate = Thread.currentThread().getContextClassLoader().loadClass(HTML_RENDERER_CLASS);
                } catch (ClassNotFoundException ex2) {
                    // We are searching for org.openide.awt.HtmlRenderer class.
                    // However, we can not find it directly from the system class loader.
                    // We need to find it via Lookup
                    try {
                        Class lookupClass = ClassLoader.getSystemClassLoader().loadClass("org.openide.util.Lookup");    // NOI18N
                        try {
                            Object defaultLookup = lookupClass.getMethod("getDefault").invoke(null);    // NOI18N
                            ClassLoader systemClassLoader = (ClassLoader) lookupClass.getMethod("lookup", Class.class).invoke(defaultLookup, ClassLoader.class);    // NOI18N
                            if (systemClassLoader == null) {
                                return null;
                            }
                            delegate = systemClassLoader.loadClass(HTML_RENDERER_CLASS);
                        } catch (NoSuchMethodException mex) {
                            Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (SecurityException mex) {
                            Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (IllegalAccessException mex) {
                            Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (IllegalArgumentException mex) {
                            Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (InvocationTargetException mex) {
                            Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                    } catch (ClassNotFoundException ex3) {
                        return null;
                    } catch (SecurityException se) {
                        return null;
                    }
                } catch (SecurityException se) {
                    return null;
                }
            } catch (SecurityException se) {
                return null;
            }
            return delegate;
        }

        private static Renderer createRenderer(Class htmlRendererClass) {
            try {
                Method createRenderer = htmlRendererClass.getMethod("createRenderer");                  // NOI18N
                return new Renderer(createRenderer.invoke(null));
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (SecurityException ex) {
                Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (InvocationTargetException ex) {
                Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        private static class Renderer {

            private Object renderer;
            private Method getTableCellRendererComponent;

            private Renderer(Object renderer) throws NoSuchMethodException {
                this.renderer = renderer;
                this.getTableCellRendererComponent = TableCellRenderer.class.getMethod(
                        "getTableCellRendererComponent", // NOI18N
                        JTable.class, Object.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE);
            }

            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected, boolean leadSelection, int row, int column
            ) {
                try {
                    return (Component) getTableCellRendererComponent.invoke(
                            renderer,
                            table, value, selected, leadSelection, row, column);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(PhotoshopOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                }
            }
        }

    }
}
