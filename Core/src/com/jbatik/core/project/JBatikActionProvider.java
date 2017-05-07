/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.project;

import com.jbatik.core.project.ui.MyProjectOperationsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.util.Lookup;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
final class JBatikActionProvider implements ActionProvider {

    private final JBatikProject p;

    public JBatikActionProvider(JBatikProject aThis) {
        this.p = aThis;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
            COMMAND_COPY,
            COMMAND_MOVE,
            COMMAND_RENAME,
            COMMAND_DELETE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(p);
            return;
        }
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(p);
            return;
        }
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(p);
            return;
        }
        if (COMMAND_RENAME.equals(command)) {
            if (p == null) {
                throw new IllegalArgumentException("Project is null");
            }

            if (!ProjectOperations.isMoveOperationSupported(p)) {
                throw new IllegalArgumentException("Attempt to rename project that does not support move.");
            }

            MyProjectOperationsImplementation.renameProject(p, "");
            return;
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        switch (command) {
            case COMMAND_RENAME:
            case COMMAND_MOVE:
            case COMMAND_COPY:
            case COMMAND_DELETE:
                return true;
        }
        return false;
    }

}
