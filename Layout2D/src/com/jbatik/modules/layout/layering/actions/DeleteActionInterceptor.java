/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.modules.layout.explorer.node.LayoutLayerNode;
import com.jbatik.modules.layout.util.NodeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExtendedDelete;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import static com.jbatik.modules.layout.layering.actions.Bundle.*;

/**
 * Workaround for ExplorerActionsImpl.DeleteActionPerformer
 *
 * Kita hanya tertarik sama Node yang extends LayoutLayerNode. Kalo ternyata
 * dari nodes yang dikirim ga ada yang implement, maka return false.
 *
 * @author RAPID02
 */
@NbBundle.Messages({
    "layersdialog.title=Confirmation",
    "layersdialog.message=Delete the selected layers?"
})
@ServiceProvider(service = ExtendedDelete.class)
public class DeleteActionInterceptor implements ExtendedDelete {

    @Override
    public boolean delete(Node[] nodes) throws IOException {
        //cari dulu ada yang perlu diintercept apa engga
        List<LayoutLayerNode> ln = getLayersNode(nodes);
        if (ln == null || ln.isEmpty()) {
            return false;
        } else {
            //ada node of interest
            //aturan potosop adalah TIDAK BOLEH doDelete jika doDelete tersebut akan
            //menghapus semua base layer (base layer = layer yang ada gambarnya,
            //contohnya group layer itu layer tapi ga ada gambarnya)
            Node root = NodeUtil.getRoot(ln.get(0));
            Set leafFromRoot = NodeUtil.getLeafSet(root);
            Set leafFromSelected = new HashSet();
            ln.stream().forEach((n) -> {
                leafFromSelected.addAll(NodeUtil.getLeafSet(n));
            });
            //jika ternyata sama, berarti ini bakal doDelete all base layer
            //kirim popup ke user kasih informasi
            if (leafFromRoot.equals(leafFromSelected)) {
                //popup
                String msg = "Cannot delete all sublayout layers on scene";
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                //ternyata ga sama, bisa mulai hapus
                if (ln.size() == 1) {
                    //cuma satu
                    ln.get(0).delete();
                } else {
                    //confirmation borongan
                    //send confirmation to user
                    Object result = DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(
                                    layersdialog_message(),
                                    layersdialog_title(),
                                    NotifyDescriptor.YES_NO_OPTION,
                                    NotifyDescriptor.INFORMATION_MESSAGE));
                    if (NotifyDescriptor.YES_OPTION == result) {
                        // do doDelete, without confirmation
                        // but first we have to sort the nodes based on level
                        // so that we'll doDelete nodes from the lowest (deepest) level
                        Collections.sort(ln, (LayoutLayerNode o1, LayoutLayerNode o2) -> NodeUtil.getNodeLevel(o2) - NodeUtil.getNodeLevel(o1));
                        for (LayoutLayerNode lln : ln) {
                            lln.doDelete(false);
                        }
                    }
                }
            }
            return true;
        }
    }

    private List<LayoutLayerNode> getLayersNode(Node[] nodes) {
        if ((nodes == null) || (nodes.length < 1)) {
            return null;
        }
        List<LayoutLayerNode> llnodes = new ArrayList<>();
        for (Node c : nodes) {
            if (c instanceof LayoutLayerNode) {
                llnodes.add((LayoutLayerNode) c);
            }
        }
        return llnodes;
    }

}
