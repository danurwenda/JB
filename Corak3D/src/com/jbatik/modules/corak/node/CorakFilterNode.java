/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.node;

import com.jbatik.core.project.JBatikProject;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.actions.PasteAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
class CorakFilterNode extends FilterNode{

    @StaticResource
    private static final String CORAK_DIR_ICON = "com/jbatik/modules/corak/node"
            + "/project-corak3d-icon.png";
    
//    String[] type= new String[]{"New Corak"};

    /**
     * Given a directory of Corak files, create its node with appropriate
     * selection of Children
     *
     * @param corDir Corak directory which contains .cor files
     * @param project a JBatikProject
     */
    public CorakFilterNode(Node corDirNode, JBatikProject project) {
        super(corDirNode, new CorakFilterChildren(corDirNode),
                new ProxyLookup(new Lookup[]{Lookups.singleton(project),
                    corDirNode.getLookup()})
        );
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(CORAK_DIR_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @NbBundle.Messages({
        "TXT_Corak_Dir=Corak files",
        "TXT_Corak_Dir_Desc=A folder containing corak files"
    })
    @Override
    public String getDisplayName() {
        return Bundle.TXT_Corak_Dir();
    }

    // TODO : override getActions
    @Override
    public Action[] getActions(boolean context) {
        List<Action> acts = new ArrayList<>(7);
        acts.add(PasteAction.get(PasteAction.class));
        List<? extends Action> actions
                = Utilities.actionsForPath("Projects/com-jbatik-project/Actions/Corak");
        acts.addAll(actions);
        return acts.toArray(new Action[acts.size()]);
    }

    @Override
    public String getShortDescription() {
        return Bundle.TXT_Corak_Dir_Desc();
    }      

//    @Override
//    public String[] getRecommendedTypes() {
//        return type;
//    }
    
    /**
     * We override this class as a mechanism to filter those nodes that we will
     * display under the Cors directory node on the project explorer
     */
    private static class CorakFilterChildren extends FilterNode.Children {

        public CorakFilterChildren(Node corDirNode) {
            super(corDirNode);
        }

        @Override
        protected Node[] createNodes(Node key) {
            List<Node> result = new ArrayList<>();
            for (Node node : super.createNodes(key)) {
                //assuming that .cor file is already made recognized by another
                //module, we can filter those nodes from .cor file through their
                //lookup
                // FIXME : ini bisa ga jangan pake extensi? pakai lookup atau mime type gitu
                if (node.getDisplayName().toLowerCase().endsWith(".cor")) {
//                if (node.getLookup().lookup(CorakDataObject.class) != null) {
                    result.add(node);
                }                
            }            
            return result.toArray(new Node[0]);
        }                

    }

}
