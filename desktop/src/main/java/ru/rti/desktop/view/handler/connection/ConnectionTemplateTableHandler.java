package ru.rti.desktop.view.handler.connection;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.manager.TemplateManager;
import ru.rti.desktop.model.column.ConnectionColumnNames;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.connection.ConnectionPanel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Singleton
public class ConnectionTemplateTableHandler implements ListSelectionListener, ActionListener {

    private final JXTableCase profileCase;
    private final JXTableCase taskCase;
    private final JXTableCase connectionCase;
    private final JXTableCase queryCase;
    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final JXTableCase connectionTemplateCase;
    private final ConnectionPanel connectionPanel;
    private final JCheckBox checkboxConfig;
    private final TemplateManager templateManager;
    private final ProfileManager profileManager;
    private int connectionID;

    @Inject
    public ConnectionTemplateTableHandler(@Named("connectionConfigPanel") ConnectionPanel connectionPanel,
                                          @Named("profileConfigCase") JXTableCase profileCase,
                                          @Named("taskConfigCase") JXTableCase taskCase,
                                          @Named("connectionConfigCase") JXTableCase connectionCase,
                                          @Named("queryConfigCase") JXTableCase queryCase,
                                          @Named("connectionTemplateCase") JXTableCase connectionTemplateCase,
                                          @Named("templateManager") TemplateManager templateManager,
                                          @Named("profileManager") ProfileManager profileManager,
                                          @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                          @Named("checkboxConfig") JCheckBox checkboxConfig) {

        this.connectionPanel = connectionPanel;

        this.profileCase = profileCase;
        this.taskCase = taskCase;
        this.connectionCase = connectionCase;
        this.queryCase = queryCase;
        this.connectionTemplateCase = connectionTemplateCase;
        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.connectionID = 0;
        this.checkboxConfig = checkboxConfig;

        this.templateManager = templateManager;
        this.profileManager = profileManager;


        this.connectionTemplateCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.connectionPanel.getJButtonTemplate().addActionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting()) {


            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");
                connectionPanel.getJButtonTemplate().setEnabled(false);
            } else {
                connectionPanel.getJTextFieldConnectionName().setEditable(false);
                connectionPanel.getJTextFieldConnectionUserName().setEditable(false);
                connectionPanel.getJTextFieldConnectionPassword().setEditable(false);
                connectionPanel.getJTextFieldConnectionDriver().setEditable(false);
                connectionPanel.getJTextFieldConnectionJar().setEditable(false);
                connectionPanel.getJTextFieldConnectionURL().setEditable(false);
                connectionPanel.getJButtonTemplate().setEnabled(true);
                connectionPanel.getConnectionButtonPanel().getBtnNew().setEnabled(false);
                connectionPanel.getConnectionButtonPanel().getBtnCopy().setEnabled(false);
                connectionPanel.getConnectionButtonPanel().getBtnDel().setEnabled(false);
                connectionPanel.getConnectionButtonPanel().getBtnEdit().setEnabled(false);
                connectionPanel.getConnectionButtonPanel().getBtnCancel().setEnabled(true);
                checkboxConfig.setEnabled(false);
                jTabbedPaneConfig.setEnabledAt(1, false);
                jTabbedPaneConfig.setEnabledAt(0, false);
                jTabbedPaneConfig.setEnabledAt(3, false);
                profileCase.getJxTable().setEnabled(false);
                taskCase.getJxTable().setEnabled(false);
                connectionCase.getJxTable().setEnabled(false);
                queryCase.getJxTable().setEnabled(false);
                this.connectionID = GUIHelper.getIdByColumnName(connectionTemplateCase.getJxTable(),
                        connectionTemplateCase.getDefaultTableModel(), listSelectionModel, ConnectionColumnNames.ID.getColName());
                List<Connection> selectConnection = templateManager.getConfigList(Connection.class).stream()
                        .filter(s -> s.getId() == connectionID).toList();
                connectionPanel.getJTextFieldConnectionName().setText(selectConnection.get(0).getName());
                connectionPanel.getJTextFieldConnectionURL().setText(selectConnection.get(0).getUrl());
                connectionPanel.getJTextFieldConnectionUserName().setText(selectConnection.get(0).getUserName());
                connectionPanel.getJTextFieldConnectionPassword().setText(selectConnection.get(0).getPassword());
                connectionPanel.getJTextFieldConnectionJar().setText(selectConnection.get(0).getJar());
                connectionPanel.getJTextFieldConnectionDriver().setText(selectConnection.get(0).getDriver());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectionPanel.getJButtonTemplate()) {

            AtomicInteger connectionIdNext = new AtomicInteger();

            profileManager.getConnectionInfoList().stream()
                    .max(Comparator.comparing(ConnectionInfo::getId))
                    .ifPresentOrElse(connection -> connectionIdNext.set(connection.getId()),
                            () -> {
                                log.info("Not found Connection");
                                connectionIdNext.set(0);
                            });

            ConnectionInfo copyConnection = new ConnectionInfo();

            if (isBusyName(connectionPanel.getJTextFieldConnectionName().getText())) {

                List<Connection> selectConnection = templateManager.getConfigList(Connection.class).stream()
                        .filter(s -> s.getId() == connectionID).toList();

                copyConnection.setId(connectionIdNext.incrementAndGet());
                copyConnection.setName(selectConnection.get(0).getName());
                copyConnection.setUserName(selectConnection.get(0).getUserName());
                copyConnection.setUrl(selectConnection.get(0).getUrl());
                copyConnection.setJar(selectConnection.get(0).getJar());
                copyConnection.setDriver(selectConnection.get(0).getDriver());
                copyConnection.setPassword(selectConnection.get(0).getPassword());

                profileManager.addConnection(copyConnection);

                connectionCase.getDefaultTableModel().getDataVector().removeAllElements();
                connectionCase.getDefaultTableModel().fireTableDataChanged();

                int selection = 0;
                int index = 0;
                for (ConnectionInfo connection : profileManager.getConnectionInfoList()) {
                    connectionCase.getDefaultTableModel()
                            .addRow(new Object[]{connection.getId(), connection.getName()});

                    if (connection.getId() == copyConnection.getId()) {
                        index++;
                        selection = index;
                    }
                    index++;
                }

                connectionCase.getJxTable().setRowSelectionInterval(selection - 1, selection - 1);
                connectionTemplateCase.getJxTable().clearSelection();
                connectionPanel.getJButtonTemplate().setEnabled(false);
                connectionPanel.getConnectionButtonPanel().setButtonView(true);
                checkboxConfig.setEnabled(true);
                jTabbedPaneConfig.setEnabledAt(1, true);
                jTabbedPaneConfig.setEnabledAt(0, true);
                jTabbedPaneConfig.setEnabledAt(3, true);
                profileCase.getJxTable().setEnabled(true);
                taskCase.getJxTable().setEnabled(true);
                connectionCase.getJxTable().setEnabled(true);
                queryCase.getJxTable().setEnabled(true);
            }
        }
    }


    public boolean isBusyName(String newName) {
        boolean isBusy = true;
        List<ConnectionInfo> connectionList = profileManager.getConnectionInfoList();
        for (ConnectionInfo connection : connectionList) {
            if ((connection.getName().equals(newName))) {
                isBusy = false;
                JOptionPane.showMessageDialog(null, "Name " + newName
                                + " already exists, please enter another one.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                connectionPanel.getJButtonTemplate().setEnabled(false);
                connectionPanel.getConnectionTemplateCase().getJxTable().clearSelection();
                connectionPanel.getConnectionButtonPanel().setButtonView(true);
                checkboxConfig.setEnabled(true);
                jTabbedPaneConfig.setEnabledAt(1, true);
                jTabbedPaneConfig.setEnabledAt(0, true);
                jTabbedPaneConfig.setEnabledAt(3, true);
                profileCase.getJxTable().setEnabled(true);
                taskCase.getJxTable().setEnabled(true);
                connectionCase.getJxTable().setEnabled(true);
                queryCase.getJxTable().setEnabled(true);
                break;
            }
        }
        return isBusy;
    }
}
