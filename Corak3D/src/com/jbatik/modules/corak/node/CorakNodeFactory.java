/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.node;

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
@NodeFactory.Registration(projectType = "com-jbatik-project", position = 10)
public class CorakNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        JBatikProject proj = p.getLookup().lookup(JBatikProject.class);
        assert proj != null;
        return new CorakDirNodeList(proj);
    }

    private class CorakDirNodeList implements NodeList<Node> {

        JBatikProject project;

        public CorakDirNodeList(JBatikProject p) {
            this.project = p;
            //find "textures" and "cors" folder inside the project directory
            //create if they are not exist
            FileObject cor = CorakFileUtil.getCoraksFolder(project, true);
            assert cor != null;
            FileObject tex = CorakFileUtil.getTexturesFolder(project, true);
            assert tex != null;
            FileObject defTex = CorakFileUtil.getDefaultTexture(tex, true);
            assert defTex != null;
        }

        @Override
        public List<Node> keys() {
            FileObject corsDirFO = CorakFileUtil.getCoraksFolder(project, false);
            DataFolder corsDirDF = DataFolder.findFolder(corsDirFO);
            List<Node> result = new ArrayList<>();

            result.add(corsDirDF.getNodeDelegate());
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
            return new CorakFilterNode(simpleNode, project);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }
    }

}
