/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package com.jbatik.modules.layout.explorer.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.awt.QuickSearch;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Quick search support for JTable
 *
 * @author Martin Entlicher
 */
class TableQuickSearchSupport implements QuickSearch.Callback {

    private int quickSearchInitialRow = -1;     // The search was initiated here
    private int quickSearchInitialColumn = -1;  // The search was initiated here
    private int quickSearchLastRow = -1;        // Last search position
    private int quickSearchLastColumn = -1;     // Last search position
    private String lastSearchText;

    private JTable table;
    //private StringValuedTable svTable;
    private QuickSearchTableFilter quickSearchTableFilter;
    private QuickSearchSettings qss;

    TableQuickSearchSupport(JTable table, QuickSearchTableFilter quickSearchTableFilter, QuickSearchSettings qss) {
        this.table = table;
        this.quickSearchTableFilter = quickSearchTableFilter;
        this.qss = qss;
    }

    public void setQuickSearchTableFilter(QuickSearchTableFilter quickSearchTableFilter, boolean asynchronous) {
        this.quickSearchTableFilter = quickSearchTableFilter;
    }

    @Override
    public void quickSearchUpdate(String searchText) {
        lastSearchText = searchText;
        if (quickSearchInitialRow == -1) {
            quickSearchInitialRow = table.getSelectedRow();
            quickSearchInitialColumn = table.getSelectedColumn();
            if (quickSearchInitialRow == -1) {
                quickSearchInitialRow = 0;
            }
            if (quickSearchInitialColumn == -1) {
                quickSearchInitialColumn = 0;
            }
        }
        quickSearchLastRow = quickSearchInitialRow;
        quickSearchLastColumn = quickSearchInitialColumn;
        doSearch(searchText, true);
    }

    @Override
    public void showNextSelection(boolean forward) {
        if (forward) {
            if (++quickSearchLastColumn >= table.getColumnCount()) {
                quickSearchLastColumn = 0;
                if (++quickSearchLastRow >= table.getRowCount()) {
                    quickSearchLastRow = 0;
                }
            }
        }
        doSearch(lastSearchText, forward);
    }

    @Override
    public String findMaxPrefix(String prefix) {
        String prefixUp;
        if (qss.isMatchCase()) {
            prefixUp = prefix;
        } else {
            prefixUp = prefix.toUpperCase();
        }
        int row1 = 0;
        int row2 = table.getRowCount();
        int col1 = 0;
        int col2 = table.getColumnCount();
        String maxPrefix = null;
        for (int row = row1; row < row2; row++) {
            for (int col = col1; col < col2; col++) {
                String str = quickSearchTableFilter.getStringValueAt(row, col);
                String strUp;
                if (qss.isMatchCase()) {
                    strUp = str;
                } else {
                    strUp = str.toUpperCase();
                }
                if (strUp.startsWith(prefixUp)) {
                    if (maxPrefix == null) {
                        maxPrefix = str;
                    } else {
                        maxPrefix = QuickSearch.findMaxPrefix(maxPrefix, str, !qss.isMatchCase());
                    }
                }
            }
        }
        if (maxPrefix != null) {
            return maxPrefix;
        } else {
            return prefix;
        }
    }

    @Override
    public void quickSearchConfirmed() {
        quickSearchInitialRow = -1;
        quickSearchInitialColumn = -1;
    }

    @Override
    public void quickSearchCanceled() {
        displaySearchResult(quickSearchInitialRow, quickSearchInitialColumn);
        quickSearchInitialRow = -1;
        quickSearchInitialColumn = -1;
    }

    private void doSearch(String searchText, boolean forward) {
        if (!qss.isMatchCase()) {
            searchText = searchText.toUpperCase();
        }
        int n = table.getRowCount();
        //boolean backward = bias == Position.Bias.Backward;
        int row1 = quickSearchLastRow;
        int row2 = quickSearchLastRow + n;
        boolean lineStartSearch = true;
        Set<String> columnsIgnoredToSearch = qss.getColumnsIgnoredToSearch();
        do {
            int col1 = quickSearchLastColumn;
            int col2 = (forward) ? table.getColumnCount() : 0;
            for (int row = (forward) ? row1 : (row2 - 1); (forward) ? row < row2 : row >= row1; row = (forward) ? ++row : --row) {
                for (int col = col1; (forward) ? col < col2 : col >= col2; col = (forward) ? ++col : --col) {
                    String cName = table.getColumnName(col);
                    if (columnsIgnoredToSearch.contains(cName)) {
                        continue;
                    }
                    String str = quickSearchTableFilter.getStringValueAt(row % n, col);
                    if (str == null) {
                        continue;
                    }
                    if (!qss.isMatchCase()) {
                        str = str.toUpperCase();
                    }
                    if (lineStartSearch) {
                        if (str.startsWith(searchText)) {
                            displaySearchResult(row % n, col);
                            return;
                        }
                    } else {
                        if (str.indexOf(searchText) >= 0) {
                            displaySearchResult(row % n, col);
                            return;
                        }
                    }
                }
                col1 = (forward) ? 0 : table.getColumnCount() - 1;
            }
            lineStartSearch = !lineStartSearch;
        } while (!lineStartSearch);
        // nothing found, remove the selection:
        table.getSelectionModel().clearSelection();
        table.getColumnModel().getSelectionModel().clearSelection();
        table.scrollRectToVisible(table.getCellRect(quickSearchInitialRow, quickSearchInitialColumn, true));
    }

    private void displaySearchResult(int row, int column) {
        quickSearchLastRow = row;
        quickSearchLastColumn = column;
        table.getSelectionModel().setSelectionInterval(row, row);
        table.getColumnModel().getSelectionModel().setSelectionInterval(column, column);
        table.scrollRectToVisible(table.getCellRect(row, column, true));
    }

    JMenu createSearchPopupMenu() {
        return new JMenu() {

            @Override
            public JPopupMenu getPopupMenu() {
                return TableQuickSearchSupport.getSearchPopupMenu(qss, table.getColumnModel(), new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doSearch(lastSearchText, true);
                    }
                });
            }

        };
    }

    static JPopupMenu getSearchPopupMenu(final QuickSearchSettings qss, final TableColumnModel columnModel,
            final ActionListener doSearchAction) {
        JPopupMenu pm = new JPopupMenu();
        final JCheckBoxMenuItem matchCase = new JCheckBoxMenuItem(
                NbBundle.getMessage(PhotoshopOutlineView.class, "CTL_MatchCase"),
                qss.isMatchCase());
        ItemListener iListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JCheckBoxMenuItem cMenu = (JCheckBoxMenuItem) e.getItemSelectable();
                if (cMenu == matchCase) {
                    qss.setMatchCase(cMenu.isSelected());
                } else {
                    String cName = cMenu.getText();
                    if (cMenu.isSelected()) {
                        qss.addColumnToSearch(cName);
                    } else {
                        qss.removeColumnFromSearch(cName);
                    }
                }
                doSearchAction.actionPerformed(null);
                //doSearch(lastSearchText, Bias.Forward);
            }
        };
        matchCase.addItemListener(iListener);
        pm.add(matchCase);
        pm.addSeparator();
        Enumeration<TableColumn> columns = columnModel.getColumns();
        if (columns.hasMoreElements()) {
            JMenuItem description = new JMenuItem(NbBundle.getMessage(PhotoshopOutlineView.class, "CTL_ColumnsToSearch")) {
                @Override
                public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
                    // Ignore
                }

                @Override
                protected void processMouseEvent(MouseEvent e) {
                    // Ignore
                }
            };
            description.setFocusable(false);
            description.setModel(new DummyButtonModel());
            pm.add(description);
        }
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            String cName = column.getHeaderValue().toString();
            JCheckBoxMenuItem cMenu = new JCheckBoxMenuItem(
                    cName,
                    !qss.getColumnsIgnoredToSearch().contains(cName));
            cMenu.addItemListener(iListener);
            pm.add(cMenu);
        }
        return pm;
    }

    static final class QuickSearchSettings {

        private boolean matchCase = NbPreferences.forModule(QuickSearchSettings.class).getBoolean("matchCase", false);
        private Set<String> columnsIgnoredToSearch = new HashSet<String>();

        public boolean isMatchCase() {
            return matchCase;
        }

        public void setMatchCase(boolean matchCase) {
            this.matchCase = matchCase;
            NbPreferences.forModule(QuickSearchSettings.class).putBoolean("matchCase", matchCase);
        }

        public Set<String> getColumnsIgnoredToSearch() {
            return columnsIgnoredToSearch;
        }

        public void addColumnToSearch(String columnName) {
            columnsIgnoredToSearch.remove(columnName);
        }

        public void removeColumnFromSearch(String columnName) {
            columnsIgnoredToSearch.add(columnName);
        }

    }

    static interface StringValuedTable {

        String getStringValueAt(int row, int col);

    }

    static final class DummyButtonModel implements ButtonModel {

        @Override
        public boolean isArmed() {
            return false;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean isPressed() {
            return false;
        }

        @Override
        public boolean isRollover() {
            return false;
        }

        @Override
        public void setArmed(boolean b) {
        }

        @Override
        public void setSelected(boolean b) {
        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public void setPressed(boolean b) {
        }

        @Override
        public void setRollover(boolean b) {
        }

        @Override
        public void setMnemonic(int key) {
        }

        @Override
        public int getMnemonic() {
            return 0;
        }

        @Override
        public void setActionCommand(String s) {
        }

        @Override
        public String getActionCommand() {
            return null;
        }

        @Override
        public void setGroup(ButtonGroup group) {
        }

        @Override
        public void addActionListener(ActionListener l) {
        }

        @Override
        public void removeActionListener(ActionListener l) {
        }

        @Override
        public void addItemListener(ItemListener l) {
        }

        @Override
        public void removeItemListener(ItemListener l) {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public Object[] getSelectedObjects() {
            return new Object[]{};
        }
    }

}
