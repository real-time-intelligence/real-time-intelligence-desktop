package ru.rti.desktop.view.handler.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.exception.EmptyNameException;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.exception.NotSelectedRowException;
import ru.rti.desktop.model.local.LoadDataMode;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.handler.LifeCycleStatus;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.query.MainQueryPanel;
import ru.rti.desktop.view.panel.config.query.MetadataQueryPanel;
import ru.rti.desktop.view.panel.config.query.QueryPanel;

import static ru.rti.desktop.model.view.handler.LifeCycleStatus.*;

@Log4j2
@Singleton
public class QueryButtonPanelHandler implements ActionListener {

    private final ProfileManager profileManager;
    private final EventListener eventListener;

    private final JXTableCase profileCase;
    private final JXTableCase taskCase;
    private final JXTableCase connectionCase;
    private final JXTableCase queryCase;
    private final QueryPanel queryPanel;
    private final ButtonPanel queryButtonPanel;
    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final JXTableCase configMetadataCase;
    private final JTabbedPane mainQuery;
    private final JCheckBox checkboxConfig;

    private final MainQueryPanel mainQueryPanel;
    private final MetadataQueryPanel metadataQueryPanel;
    private LifeCycleStatus status;
    private final ResourceBundle bundleDefault;

    @Inject
    public QueryButtonPanelHandler(@Named("profileManager") ProfileManager profileManager,
                                   @Named("eventListener") EventListener eventListener,
                                   @Named("profileConfigCase") JXTableCase profileCase,
                                   @Named("taskConfigCase") JXTableCase taskCase,
                                   @Named("connectionConfigCase") JXTableCase connectionCase,
                                   @Named("queryConfigCase") JXTableCase queryCase,
                                   @Named("configMetadataCase") JXTableCase configMetadataCase,
                                   @Named("queryConfigPanel") QueryPanel queryPanel,
                                   @Named("queryButtonPanel") ButtonPanel queryButtonPanel,
                                   @Named("mainQueryPanel") MainQueryPanel mainQueryPanel,
                                   @Named("metadataQueryPanel") MetadataQueryPanel metadataQueryPanel,
                                   @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                   @Named("mainQueryTab") JTabbedPane mainQuery,
                                   @Named("checkboxConfig") JCheckBox checkboxConfig) {
        this.profileManager = profileManager;
        this.profileCase = profileCase;
        this.taskCase = taskCase;
        this.connectionCase = connectionCase;
        this.queryCase = queryCase;
        this.queryPanel = queryPanel;
        this.queryButtonPanel = queryButtonPanel;
        this.configMetadataCase = configMetadataCase;
        this.mainQuery = mainQuery;

        this.mainQueryPanel = mainQueryPanel;
        this.metadataQueryPanel = metadataQueryPanel;

        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.checkboxConfig = checkboxConfig;

        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.queryButtonPanel.getBtnNew().addActionListener(this);
        this.queryButtonPanel.getBtnCopy().addActionListener(this);
        this.queryButtonPanel.getBtnDel().addActionListener(this);
        this.queryButtonPanel.getBtnEdit().addActionListener(this);
        this.queryButtonPanel.getBtnSave().addActionListener(this);
        this.queryButtonPanel.getBtnCancel().addActionListener(this);

        this.queryButtonPanel.getBtnDel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        this.queryButtonPanel.getBtnDel().getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryButtonPanel.getBtnDel().doClick();
            }
        });


        this.queryButtonPanel.getBtnCancel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        this.queryButtonPanel.getBtnCancel().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryButtonPanel.getBtnCancel().doClick();
            }
        });

        this.status = LifeCycleStatus.NONE;

        this.eventListener = eventListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == queryButtonPanel.getBtnNew()) {

            status = NEW;
            setPanelView(false);
            newEmptyPanel();
            clearProfileMetadataCase();

        } else if (e.getSource() == queryButtonPanel.getBtnCopy()) {

            status = COPY;
            if (queryCase.getJxTable().getSelectedRow() == -1) {
                throw new NotSelectedRowException("The query to copy is not selected. Please select and try again!");
            } else {
                setPanelView(false);

                int queryId = getSelectedQueryId();
                QueryInfo query = profileManager.getQueryInfoById(queryId);
                if (Objects.isNull(query)) {
                    throw new NotFoundException("Not found query: " + queryId);
                }

                mainQueryPanel.getQueryName().setText(query.getName() + "_copy");
                mainQueryPanel.getQueryDescription().setText(query.getDescription() + "_copy");
                mainQueryPanel.getQueryGatherDataComboBox().setSelectedItem(query.getGatherDataSql());
                mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(query.getGatherDataSql());
                mainQueryPanel.getQuerySqlText().setText(query.getText());
            }

        } else if (e.getSource() == queryButtonPanel.getBtnDel()) {
            if (queryCase.getJxTable().getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(null, "Not selected query. Please select and try again!",
                        "General Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int queryId = getSelectedQueryId();
                int input = JOptionPane.showConfirmDialog(new JDialog(),// 0=yes, 1=no, 2=cancel
                        "Do you want to delete configuration: " + queryCase.getDefaultTableModel()
                                .getValueAt(queryCase.getJxTable().getSelectedRow(), 1) + "?");
                if (isUsedOnTask(queryId)) {
                    if (input == 0) {
                        QueryInfo query = profileManager.getQueryInfoById(queryId);
                        if (Objects.isNull(query)) {
                            throw new NotFoundException("Not found query by id: " + queryId);
                        }

                        profileManager.deleteQuery(query.getId(), query.getName());
                        profileManager.deleteTable(query.getName());

                        clearQueryCase();

                        profileManager.getQueryInfoList().forEach(queryInfo -> queryCase.getDefaultTableModel()
                                .addRow(new Object[]{queryInfo.getId(), queryInfo.getName()}));

                        if (queryCase.getJxTable().getSelectedRow() > 0) {
                            queryCase.getJxTable().setRowSelectionInterval(0, 0);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Cannot delete this query it is used in the task",
                            "General Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == queryButtonPanel.getBtnEdit()) {
            if (queryCase.getJxTable().getSelectedRow() == -1) {
                throw new NotSelectedRowException("Not selected task. Please select and try again!");
            }
            status = EDIT;
            setPanelView(false);

        } else if (e.getSource() == queryButtonPanel.getBtnSave()) {
            if (NEW.equals(status) || COPY.equals(status)) {

                AtomicInteger queryIdNext = new AtomicInteger();

                profileManager.getQueryInfoList().stream()
                        .max(Comparator.comparing(QueryInfo::getId))
                        .ifPresentOrElse(query -> queryIdNext.set(query.getId()),
                                () -> {
                                    log.info("Not found Query");
                                    queryIdNext.set(0);
                                });

                if (!queryPanel.getMainQueryPanel().getQueryName().getText().trim().isEmpty()) {
                    int queryId = queryIdNext.incrementAndGet();
                    String newQueryName = queryPanel.getMainQueryPanel().getQueryName().getText();
                    checkQueryNameIsBusy(queryId, newQueryName);

                    QueryInfo queryInfo = new QueryInfo();
                    queryInfo.setId(queryId);
                    queryInfo.setName(mainQueryPanel.getQueryName().getText());
                    queryInfo.setDescription(mainQueryPanel.getQueryDescription().getText());

                    String selectedEnumGatherData = Objects.requireNonNull(mainQueryPanel.getQueryGatherDataComboBox()
                            .getSelectedItem()).toString();
                    String selectedEnumLoadData = Objects.requireNonNull(mainQueryPanel.getQueryLoadDataModeComboBox()
                            .getSelectedItem()).toString();

                    queryInfo.setGatherDataSql(GatherDataSql.valueOf(selectedEnumGatherData));
                    queryInfo.setLoadDataMode(LoadDataMode.valueOf(selectedEnumLoadData));
                    queryInfo.setText(mainQueryPanel.getQuerySqlText().getText());

                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName(queryInfo.getName());

                    profileManager.addQuery(queryInfo);
                    profileManager.addTable(tableInfo);

                    clearQueryCase();

                    int selection = 0;
                    int index = 0;
                    for (QueryInfo query : profileManager.getQueryInfoList()) {
                        queryCase.getDefaultTableModel().addRow(new Object[]{query.getId(), query.getName()});

                        if (query.getId() == queryInfo.getId()) {
                            index++;
                            selection = index;
                        }
                        index++;
                    }

                    setPanelView(true);
                    queryCase.getJxTable().setRowSelectionInterval(selection - 1, selection - 1);
                } else {
                    throw new EmptyNameException("The name field is empty");
                }
            } else if (EDIT.equals(status)) {
                int queryId = getSelectedQueryId();

                if (!queryPanel.getMainQueryPanel().getQueryName().getText().trim().isEmpty()) {

                    int selectedIndex = queryCase.getJxTable().getSelectedRow();
                    String newQueryName = queryPanel.getMainQueryPanel().getQueryName().getText();
                    checkQueryNameIsBusy(queryId, newQueryName);

                    QueryInfo oldQuery = profileManager.getQueryInfoById(queryId);

                    QueryInfo editQuery = new QueryInfo();
                    editQuery.setId(queryId);
                    editQuery.setName(mainQueryPanel.getQueryName().getText());
                    editQuery.setDescription(mainQueryPanel.getQueryDescription().getText());
                    String selectedEnumGatherData = Objects.requireNonNull(mainQueryPanel.getQueryGatherDataComboBox().getSelectedItem())
                            .toString();
                    String selectedEnumLoadData = Objects.requireNonNull(mainQueryPanel.getQueryLoadDataModeComboBox().getSelectedItem())
                            .toString();
                    editQuery.setGatherDataSql(GatherDataSql.valueOf(selectedEnumGatherData));
                    editQuery.setLoadDataMode(LoadDataMode.valueOf(selectedEnumLoadData));
                    editQuery.setText(mainQueryPanel.getQuerySqlText().getText());

                    clearProfileMetadataCase();

                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName(editQuery.getName());

                    if (!oldQuery.getName().equals(newQueryName)) {
                        deleteQueryById(queryId);
                        profileManager.addQuery(editQuery);
                        profileManager.addTable(tableInfo);
                    } else {
                        profileManager.updateQuery(editQuery);
                    }

                    clearQueryCase();

                    for (QueryInfo q : profileManager.getQueryInfoList()) {
                        queryCase.getDefaultTableModel()
                                .addRow(new Object[]{q.getId(), q.getName()});
                    }
                    setPanelView(true);

                    mainQueryPanel.getQueryGatherDataComboBox().setSelectedItem(GatherDataSql.valueOf(selectedEnumGatherData));
                    mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(LoadDataMode.valueOf(selectedEnumLoadData));
                    queryCase.getJxTable().setRowSelectionInterval(selectedIndex, selectedIndex);

                } else {
                    throw new EmptyNameException("The name field is empty");
                }

            }
        } else if (e.getSource() == queryButtonPanel.getBtnCancel()) {
            if (queryCase.getJxTable().getSelectedRowCount() > 0) {
                int selectedIndex = queryCase.getJxTable().getSelectedRow();
                queryCase.getJxTable().setRowSelectionInterval(0, 0);
                setPanelView(true);
                queryCase.getJxTable().setRowSelectionInterval(selectedIndex, selectedIndex);
                int queryId = getSelectedQueryId();
                QueryInfo queryInfo = profileManager.getQueryInfoById(queryId);
                if (Objects.isNull(queryInfo)) {
                    throw new NotFoundException("Not found query: " + queryId);
                }
                mainQueryPanel.getQueryName().setText(queryInfo.getName());
                mainQueryPanel.getQueryDescription().setText(queryInfo.getDescription());
                mainQueryPanel.getQueryGatherDataComboBox().setSelectedItem(queryInfo.getGatherDataSql());
                if (queryInfo.getLoadDataMode() != null) {
                    mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(queryInfo.getLoadDataMode());
                } else {
                    mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(LoadDataMode.JDBC_REAL_TIME);
                }
                mainQueryPanel.getQuerySqlText().setText(queryInfo.getText());

            } else {
                setPanelView(true);
                newEmptyPanel();
                clearProfileMetadataCase();
            }
        }
    }

    private void newEmptyPanel() {
        mainQueryPanel.getQueryName().setText("");
        mainQueryPanel.getQueryName().setPrompt(bundleDefault.getString("qName"));
        mainQueryPanel.getQueryDescription().setText("");
        mainQueryPanel.getQueryDescription().setPrompt(bundleDefault.getString("qDesc"));
        mainQueryPanel.getQuerySqlText().setText("");
        mainQueryPanel.getQuerySqlText().setPrompt(bundleDefault.getString("qSqlText"));
    }

    public void checkQueryNameIsBusy(int id, String newQueryName) {
        List<QueryInfo> queryList = profileManager.getQueryInfoList();
        for (QueryInfo query : queryList) {
            if (query.getName().equals(newQueryName) && query.getId() != id) {
                throw new NotFoundException("Name " + newQueryName
                        + " already exists, please enter another one.");
            }
        }
    }

    public void deleteQueryById(int id) {
        QueryInfo queryDel = profileManager.getQueryInfoById(id);
        if (Objects.isNull(queryDel)) {
            throw new NotFoundException("Not found query by id: " + id);
        }
        profileManager.deleteQuery(queryDel.getId(), queryDel.getName());
    }

    private void clearProfileMetadataCase() {
        metadataQueryPanel.getConfigMetadataCase().getDefaultTableModel().getDataVector().removeAllElements();
        metadataQueryPanel.getConfigMetadataCase().getDefaultTableModel().fireTableDataChanged();
    }

    private void clearQueryCase() {
        queryCase.getDefaultTableModel().getDataVector().removeAllElements();
        queryCase.getDefaultTableModel().fireTableDataChanged();
    }

    private int getSelectedQueryId() {
        return (Integer) queryCase.getDefaultTableModel()
                .getValueAt(queryCase.getJxTable().getSelectedRow(), 0);
    }

    private void setPanelView(Boolean isSelected) {
        queryButtonPanel.setButtonView(isSelected);
        mainQueryPanel.getQueryName().setEditable(!isSelected);
        mainQueryPanel.getQueryDescription().setEditable(!isSelected);
        mainQueryPanel.getQueryGatherDataComboBox().setEnabled(!isSelected);
        mainQueryPanel.getQueryLoadDataModeComboBox().setEnabled(!isSelected);
        mainQueryPanel.getQuerySqlText().setEditable(!isSelected);
        jTabbedPaneConfig.setEnabledAt(1, isSelected);
        jTabbedPaneConfig.setEnabledAt(2, isSelected);
        jTabbedPaneConfig.setEnabledAt(0, isSelected);
        taskCase.getJxTable().setEnabled(isSelected);
        connectionCase.getJxTable().setEnabled(isSelected);
        profileCase.getJxTable().setEnabled(isSelected);
        queryCase.getJxTable().setEnabled(isSelected);
        mainQuery.setEnabledAt(1, isSelected);
        mainQuery.setEnabledAt(2, isSelected);
        checkboxConfig.setEnabled(isSelected);
    }

    private boolean isUsedOnTask(int queryId) {
        return !profileManager.getTaskInfoList().stream()
                .anyMatch(task -> task.getQueryInfoList().contains(queryId));
    }
}

