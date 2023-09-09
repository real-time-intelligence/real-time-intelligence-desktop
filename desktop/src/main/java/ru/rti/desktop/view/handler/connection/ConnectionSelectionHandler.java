package ru.rti.desktop.view.handler.connection;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.manager.TemplateManager;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.EditTabbedPane;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.handler.MouseListenerImpl;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.connection.ConnectionPanel;

@Log4j2
@Singleton
public class ConnectionSelectionHandler extends MouseListenerImpl implements ListSelectionListener, ItemListener {

    private final ProfileManager profileManager;
    private final TemplateManager templateManager;

    private final JXTableCase connectionCase;
    private final JXTableCase profileCase;
    private final JXTableCase taskCase;
    private final JXTableCase queryCase;

    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final ConnectionPanel connectionPanel;
    private final ButtonPanel connectionButtonPanel;
    private final JXTableCase connectionTemplateCase;
    private final JCheckBox checkboxConfig;
    private Boolean isSelected;
    private final ResourceBundle bundleDefault;

    @Inject
    public ConnectionSelectionHandler(@Named("profileManager") ProfileManager profileManager,
                                      @Named("templateManager") TemplateManager templateManager,
                                      @Named("connectionConfigCase") JXTableCase connectionCase,
                                      @Named("taskConfigCase") JXTableCase taskCase,
                                      @Named("queryConfigCase") JXTableCase queryCase,
                                      @Named("profileConfigCase") JXTableCase profileCase,
                                      @Named("connectionConfigPanel") ConnectionPanel connectionPanel,
                                      @Named("connectionButtonPanel") ButtonPanel connectionButtonPanel,
                                      @Named("connectionTemplateCase") JXTableCase connectionTemplateCase,
                                      @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                      @Named("checkboxConfig") JCheckBox checkboxConfig) {
        this.profileManager = profileManager;
        this.templateManager = templateManager;

        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.connectionCase = connectionCase;
        this.profileCase = profileCase;
        this.taskCase = taskCase;
        this.queryCase = queryCase;
        this.connectionCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.connectionCase.getJxTable().addMouseListener(this);
        this.connectionPanel = connectionPanel;
        this.connectionButtonPanel = connectionButtonPanel;
        this.connectionTemplateCase = connectionTemplateCase;
        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.checkboxConfig = checkboxConfig;
        this.checkboxConfig.addItemListener(this);
        this.isSelected = false;

        List<Connection> connectionListTemplate = templateManager.getConfigList(Connection.class);
        connectionListTemplate.stream()
                .filter(f -> !connectionListTemplate.contains(f.getId()))
                .forEach(connectionIn -> connectionTemplateCase.getDefaultTableModel()
                        .addRow(new Object[]{connectionIn.getId(), connectionIn.getName(),
                                connectionIn.getUserName(), connectionIn.getPassword(), connectionIn.getUrl(),
                                connectionIn.getJar(), connectionIn.getDriver()}));
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        if (jTabbedPaneConfig.isEnabledAt(2)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.CONNECTION);
        }

        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");
                connectionPanel.getJTextFieldConnectionName().setEditable(false);
                connectionPanel.getJTextFieldConnectionUserName().setEditable(false);
                connectionPanel.getJTextFieldConnectionPassword().setEditable(false);
                connectionPanel.getJTextFieldConnectionDriver().setEditable(false);
                connectionPanel.getJTextFieldConnectionJar().setEditable(false);
                connectionPanel.getJTextFieldConnectionURL().setEditable(false);
                connectionPanel.getJButtonTemplate().setEnabled(false);
                connectionPanel.getJTextFieldConnectionName().setText("");
                connectionPanel.getJTextFieldConnectionUserName().setText("");
                connectionPanel.getJTextFieldConnectionPassword().setText("");
                connectionPanel.getJTextFieldConnectionDriver().setText("");
                connectionPanel.getJTextFieldConnectionJar().setText("");
                connectionPanel.getJTextFieldConnectionURL().setText("");
                connectionPanel.getJTextFieldConnectionName().setPrompt(bundleDefault.getString("cName"));
                connectionPanel.getJTextFieldConnectionUserName().setPrompt(bundleDefault.getString("cUserName"));
                connectionPanel.getJTextFieldConnectionURL().setPrompt(bundleDefault.getString("cURL"));
                connectionPanel.getJTextFieldConnectionJar().setPrompt(bundleDefault.getString("cJar"));
                connectionPanel.getJTextFieldConnectionDriver().setPrompt(bundleDefault.getString("cDriver"));

            } else {
                int connectionID = GUIHelper.getIdByColumnName(connectionCase.getJxTable(),
                        connectionCase.getDefaultTableModel(),
                        listSelectionModel, TaskColumnNames.ID.getColName());

                ConnectionInfo selectConnection = profileManager.getConnectionInfoById(connectionID);
                if (Objects.isNull(selectConnection)) {
                    throw new NotFoundException("Not found task: " + connectionID);
                }

                connectionPanel.getJTextFieldConnectionName().setText(selectConnection.getName());
                connectionPanel.getJTextFieldConnectionURL().setText(selectConnection.getUrl());
                connectionPanel.getJTextFieldConnectionUserName().setText(selectConnection.getUserName());
                connectionPanel.getJTextFieldConnectionPassword().setText(selectConnection.getPassword());
                connectionPanel.getJTextFieldConnectionJar().setText(selectConnection.getJar());
                connectionPanel.getJTextFieldConnectionDriver().setText(selectConnection.getDriver());

                GUIHelper.disableButton(connectionButtonPanel, !isSelected);
                connectionPanel.getConnectionTemplateCase().getJxTable().setEnabled(!isSelected);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (jTabbedPaneConfig.isEnabledAt(2)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.CONNECTION);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.checkboxConfig) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                isSelected = true;
                GUIHelper.disableButton(connectionButtonPanel, false);
                connectionPanel.getConnectionTemplateCase().getJxTable().setEnabled(false);
            } else {
                isSelected = false;
                GUIHelper.disableButton(connectionButtonPanel, true);
                connectionPanel.getConnectionTemplateCase().getJxTable().setEnabled(true);
            }
        }
    }
}


