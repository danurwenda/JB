/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.project;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
class JBatikProjectLogicalView implements LogicalViewProvider {

    private final JBatikProject project;

    public JBatikProjectLogicalView(JBatikProject aThis) {
        this.project = aThis;
    }

    @Override
    public Node createLogicalView() {
        return new JBatikProjectRootNode(project);
    }

    private static final class JBatikProjectRootNode extends AbstractNode {

        

        public static final String REGISTERED_NODE_LOCATION
                = "Projects/com-jbatik-project/Nodes";//NOI18N

        final JBatikProject project;

        public JBatikProjectRootNode(JBatikProject project) {
            super(NodeFactorySupport.createCompositeChildren(project, REGISTERED_NODE_LOCATION), Lookups.singleton(project));
            this.project = project;
            setIconBaseWithExtension(JBatikProject.JBATIK_PROJECT_ICON_PATH);
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[]{       
                CommonProjectActions.renameProjectAction(),
                CommonProjectActions.moveProjectAction(),
                CommonProjectActions.copyProjectAction(),
                CommonProjectActions.deleteProjectAction(),
                CommonProjectActions.closeProjectAction()};
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(JBatikProject.JBATIK_PROJECT_ICON_PATH);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        /**
         * Full name of project directory. If we use getName instead, a project
         * directory with "abc.def" as its name will be displayed as "abc" only
         * @return 
         */
        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getNameExt();
        }

        @NbBundle.Messages("TXT_Project_Node=jBatik Project in ")
        @Override
        public String getShortDescription() {
            return Bundle.TXT_Project_Node() + project.getProjectDirectory().getPath();
        }
    }

    @Override
    public Node findPath(Node root, Object target) {
        //unimplemented for now
        return null;
    }

}
