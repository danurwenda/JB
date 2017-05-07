/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.node;

import com.jbatik.core.project.JBatikProject;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
class LayoutFilterNode extends FilterNode {

    @StaticResource
    private static final String LAYOUT_DIR_ICON = "com/jbatik/modules/layout/node"
            + "/project-layout2d-icon.png";

    /**
     * Given a directory of Layout files, create its node with appropriate
     * selection of Children
     *
     * @param layDirNode Layout directory which contains .lay files
     * @param project a JBatikProject
     */
    public LayoutFilterNode(Node layDirNode, JBatikProject project) {
        super(
                layDirNode,
                new LayoutFilterChildren(layDirNode),
                new ProxyLookup(
                        Lookups.singleton(project),
                        layDirNode.getLookup()
                )
        );
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(LAYOUT_DIR_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @NbBundle.Messages({
        "TXT_Layout_Dir=Layout files",
        "TXT_Layout_Dir_Desc=A folder containing layout files"
    })
    @Override
    public String getDisplayName() {
        return Bundle.TXT_Layout_Dir();
    }

    // TODO : override getActions
    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> actions
                = Utilities.actionsForPath("Projects/com-jbatik-project/Actions/Layout");
        return actions.toArray(new Action[actions.size()]);
//        return new Action[]{
//            //            CommonProjectActions.newFileAction(),
//            PasteAction.get(PasteAction.class)
//        };
    }

    @Override
    public String getShortDescription() {
        return Bundle.TXT_Layout_Dir_Desc();
    }

    /**
     * We override this class as a mechanism to filter those nodes we will
     * display under the Lays directory node on the project explorer
     */
    private static class LayoutFilterChildren extends FilterNode.Children {

        public LayoutFilterChildren(Node corDirNode) {
            super(corDirNode);
        }

        @Override
        protected Node[] createNodes(Node key) {
            List<Node> result = new ArrayList<>();
            for (Node node : super.createNodes(key)) {
                //assuming that .lay file is already made recognized by another
                //module, we can filter those nodes from .lay file through their
                //lookup
                // FIXME : ini bisa ga jangan pake extensi? pakai lookup atau mime type gitu
                if (node.getDisplayName().endsWith(".lay") || node.getDisplayName().endsWith(".LAY")) {
//                if (node.getLookup().lookup(LayoutDataObject.class) != null) {//dafuq man ini bikin warning dan load lama
                    result.add(node);
                }
            }
            return result.toArray(new Node[0]);
        }

    }

}
