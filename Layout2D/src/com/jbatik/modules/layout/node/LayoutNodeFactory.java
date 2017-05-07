/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.node;

import com.jbatik.core.project.JBatikProject;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 * NodeFactory for "Corak" folder which contains .cor files
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
@NodeFactory.Registration(projectType = "com-jbatik-project", position = 20)
public class LayoutNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        JBatikProject proj = p.getLookup().lookup(JBatikProject.class);
        assert proj != null;
        return new LayoutDirNodeList(proj);
    }

    private class LayoutDirNodeList implements NodeList<Node> {

        JBatikProject project;

        public LayoutDirNodeList(JBatikProject p) {
            this.project = p;
            //find "lib" and "lays" folder inside the project directory
            //create if they are not exist
            FileObject lay = LayoutFileUtil.getLayoutsFolder(project, true);
            assert lay != null;
            FileObject lib = LayoutFileUtil.getLibrariesFolder(project, true);
            assert lib != null;
        }

        @Override
        public List<Node> keys() {
            FileObject laysDirFO = LayoutFileUtil.getLayoutsFolder(project, false);
            DataFolder laysDirDF = DataFolder.findFolder(laysDirFO);
            List<Node> result = new ArrayList<>();

            result.add(laysDirDF.getNodeDelegate());
            return result;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public Node node(Node simpleNode) {
            return new LayoutFilterNode(simpleNode, project);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }
    }

}
