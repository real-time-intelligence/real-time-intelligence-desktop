package ru.rti.desktop.view.handler.task;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.manager.TemplateManager;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.EditTabbedPane;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.handler.MouseListenerImpl;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.task.MultiSelectQueryPanel;
import ru.rti.desktop.view.panel.config.task.TaskPanel;

@Log4j2
@Singleton
public class TaskSelectionHandler extends MouseListenerImpl implements ListSelectionListener, ItemListener, ChangeListener {

    private final ProfileManager profileManager;
    private final TemplateManager templateManager;

    private final JXTableCase taskCase;
    private final JXTableCase profileCase;
    private final JXTableCase connectionCase;
    private final JXTableCase queryCase;

    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final TaskPanel taskPanel;
    private final MultiSelectQueryPanel multiSelectQueryPanel;
    private final ButtonPanel taskButtonPanel;
    private final JCheckBox checkboxConfig;
    private Boolean isSelected;
    private List<List<?>> connectionDataList;
    private int taskId = -1;
    private final ResourceBundle bundleDefault;

    @Inject
    public TaskSelectionHandler(@Named("profileManager") ProfileManager profileManager,
                                @Named("templateManager") TemplateManager templateManager,
                                @Named("taskConfigCase") JXTableCase taskCase,
                                @Named("profileConfigCase") JXTableCase profileCase,
                                @Named("connectionConfigCase") JXTableCase connectionCase,
                                @Named("queryConfigCase") JXTableCase queryCase,
                                @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                @Named("taskConfigPanel") TaskPanel taskPanel,
                                @Named("multiSelectQueryPanel") MultiSelectQueryPanel multiSelectQueryPanel,
                                @Named("taskButtonPanel") ButtonPanel taskButtonPanel,
                                @Named("checkboxConfig") JCheckBox checkboxConfig) {
        this.profileManager = profileManager;
        this.templateManager = templateManager;

        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.taskCase = taskCase;
        this.profileCase = profileCase;
        this.connectionCase = connectionCase;
        this.queryCase = queryCase;
        this.taskCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.taskCase.getJxTable().addMouseListener(this);

        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.taskPanel = taskPanel;
        this.multiSelectQueryPanel = multiSelectQueryPanel;
        this.taskButtonPanel = taskButtonPanel;
        this.multiSelectQueryPanel.getQueryListCase().getJxTable().getColumnExt(0).setVisible(false);
        this.multiSelectQueryPanel.getSelectedQueryCase().getJxTable().getColumnExt(0).setVisible(false);
        this.multiSelectQueryPanel.getTemplateListQueryCase().getJxTable().getColumnExt(0).setVisible(false);

        this.checkboxConfig = checkboxConfig;
        this.checkboxConfig.addItemListener(this);
        this.isSelected = false;

        this.jTabbedPaneConfig.addChangeListener(this);

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        if (jTabbedPaneConfig.isEnabledAt(1)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.TASK);
        }

        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");

                clearMultiSelectionPanel();
                taskId = -1;

                multiSelectQueryPanel.getJTabbedPaneQuery().setSelectedIndex(0);

                taskPanel.getJTextFieldTask().setEditable(false);
                taskPanel.getJTextFieldDescription().setEditable(false);
                taskPanel.getTaskConnectionComboBox().setEnabled(false);
                taskPanel.getRadioButtonPanel().setButtonNotView();
                multiSelectQueryPanel.getUnPickBtn().setEnabled(false);
                multiSelectQueryPanel.getPickBtn().setEnabled(false);
                multiSelectQueryPanel.getPickAllBtn().setEnabled(false);
                multiSelectQueryPanel.getUnPickAllBtn().setEnabled(false);

                List<ConnectionInfo> connectionAll = profileManager.getConnectionInfoList();
                this.connectionDataList = new LinkedList<>();
                connectionAll.forEach(connection -> connectionDataList.add(
                        new ArrayList<>(Arrays.asList(connection.getName(), connection.getUserName(),
                                connection.getUrl(), connection.getJar(), connection.getDriver()))));

                taskPanel.getJTextFieldTask().setText("");
                taskPanel.getJTextFieldTask().setPrompt(bundleDefault.getString("tName"));
                taskPanel.getJTextFieldDescription().setText("");
                taskPanel.getJTextFieldDescription().setPrompt(bundleDefault.getString("tDesc"));
                taskPanel.getTaskConnectionComboBox().setTableData(connectionDataList);
                taskPanel.getTaskConnectionComboBox().setEnabled(false);

            } else {
                clearMultiSelectionPanel();

                taskId = getSelectedTaskId();

                TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);
                if (Objects.isNull(taskInfo)) {
                    throw new NotFoundException("Not found task: " + taskId);
                }

                taskPanel.getJTextFieldTask().setText(taskInfo.getName());
                taskPanel.getJTextFieldDescription().setText(taskInfo.getDescription());
                taskPanel.getRadioButtonPanel().setSelectedRadioButton(taskInfo.getPullTimeout() + " sec");

                ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(taskInfo.getConnectionId());
                if (Objects.isNull(connectionInfo)) {
                    throw new NotFoundException("Not found connection: " + taskInfo.getConnectionId());
                }
                List<ConnectionInfo> connectionAll = profileManager.getConnectionInfoList();
                List<List<?>> connectionData = new LinkedList<>();
                connectionData.add(
                        new ArrayList<>(Arrays.asList(connectionInfo.getName(), connectionInfo.getUserName(),
                                connectionInfo.getUrl(), connectionInfo.getJar(), connectionInfo.getDriver())));
                connectionAll.stream()
                        .filter(f -> f.getId() != connectionInfo.getId())
                        .forEach(conn -> connectionData.add(
                                new ArrayList<>(Arrays.asList(conn.getName(), conn.getUserName(),
                                        conn.getUrl(), conn.getJar(), conn.getDriver()))));
                taskPanel.getTaskConnectionComboBox().setTableData(connectionData);

                if (Objects.isNull(profileManager.getTaskInfoById(taskId))) {
                    throw new NotFoundException("Not found task: " + taskId);
                } else {
                    profileManager.getTaskInfoById(taskId)
                            .getQueryInfoList()
                            .forEach(queryId -> {
                                QueryInfo queryInfo = profileManager.getQueryInfoById(queryId);
                                if (Objects.isNull(queryInfo)) {
                                    throw new NotFoundException("Not found task: " + queryId);
                                }
                                multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel()
                                        .addRow(new Object[]{queryInfo.getId(),
                                                queryInfo.getName(),
                                                queryInfo.getDescription(),
                                                queryInfo.getText()});
                            });
                }


                multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel().fireTableDataChanged();

                String connName = taskPanel.getTaskConnectionComboBox().getSelectedRow().get(0).toString();
                String connDriver = taskPanel.getTaskConnectionComboBox().getSelectedRow().get(4).toString();

                log.info("Connection's " + connName + " driver: " + connDriver);
                fillAvailableQueryList(connDriver);

                fillConnectionCheckboxIsSelected(isSelected);
                fillQueryCheckboxIsSelected(isSelected);

                GUIHelper.disableButton(taskButtonPanel, !isSelected);
            }
        }
    }

    private void clearMultiSelectionPanel() {
        multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel().getDataVector().removeAllElements();
        multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel().fireTableDataChanged();
        multiSelectQueryPanel.getQueryListCase().getDefaultTableModel().getDataVector().removeAllElements();
        multiSelectQueryPanel.getQueryListCase().getDefaultTableModel().fireTableDataChanged();
        multiSelectQueryPanel.getTemplateListQueryCase().getDefaultTableModel().getDataVector().removeAllElements();
        multiSelectQueryPanel.getTemplateListQueryCase().getDefaultTableModel().fireTableDataChanged();
    }

    private void fillAvailableQueryList(String connDriver) {
        List<String> listSelectedQueryForExclude = getSelectedQueryNameList();

        // Configuration
        multiSelectQueryPanel.getQueryListCase().getDefaultTableModel().getDataVector().removeAllElements();
        multiSelectQueryPanel.getQueryListCase().getDefaultTableModel().fireTableDataChanged();

        List<String> connectionDrivers = getConnectionDriverAll();
        connectionDrivers.removeIf(q -> connDriver.contains(q));

        List<QueryInfo> queryListOfUnsuitableConnDriver = connectionDrivers.stream()
                .flatMap(driver -> profileManager.getQueryInfoListByConnDriver(driver).stream())
                .collect(Collectors.toList());


        List<QueryInfo> queryListWithoutConnDriver = profileManager.getQueryInfoList().stream()
                .filter(q -> !listSelectedQueryForExclude.contains(q.getName()))
                .filter(q -> !queryListOfUnsuitableConnDriver.contains(q))
                .collect(Collectors.toList());


        for (QueryInfo q : queryListWithoutConnDriver) {
            multiSelectQueryPanel.getQueryListCase().getDefaultTableModel()
                    .addRow(new Object[]{q.getId(), q.getName(), q.getDescription(), q.getText()});
        }

        // Template
        multiSelectQueryPanel.getTemplateListQueryCase().getDefaultTableModel().getDataVector().removeAllElements();
        multiSelectQueryPanel.getTemplateListQueryCase().getDefaultTableModel().fireTableDataChanged();

        templateManager.getQueryListByConnDriver(connDriver).stream()
                .forEach(q -> {

                    QueryInfo queryInfo = new QueryInfo();
                    queryInfo.setId(q.getId());
                    queryInfo.setName(q.getName());
                    queryInfo.setText(q.getText());
                    queryInfo.setDescription(q.getDescription());

                    multiSelectQueryPanel.getTemplateListQueryCase().getDefaultTableModel()
                            .addRow(new Object[]{queryInfo.getId(), queryInfo.getName(),
                                    queryInfo.getDescription(), queryInfo.getText()});
                });

    }

    private List<String> getSelectedQueryNameList() {
        List<String> queryListId = new ArrayList<>();

        DefaultTableModel tableModel = multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel();
        if (tableModel.getRowCount() > 0) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String selectedQueryName = tableModel.getValueAt(i, 1).toString();
                queryListId.add(selectedQueryName);
            }
        }

        return queryListId;
    }

    private List<String> getConnectionDriverAll() {
        List<String> connAllDriver = Collections.emptyList();
        List<ConnectionInfo> connectionInfoList = profileManager.getConnectionInfoList();
        connAllDriver = connectionInfoList.stream()
                .map(ConnectionInfo::getDriver)
                .collect(Collectors.toList());

        return connAllDriver.stream().distinct().collect(Collectors.toList());
    }

    private int getSelectedTaskId() {
        return GUIHelper.getIdByColumnName(taskCase.getJxTable(),
                taskCase.getDefaultTableModel(), taskCase.getJxTable().getSelectionModel(),
                TaskColumnNames.ID.getColName());
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (jTabbedPaneConfig.isEnabledAt(1)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.TASK);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            GUIHelper.disableButton(taskButtonPanel, false);
            isSelected = true;
            fillConnectionCheckboxIsSelected(true);
            fillQueryCheckboxIsSelected(true);
        } else {
            GUIHelper.disableButton(taskButtonPanel, true);
            isSelected = false;
            fillConnectionCheckboxIsSelected(false);
            fillQueryCheckboxIsSelected(false);
        }
    }

    public void fillConnectionCheckboxIsSelected(Boolean isSelected) {
        connectionCase.getDefaultTableModel().getDataVector().removeAllElements();
        connectionCase.getDefaultTableModel().fireTableDataChanged();

        if (taskCase.getDefaultTableModel().getRowCount() > 0) {
            if ( getSelectedTaskId() == 0) {
                taskCase.getJxTable().setRowSelectionInterval(0, 0);
            }
        }

        if (isSelected) {
            if (profileCase.getDefaultTableModel().getRowCount() > 0) {
               int taskId = getSelectedTaskId();

                TaskInfo task = profileManager.getTaskInfoById(taskId);
                if (Objects.isNull(task)) {
                    throw new NotFoundException("Not found task: " + taskId);
                }

                ConnectionInfo connection = profileManager.getConnectionInfoById(task.getConnectionId());
                if (Objects.isNull(connection)) {
                    throw new NotFoundException("Not found connection: " + task.getConnectionId());
                }

                connectionCase.getDefaultTableModel().addRow(new Object[]{connection.getId(), connection.getName()});
            }
        } else {
            profileManager.getConnectionInfoList()
                    .forEach(e -> connectionCase.getDefaultTableModel().addRow(new Object[]{e.getId(), e.getName()}));
        }
    }

    public void fillQueryCheckboxIsSelected(Boolean isSelected) {
        queryCase.getDefaultTableModel().getDataVector().removeAllElements();
        queryCase.getDefaultTableModel().fireTableDataChanged();

        if (isSelected) {
            if (profileCase.getDefaultTableModel().getRowCount() > 0) {
                if (Objects.isNull(profileManager.getTaskInfoById(taskId))) {
                    throw new NotFoundException("Not found task: " + taskId);
                } else {
                    profileManager.getTaskInfoById(taskId)
                            .getQueryInfoList()
                            .forEach(queryId -> {
                                QueryInfo queryIn = profileManager.getQueryInfoById(queryId);
                                if (Objects.isNull(queryIn)) {
                                    throw new NotFoundException("Not found query: " + queryId);
                                }
                                queryCase.getDefaultTableModel().addRow(new Object[]{queryIn.getId(), queryIn.getName()});
                            });
                }
            }
        } else {
            profileManager.getQueryInfoList()
                    .forEach(e -> queryCase.getDefaultTableModel().addRow(new Object[]{e.getId(), e.getName()}));
        }
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        if (!isSelected) {
            if (jTabbedPaneConfig.getSelectedIndex() == 1) {
                if (taskId == -1) {
                    clearMultiSelectionPanel();

                    multiSelectQueryPanel.getJTabbedPaneQuery().setSelectedIndex(0);

                    taskPanel.getJTextFieldTask().setEditable(false);
                    taskPanel.getJTextFieldDescription().setEditable(false);
                    taskPanel.getTaskConnectionComboBox().setEnabled(false);
                    taskPanel.getRadioButtonPanel().setButtonNotView();
                    multiSelectQueryPanel.getUnPickBtn().setEnabled(false);
                    multiSelectQueryPanel.getPickBtn().setEnabled(false);
                    multiSelectQueryPanel.getPickAllBtn().setEnabled(false);
                    multiSelectQueryPanel.getUnPickAllBtn().setEnabled(false);

                    List<ConnectionInfo> connectionAll = profileManager.getConnectionInfoList();
                    if (!connectionAll.isEmpty()) {
                        this.connectionDataList = new LinkedList<>();
                        connectionAll.forEach(connection -> connectionDataList.add(
                                new ArrayList<>(Arrays.asList(connection.getName(), connection.getUserName(),
                                        connection.getUrl(), connection.getJar(), connection.getDriver()))));

                        taskPanel.getJTextFieldTask().setText("");
                        taskPanel.getJTextFieldTask().setPrompt(bundleDefault.getString("tName"));
                        taskPanel.getJTextFieldDescription().setText("");
                        taskPanel.getJTextFieldDescription().setPrompt(bundleDefault.getString("tDesc"));
                        taskPanel.getTaskConnectionComboBox().setTableData(connectionDataList);
                        taskPanel.getTaskConnectionComboBox().setEnabled(false);

                        String connDriver = taskPanel.getTaskConnectionComboBox().getSelectedRow().get(4).toString();
                        fillAvailableQueryList(connDriver);
                    } else{
                        taskPanel.getTaskConnectionComboBox().setTableData(Collections.emptyList());
                    }
                } else {
                    clearMultiSelectionPanel();

                    TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);
                    if (Objects.isNull(taskInfo)) {
                        throw new NotFoundException("Not found task: " + taskId);
                    }

                    taskPanel.getJTextFieldTask().setText(taskInfo.getName());
                    taskPanel.getJTextFieldDescription().setText(taskInfo.getDescription());
                    taskPanel.getRadioButtonPanel().setSelectedRadioButton(taskInfo.getPullTimeout() + " sec");

                    ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(taskInfo.getConnectionId());
                    if (Objects.isNull(connectionInfo)) {
                        throw new NotFoundException("Not found connection: " + taskInfo.getConnectionId());
                    }
                    List<ConnectionInfo> connectionAll = profileManager.getConnectionInfoList();
                    List<List<?>> connectionData = new LinkedList<>();
                    connectionData.add(
                            new ArrayList<>(Arrays.asList(connectionInfo.getName(), connectionInfo.getUserName(),
                                    connectionInfo.getUrl(), connectionInfo.getJar(), connectionInfo.getDriver())));
                    connectionAll.stream()
                            .filter(f -> f.getId() != connectionInfo.getId())
                            .forEach(conn -> connectionData.add(
                                    new ArrayList<>(Arrays.asList(conn.getName(), conn.getUserName(),
                                            conn.getUrl(), conn.getJar(), conn.getDriver()))));
                    taskPanel.getTaskConnectionComboBox().setTableData(connectionData);

                    if (Objects.isNull(profileManager.getTaskInfoById(taskId))) {
                        throw new NotFoundException("Not found task: " + taskId);
                    } else {
                        profileManager.getTaskInfoById(taskId)
                                .getQueryInfoList()
                                .forEach(queryId -> {
                                    QueryInfo queryInfo = profileManager.getQueryInfoById(queryId);
                                    if (Objects.isNull(queryInfo)) {
                                        throw new NotFoundException("Not found task: " + queryId);
                                    }
                                    multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel()
                                            .addRow(new Object[]{queryInfo.getId(),
                                                    queryInfo.getName(),
                                                    queryInfo.getDescription(),
                                                    queryInfo.getText()});
                                });
                    }


                    multiSelectQueryPanel.getSelectedQueryCase().getDefaultTableModel().fireTableDataChanged();

                    String connName = taskPanel.getTaskConnectionComboBox().getSelectedRow().get(0).toString();
                    String connDriver = taskPanel.getTaskConnectionComboBox().getSelectedRow().get(4).toString();

                    log.info("Connection's " + connName + " driver: " + connDriver);
                    fillAvailableQueryList(connDriver);

                    fillConnectionCheckboxIsSelected(isSelected);
                    fillQueryCheckboxIsSelected(isSelected);

                    GUIHelper.disableButton(taskButtonPanel, !isSelected);
                }
            }
        }
    }
}