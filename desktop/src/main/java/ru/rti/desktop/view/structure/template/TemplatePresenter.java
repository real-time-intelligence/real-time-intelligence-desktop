package ru.rti.desktop.view.structure.template;

import static ru.rti.desktop.model.config.ConfigClasses.Connection;
import static ru.rti.desktop.model.config.ConfigClasses.Query;
import static ru.rti.desktop.model.config.ConfigClasses.Task;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import lombok.extern.log4j.Log4j2;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTextArea;
import ru.rti.desktop.exception.EmptyNameException;
import ru.rti.desktop.exception.EntityExistException;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ConfigurationManager;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.manager.TemplateManager;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.config.ConfigClasses;
import ru.rti.desktop.model.config.ConfigEntity;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.config.Profile;
import ru.rti.desktop.model.config.Query;
import ru.rti.desktop.model.config.Table;
import ru.rti.desktop.model.config.Task;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.TemplateAction;
import ru.rti.desktop.model.view.TemplateState;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.TemplateListener;
import ru.rti.desktop.security.EncryptDecrypt;
import ru.rti.desktop.view.panel.template.TemplateConnPanel;
import ru.rti.desktop.view.panel.template.TemplateEditPanel;
import ru.rti.desktop.view.structure.TemplateView;

@Log4j2
@Singleton
public class TemplatePresenter extends WindowAdapter
        implements TemplateListener, ListSelectionListener, ActionListener, FocusListener, CellEditorListener, KeyListener {

    private final TemplateView templateView;
    private final EventListener eventListener;
    private final ProfileManager profileManager;
    private final ConfigurationManager configurationManager;
    private final TemplateManager templateManager;
    private final EncryptDecrypt encryptDecrypt;

    private final JButton templateLoadJButton;
    private final JButton templateSaveJButton;
    private final TemplateEditPanel templateEditPanel;

    private final JXTableCase templateTaskCase;
    private final JXTableCase templateConnCase;
    private final JXTableCase templateQueryCase;

    private final TemplateConnPanel templateConnPanel;
    private final JXTableCase templateMetricsCase;

    private final JXTextArea taskDescription;
    private final JXTextArea queryDescription;
    private final JXTextArea queryText;
    private final DefaultCellEditor cellEditor;
    private List<String> arrText;


    @Inject
    public TemplatePresenter(@Named("templateView") TemplateView templateView,
                             @Named("eventListener") EventListener eventListener,
                             @Named("profileManager") ProfileManager profileManager,
                             @Named("configurationManager") ConfigurationManager configurationManager,
                             @Named("templateManager") TemplateManager templateManager,
                             @Named("encryptDecrypt") EncryptDecrypt encryptDecrypt,
                             @Named("templateLoadJButton") JButton templateLoadJButton,
                             @Named("templateSaveJButton") JButton templateSaveJButton,
                             @Named("templateEditPanel") TemplateEditPanel templateEditPanel,
                             @Named("templateTaskCase") JXTableCase templateTaskCase,
                             @Named("templateConnCase") JXTableCase templateConnCase,
                             @Named("templateQueryCase") JXTableCase templateQueryCase,
                             @Named("templateConnPanel") TemplateConnPanel templateConnPanel,
                             @Named("templateMetricsCase") JXTableCase templateMetricsCase,
                             @Named("templateTaskDescription") JXTextArea taskDescription,
                             @Named("templateQueryDescription") JXTextArea queryDescription,
                             @Named("templateQueryText") JXTextArea queryText) {
        this.templateView = templateView;
        this.eventListener = eventListener;
        this.profileManager = profileManager;
        this.configurationManager = configurationManager;
        this.templateManager = templateManager;
        this.encryptDecrypt = encryptDecrypt;

        this.templateLoadJButton = templateLoadJButton;
        this.templateSaveJButton = templateSaveJButton;
        this.templateLoadJButton.addActionListener(this);
        this.templateSaveJButton.addActionListener(this);

        this.templateEditPanel = templateEditPanel;

        this.templateTaskCase = templateTaskCase;
        this.templateConnCase = templateConnCase;
        this.templateQueryCase = templateQueryCase;

        this.templateTaskCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.templateConnCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.templateQueryCase.getJxTable().getSelectionModel().addListSelectionListener(this);

        this.templateConnPanel = templateConnPanel;
        this.templateMetricsCase = templateMetricsCase;

        this.taskDescription = taskDescription;
        this.queryDescription = queryDescription;
        this.queryText = queryText;

        this.eventListener.addTemplateStateListener(this);

        this.templateEditPanel.getProfileName().addFocusListener(this);
        this.templateEditPanel.getTaskName().addFocusListener(this);
        this.templateEditPanel.getConnName().addFocusListener(this);

        this.cellEditor = new DefaultCellEditor(new JTextField());
        cellEditor.addCellEditorListener(this);
        this.templateEditPanel.getTemplateQueryCase().getJxTable().getColumnModel().getColumn(0).setCellEditor(cellEditor);

        arrText = new ArrayList<>();

        this.templateEditPanel.getProfileName().addKeyListener(this);
        this.templateEditPanel.getTaskName().addKeyListener(this);
        this.templateEditPanel.getConnName().addKeyListener(this);
        this.templateEditPanel.getConnUserName().addKeyListener(this);
        this.templateEditPanel.getConnPassword().addKeyListener(this);
        this.templateEditPanel.getConnUrl().addKeyListener(this);

    }

    @Override
    public void fireShowTemplate(TemplateState templateState) {
        if (templateState == TemplateState.SHOW) {
            this.templateView.showTemplate();
        }
        if (templateState == TemplateState.HIDE) {
            this.templateView.hideTemplate();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        log.info("Window template closing event received");
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        // prevents double events
        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing task fields");
            } else {
                if (e.getSource() == templateTaskCase.getJxTable().getSelectionModel()) {
                    log.info("Fire on tasks..");
                    int taskId = GUIHelper.getIdByColumnName(templateTaskCase.getJxTable(), templateTaskCase.getDefaultTableModel(),
                            listSelectionModel, TaskColumnNames.ID.getColName());

                    Task task = templateManager.getConfigList(Task.class)
                            .stream()
                            .filter(f -> f.getId() == taskId)
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found task: " + taskId));

                    Connection connection = templateManager.getConfigList(Connection.class)
                            .stream()
                            .filter(f -> f.getId() == task.getConnectionId())
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found connection: " + task.getConnectionId()));

                    List<Query> queryList = templateManager.getConfigList(Query.class);

                    templateQueryCase.getDefaultTableModel().getDataVector().removeAllElements();
                    templateQueryCase.getDefaultTableModel().fireTableDataChanged();
                    task.getQueryList()
                            .forEach(query -> queryList.stream()
                                    .filter(f -> f.getId() == query)
                                    .forEach(queryIn -> {
                                        templateQueryCase.getDefaultTableModel().addRow(
                                                new Object[]{queryIn.getId(), queryIn.getName(), queryIn.getGatherDataSql().name(),
                                                        queryIn.getLoadDataMode().name()});
                                    }));

                    taskDescription.setText(task.getDescription());

                    templateConnCase.getDefaultTableModel().getDataVector().removeAllElements();
                    templateConnCase.getDefaultTableModel().fireTableDataChanged();
                    templateConnCase.getDefaultTableModel().addRow(new Object[]{connection.getId(), connection.getName()});

                    templateConnPanel.getConnectionName().setText(connection.getName());
                    templateConnPanel.getConnectionURL().setText(connection.getUrl());
                    templateConnPanel.getConnectionUserName().setText(connection.getUserName());
                    templateConnPanel.getConnectionJar().setText(connection.getJar());
                    templateConnPanel.getConnectionDriver().setText(connection.getDriver());

                    templateConnCase.getJxTable().setRowSelectionInterval(0, 0);
                    templateQueryCase.getJxTable().setRowSelectionInterval(0, 0);

                    log.info("Task ID: " + taskId);
                }

                if (e.getSource() == templateConnCase.getJxTable().getSelectionModel()) {
                    log.info("Fire on connections..");
                }
                if (e.getSource() == templateQueryCase.getJxTable().getSelectionModel()) {
                    log.info("Fire on queries..");
                    templateMetricsCase.getDefaultTableModel().getDataVector().removeAllElements();
                    templateMetricsCase.getDefaultTableModel().fireTableDataChanged();

                    int queryId = GUIHelper.getIdByColumnName(templateQueryCase.getJxTable(),
                            templateQueryCase.getDefaultTableModel(), listSelectionModel, QueryColumnNames.ID.getColName());

                    Query query = templateManager.getConfigList(Query.class)
                            .stream()
                            .filter(f -> f.getId() == queryId)
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found query: " + queryId));

                    for (Metric m : query.getMetricList()) {
                        templateMetricsCase.getDefaultTableModel()
                                .addRow(new Object[]{m.getId(),
                                        m.getName(),
                                        m.getIsDefault(),
                                        m.getXAxis().getColName(),
                                        m.getYAxis().getColName(),
                                        m.getGroup().getColName(),
                                        m.getMetricFunction().toString(),
                                        m.getChartType().toString()});
                    }

                    queryDescription.setText(query.getDescription());
                    queryText.setText(query.getText());
                }
            }
        }
    }

    public <T> void fillModel(Class<T> clazz) {
        if (Task.equals(ConfigClasses.fromClass(clazz))) {
            log.info("Task..");

            templateManager.getConfigList(Task.class)
                    .forEach(e -> templateTaskCase.getDefaultTableModel()
                            .addRow(new Object[]{e.getId(), e.getName(), e.getPullTimeout() + " sec."}));

            templateTaskCase.getJxTable().setRowSelectionInterval(0, 0);
        }

        if (Connection.equals(ConfigClasses.fromClass(clazz))) {
            log.info("Connection..");
        }

        if (Query.equals(ConfigClasses.fromClass(clazz))) {
            log.info("Query..");
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int taskId = GUIHelper.getIdByColumnName(templateTaskCase.getJxTable(),
                templateTaskCase.getDefaultTableModel(), templateTaskCase.getJxTable().getSelectionModel(),
                QueryColumnNames.ID.getColName());
        Task task = templateManager.getConfigList(Task.class)
                .stream()
                .filter(f -> f.getId() == taskId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("Not found task: " + taskId));

        int connId = GUIHelper.getIdByColumnName(templateConnCase.getJxTable(),
                templateConnCase.getDefaultTableModel(), templateConnCase.getJxTable().getSelectionModel(),
                QueryColumnNames.ID.getColName());
        Connection connection = templateManager.getConfigList(Connection.class)
                .stream()
                .filter(f -> f.getId() == connId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("Not found connection: " + connId));

        int queryId = GUIHelper.getIdByColumnName(templateQueryCase.getJxTable(),
                templateQueryCase.getDefaultTableModel(), templateQueryCase.getJxTable().getSelectionModel(),
                QueryColumnNames.ID.getColName());
        Query query = templateManager.getConfigList(Query.class)
                .stream()
                .filter(f -> f.getId() == queryId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("Not found query: " + queryId));

        if (e.getActionCommand().equals(TemplateAction.LOAD.name())) {

            templateEditPanel.getProfileName().setText("");
            templateEditPanel.getProfileDesc().setText("");

            templateEditPanel.getTaskName().setText(task.getName());
            templateEditPanel.getTaskDesc().setText(task.getDescription());

            templateEditPanel.getConnName().setText(connection.getName());
            templateEditPanel.getConnUserName().setText(connection.getUserName());
            templateEditPanel.getConnUrl().setText(connection.getUrl());
            templateEditPanel.getConnJar().setText(connection.getJar());

            templateEditPanel.getQueryName().setText(query.getName());
            templateEditPanel.getQueryDesc().setText(query.getDescription());
            templateEditPanel.getStatusQuery().setText("Query already exist");

            List<Query> queryList = templateManager.getConfigList(Query.class)
                    .stream()
                    .filter(f -> task.getQueryList().stream().anyMatch(q -> q == f.getId()))
                    .toList();
            templateEditPanel.updateModelTemplateEditQueryCase(queryList);

            // Check profile name
            changeStatusIfEntityExist(Profile.class, templateEditPanel.getProfileName().getText());

            // Check task name
            changeStatusIfEntityExist(Task.class, templateEditPanel.getTaskName().getText());

            // Check connection name
            changeStatusIfEntityExist(Connection.class, templateEditPanel.getConnName().getText());

            // Check query name
            DefaultTableModel defaultTableModel = templateEditPanel.getTemplateQueryCase().getDefaultTableModel();

            for (int rowIndex = 0; rowIndex < defaultTableModel.getRowCount(); rowIndex++) {
                String queryName = (String) defaultTableModel.getValueAt(rowIndex, 1);
                changeStatusIfEntityExist(Query.class, queryName);
            }
            templateEditPanel.setVisible(true);
        } else if (e.getActionCommand().equals(TemplateAction.SAVE.name())) {

            if (!templateEditPanel.getProfileName().getText().trim().isEmpty() &&
                    !templateEditPanel.getTaskName().getText().trim().isEmpty() &&
                    !templateEditPanel.getConnName().getText().trim().isEmpty()) {
                // Check profile name
                raiseAnErrorIfEntityExist(Profile.class, templateEditPanel.getProfileName().getText());

                // Check task name
                raiseAnErrorIfEntityExist(Task.class, templateEditPanel.getTaskName().getText());

                // Check connection name
                raiseAnErrorIfEntityExist(Connection.class, templateEditPanel.getConnName().getText());

                // Check query name
                DefaultTableModel defaultTableModel = templateEditPanel.getTemplateQueryCase().getDefaultTableModel();

                for (int rowIndex = 0; rowIndex < defaultTableModel.getRowCount(); rowIndex++) {
                    String queryName = (String) defaultTableModel.getValueAt(rowIndex, 1);
                    raiseAnErrorIfEntityExist(Query.class, queryName);
                }

                // Save query and table
                List<Integer> quiryIdList = new ArrayList<>();

                int queryMaxId = configurationManager.getConfigList(Query.class)
                        .stream()
                        .map(ru.rti.desktop.model.config.Query::getId)
                        .reduce(Integer::max)
                        .orElse(0) + 1;

                for (int rowIndex = 0; rowIndex < defaultTableModel.getRowCount(); rowIndex++) {
                    Integer queryTemplateId = (Integer) defaultTableModel.getValueAt(rowIndex, 0);
                    String queryTemplateName = (String) defaultTableModel.getValueAt(rowIndex, 1);
                    String queryTemplateDesc = (String) defaultTableModel.getValueAt(rowIndex, 2);

                    Query saveQuery = templateManager.getConfigList(Query.class)
                            .stream()
                            .filter(f -> f.getId() == queryTemplateId)
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found query template by queryId: " + queryTemplateId));

                    saveQuery.setId(queryMaxId);
                    saveQuery.setName(queryTemplateName);
                    saveQuery.setDescription(queryTemplateDesc);

                    configurationManager.addConfig(saveQuery, Query.class);

                    // Save table
                    Table table = new Table();
                    table.setTableName(saveQuery.getName());
                    configurationManager.addConfig(table, Table.class);

                    quiryIdList.add(queryMaxId);

                    queryMaxId++;
                }

                // Save connection
                int connMaxId = configurationManager.getConfigList(Connection.class)
                        .stream()
                        .map(ru.rti.desktop.model.config.Connection::getId)
                        .reduce(Integer::max)
                        .orElse(0) + 1;

                connection.setId(connMaxId);
                connection.setName(templateEditPanel.getConnName().getText());
                connection.setUrl(templateEditPanel.getConnUrl().getText());
                connection.setJar(templateEditPanel.getConnJar().getText());
                connection.setUserName(templateEditPanel.getConnUserName().getText());
                connection.setPassword(encryptDecrypt.encrypt(String.valueOf(templateEditPanel.getConnPassword().getPassword())));

                configurationManager.addConfig(connection, Connection.class);

                // Save task
                int taskMaxId = configurationManager.getConfigList(Task.class)
                        .stream()
                        .map(ru.rti.desktop.model.config.Task::getId)
                        .reduce(Integer::max)
                        .orElse(0) + 1;

                task.setId(taskMaxId);
                task.setName(templateEditPanel.getTaskName().getText());
                task.setDescription(templateEditPanel.getTaskDesc().getText());
                task.setConnectionId(connMaxId);
                task.setQueryList(quiryIdList);

                configurationManager.addConfig(task, Task.class);

                // Save profile
                int profileMaxId = configurationManager.getConfigList(Profile.class)
                        .stream()
                        .map(ru.rti.desktop.model.config.Profile::getId)
                        .reduce(Integer::max)
                        .orElse(0) + 1;

                Profile profile = new Profile();
                profile.setId(profileMaxId);
                profile.setName(templateEditPanel.getProfileName().getText());
                profile.setDescription(templateEditPanel.getProfileDesc().getText());
                profile.setTaskList(Collections.singletonList(taskMaxId));

                configurationManager.addConfig(profile, Profile.class);

                profileManager.updateCache();

                eventListener.fireProfileAdd();

                templateEditPanel.setVisible(false);
            } else {
                throw new EmptyNameException("The name field is empty");
            }

        }
    }

    private <T> void raiseAnErrorIfEntityExist(Class<? extends ConfigEntity> clazz, String entityName) {
        // Check profile name
        configurationManager.getConfigList(clazz)
                .stream()
                .filter(f -> f.getName().equalsIgnoreCase(entityName))
                .findAny()
                .ifPresentOrElse(profile -> {
                    throw new EntityExistException("Entity " + clazz.getSimpleName() + " with name " + entityName
                            + " already exist. Choose another one..");
                }, () -> {
                });
    }

    private <T> void changeStatusIfEntityExist(Class<? extends ConfigEntity> clazz, String entityName) {
        // Check profile name
        configurationManager.getConfigList(clazz)
                .stream()
                .filter(f -> f.getName().trim().equalsIgnoreCase(entityName.trim()))
                .findAny()
                .ifPresentOrElse(profile -> {
                    viewStatus(clazz, entityName);
                    templateEditPanel.getTemplateSaveJButton().setEnabled(false);
                }, () -> {
                    hideStatus(clazz, entityName);
                });
    }

    private void viewStatus(Class<? extends ConfigEntity> clazz, String entityName) {
        if (clazz.getSimpleName().equalsIgnoreCase("Task")) {
            templateEditPanel.getStatusTask().setText("Task " + entityName.trim() + " already exist");
            templateEditPanel.getStatusTask().setVisible(true);

        } else if (clazz.getSimpleName().equalsIgnoreCase("Connection")) {
            templateEditPanel.getStatusConn().setText("Connection " + entityName.trim() + " already exist");
            templateEditPanel.getStatusConn().setVisible(true);

        } else if (clazz.getSimpleName().equalsIgnoreCase("Query")) {

            templateEditPanel.getStatusQuery().setVisible(true);

            if (arrText.size() == 0) {
                arrText.add("Query already exist : ");
                arrText.add("\n" + entityName);
            } else {
                arrText.add("\n" + entityName);
            }
            String text = "";
            for (String arr : arrText) {
                text = text + arr;
            }
            templateEditPanel.getStatusQuery().setText(text);

        }
    }

    private void hideStatus(Class<? extends ConfigEntity> clazz, String entityName) {
        if (clazz.getSimpleName().equalsIgnoreCase("Task")) {
            templateEditPanel.getStatusTask().setVisible(false);
        } else if (clazz.getSimpleName().equalsIgnoreCase("Connection")) {
            templateEditPanel.getStatusConn().setVisible(false);
        } else if (clazz.getSimpleName().equalsIgnoreCase("Query")) {
            if (arrText.size() <= 1) {
                templateEditPanel.getStatusQuery().setVisible(false);
            }
        }

        if (templateEditPanel.isVisible()
                && !templateEditPanel.getStatusTask().isVisible()
                && !templateEditPanel.getStatusConn().isVisible()
                && !templateEditPanel.getStatusQuery().isVisible()) {
            templateEditPanel.getTemplateSaveJButton().setEnabled(true);
        }

    }


    @Override
    public void focusGained(FocusEvent focusEvent) {
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {
        changeStatusIfEntityExist(Profile.class, templateEditPanel.getProfileName().getText());
        changeStatusIfEntityExist(Task.class, templateEditPanel.getTaskName().getText());
        changeStatusIfEntityExist(Connection.class, templateEditPanel.getConnName().getText());
    }


    @Override
    public void editingStopped(ChangeEvent e) {
        // завершение редактирования ячейки
        DefaultTableModel defaultTableModel = templateEditPanel.getTemplateQueryCase().getDefaultTableModel();
        arrText.clear();
        for (int rowIndex = 0; rowIndex < defaultTableModel.getRowCount(); rowIndex++) {
            String queryName = (String) defaultTableModel.getValueAt(rowIndex, 1);
            changeStatusIfEntityExist(Query.class, queryName);
        }

    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        // отмена редактирования ячейки
    }


    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (templateEditPanel.getProfileName().hasFocus()) {
                templateEditPanel.getProfileDesc().requestFocus();
            }
            if (templateEditPanel.getTaskName().hasFocus()) {
                templateEditPanel.getTaskDesc().requestFocus();
            }
            if (templateEditPanel.getConnName().hasFocus()) {
                templateEditPanel.getConnUserName().requestFocus();
            }
            if (templateEditPanel.getConnUserName().hasFocus()) {
                templateEditPanel.getConnPassword().requestFocus();
            }
            if (templateEditPanel.getConnPassword().hasFocus()) {
                templateEditPanel.getConnUrl().requestFocus();
            }
            if (templateEditPanel.getConnUrl().hasFocus()) {
                templateEditPanel.getConnJar().requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
