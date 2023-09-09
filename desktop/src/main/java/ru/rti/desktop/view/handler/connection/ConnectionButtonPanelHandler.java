package ru.rti.desktop.view.handler.connection;

import static ru.rti.desktop.model.view.handler.LifeCycleStatus.COPY;
import static ru.rti.desktop.model.view.handler.LifeCycleStatus.EDIT;
import static ru.rti.desktop.model.view.handler.LifeCycleStatus.NEW;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.exception.EmptyNameException;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.exception.NotSelectedRowException;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.handler.LifeCycleStatus;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.security.EncryptDecrypt;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.connection.ConnectionPanel;

@Log4j2
@Singleton
public class ConnectionButtonPanelHandler implements ActionListener {

    private final ProfileManager profileManager;
    private final EventListener eventListener;
    private final JXTableCase profileCase;
    private final JXTableCase taskCase;
    private final JXTableCase connectionCase;
    private final JXTableCase queryCase;
    private final JXTableCase connectionTemplateCase;
    private final ConnectionPanel connectionPanel;
    private final ButtonPanel connectionButtonPanel;
    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final JCheckBox checkboxConfig;
    private final EncryptDecrypt encryptDecrypt;
    private LifeCycleStatus status;
    private boolean isPasswordChanged = false;
    private ConnectionInfo oldFileConnection;

    private final JFileChooser jarFC;
    private JFrame jFrame;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ResourceBundle bundleDefault;

    @Inject
    public ConnectionButtonPanelHandler(@Named("profileManager") ProfileManager profileManager,
                                        @Named("eventListener") EventListener eventListener,
                                        @Named("encryptDecrypt") EncryptDecrypt encryptDecrypt,
                                        @Named("profileConfigCase") JXTableCase profileCase,
                                        @Named("taskConfigCase") JXTableCase taskCase,
                                        @Named("connectionConfigCase") JXTableCase connectionCase,
                                        @Named("connectionTemplateCase") JXTableCase connectionTemplateCase,
                                        @Named("queryConfigCase") JXTableCase queryCase,
                                        @Named("connectionConfigPanel") ConnectionPanel connectionPanel,
                                        @Named("connectionButtonPanel") ButtonPanel connectionButtonPanel,
                                        @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                        @Named("checkboxConfig") JCheckBox checkboxConfig) {

        this.profileManager = profileManager;
        this.encryptDecrypt = encryptDecrypt;

        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.profileCase = profileCase;
        this.taskCase = taskCase;
        this.connectionCase = connectionCase;
        this.connectionTemplateCase = connectionTemplateCase;
        this.queryCase = queryCase;
        this.connectionPanel = connectionPanel;
        this.connectionButtonPanel = connectionButtonPanel;
        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.checkboxConfig = checkboxConfig;
        this.jarFC = new JFileChooser();

        this.connectionButtonPanel.getBtnNew().addActionListener(this);
        this.connectionButtonPanel.getBtnCopy().addActionListener(this);
        this.connectionButtonPanel.getBtnDel().addActionListener(this);
        this.connectionButtonPanel.getBtnEdit().addActionListener(this);
        this.connectionButtonPanel.getBtnSave().addActionListener(this);
        this.connectionButtonPanel.getBtnCancel().addActionListener(this);

        this.connectionPanel.getJarButton().addActionListener(e -> executor.submit(() -> {
            connectionPanel.getJarButton().setEnabled(false);
            connectionPanel.getJTextFieldConnectionJar().requestFocus();

            int returnVal = jarFC.showOpenDialog(jFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = jarFC.getSelectedFile();
                connectionPanel.getJTextFieldConnectionJar().setText(file.getAbsolutePath());
            }

            this.connectionPanel.getJarButton().setVisible(true);
            connectionPanel.getJarButton().setEnabled(true);
        }));

        this.connectionButtonPanel.getBtnDel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        this.connectionButtonPanel.getBtnDel().getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionButtonPanel.getBtnDel().doClick();
            }
        });

        this.connectionButtonPanel.getBtnCancel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        this.connectionButtonPanel.getBtnCancel().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionButtonPanel.getBtnCancel().doClick();
            }
        });

        this.status = LifeCycleStatus.NONE;

        this.connectionPanel.getJTextFieldConnectionPassword()
                .getDocument().addDocumentListener((PasswordDocumentListener) e -> {
                    if (this.connectionPanel.getJTextFieldConnectionPassword().isEditable()) {
                        isPasswordChanged = true;
                    }
                });

        this.eventListener = eventListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String oldGuiPass = "";
        if (isPasswordChanged) {
            oldGuiPass = String.valueOf(connectionPanel.getJTextFieldConnectionPassword().getPassword());
        } else {

            try {
                oldGuiPass = encryptDecrypt
                    .decrypt(String.valueOf(connectionPanel.getJTextFieldConnectionPassword().getPassword()));
            } catch (Exception exception) {
                isPasswordChanged = true;
                connectionPanel.getJTextFieldConnectionPassword().setText("");

                int connectionId = getSelectedConnectionId();
                oldFileConnection = profileManager.getConnectionInfoById(connectionId);
                oldFileConnection.setPassword("");
                profileManager.updateConnection(oldFileConnection);
                profileManager.updateCache();

                JOptionPane.showMessageDialog(connectionPanel,
                    "Need to set new password. Perhaps the configuration was copied from another computer",
                    "Password decryption issue", JOptionPane.WARNING_MESSAGE);
            }

        }

        if (e.getSource() == connectionButtonPanel.getBtnNew()) {
            status = NEW;

            newEmptyPanel();
            setPanelView(false);

        } else if (e.getSource() == connectionButtonPanel.getBtnCopy()) {
            status = COPY;

            if (connectionCase.getJxTable().getSelectedRow() == -1) {
                throw new NotSelectedRowException("The connection to copy is not selected. Please select and try again!");
            } else {
                int connectionId = getSelectedConnectionId();
                ConnectionInfo connection = profileManager.getConnectionInfoById(connectionId);
                if (Objects.isNull(connection)) {
                    throw new NotFoundException("Not found task: " + connectionId);
                }

                setPanelView(false);

                connectionPanel.getJTextFieldConnectionName().setText(connection.getName() + "_copy");
                connectionPanel.getJTextFieldConnectionURL().setText(connection.getUrl() + "_copy");
                connectionPanel.getJTextFieldConnectionUserName().setText(connection.getUserName() + "_copy");
                connectionPanel.getJTextFieldConnectionPassword().setText("");
                connectionPanel.getJTextFieldConnectionJar().setText(connection.getJar() + "_copy");
                connectionPanel.getJTextFieldConnectionDriver().setText(connection.getDriver() + "_copy");

            }

        } else if (e.getSource() == connectionButtonPanel.getBtnDel()) {
            if (connectionCase.getJxTable().getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(null, "Not selected connection. Please select and try again!",
                        "General Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int connectionId = getSelectedConnectionId();
                int input = JOptionPane.showConfirmDialog(new JDialog(),// 0=yes, 1=no, 2=cancel
                        "Do you want to delete configuration: " + connectionCase.getDefaultTableModel()
                                .getValueAt(connectionCase.getJxTable().getSelectedRow(), 1) + "?");
                if (input == 0) {
                    if (isUsedOnTask(connectionId)) {
                        ConnectionInfo connection = profileManager.getConnectionInfoById(connectionId);
                        if (Objects.isNull(connection)) {
                            throw new NotFoundException("Not found task: " + connectionId);
                        }
                        profileManager.deleteConnection(connection.getId(), connection.getName());

                        clearConnectionCase();

                        profileManager.getConnectionInfoList().forEach(connectionInfo -> connectionCase.getDefaultTableModel()
                                .addRow(new Object[]{connectionInfo.getId(), connectionInfo.getName()}));

                        if (connectionCase.getJxTable().getSelectedRow() > 0) {
                            connectionCase.getJxTable().setRowSelectionInterval(0, 0);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Cannot delete this connection it is used in the task",
                                "General Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else if (e.getSource() == connectionButtonPanel.getBtnEdit()) {
            if (connectionCase.getJxTable().getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(null, "Not selected connection. Please select and try again!",
                        "General Error", JOptionPane.ERROR_MESSAGE);
            } else {
                status = EDIT;
                int connectionId = getSelectedConnectionId();
                oldFileConnection = profileManager.getConnectionInfoById(connectionId);
                if (Objects.isNull(oldFileConnection)) {
                    throw new NotFoundException("Not found task: " + connectionId);
                }
                setPanelView(false);
            }
        } else if (e.getSource() == connectionButtonPanel.getBtnSave()) {

            if (NEW.equals(status) || COPY.equals(status)) {

                AtomicInteger connectionIdNext = new AtomicInteger();

                profileManager.getConnectionInfoList().stream()
                        .max(Comparator.comparing(ConnectionInfo::getId))
                        .ifPresentOrElse(connection -> connectionIdNext.set(connection.getId()),
                                () -> {
                                    log.info("Not found Connection");
                                    connectionIdNext.set(0);
                                });


                if (!connectionPanel.getJTextFieldConnectionName().getText().trim().isEmpty()) {

                    int connectionId = connectionIdNext.incrementAndGet();
                    String newConnectionName = connectionPanel.getJTextFieldConnectionName().getText();
                    checkConnectionNameIsBusy(connectionId, newConnectionName);

                    ConnectionInfo saveConnection = new ConnectionInfo();
                    saveConnection.setId(connectionIdNext.incrementAndGet());
                    saveConnection.setName(connectionPanel.getJTextFieldConnectionName().getText());
                    saveConnection.setUserName(connectionPanel.getJTextFieldConnectionUserName().getText());
                    saveConnection.setUrl(connectionPanel.getJTextFieldConnectionURL().getText());
                    saveConnection.setJar(connectionPanel.getJTextFieldConnectionJar().getText());
                    saveConnection.setDriver(connectionPanel.getJTextFieldConnectionDriver().getText());

                    saveConnection.setPassword(encryptDecrypt.encrypt(
                            String.valueOf(connectionPanel.getJTextFieldConnectionPassword().getPassword())));

                    isPasswordChanged = false;

                    profileManager.addConnection(saveConnection);

                    clearConnectionCase();

                    int selection = 0;
                    int index = 0;
                    for (ConnectionInfo connection : profileManager.getConnectionInfoList()) {
                        connectionCase.getDefaultTableModel()
                                .addRow(new Object[]{connection.getId(), connection.getName()});

                        if (connection.getId() == saveConnection.getId()) {
                            index++;
                            selection = index;
                        }
                        index++;
                    }

                    setPanelView(true);
                    connectionCase.getJxTable().setRowSelectionInterval(selection - 1, selection - 1);

                } else {
                    throw new EmptyNameException("The name field is empty");
                }

            } else if (EDIT.equals(status)) {

                int selectedIndex = connectionCase.getJxTable().getSelectedRow();
                int connectionId = getSelectedConnectionId();

                if (!connectionPanel.getJTextFieldConnectionName().getText().trim().isEmpty()) {
                    String newConnectionName = connectionPanel.getJTextFieldConnectionName().getText();
                    checkConnectionNameIsBusy(connectionId, newConnectionName);
                    ConnectionInfo oldConnection = profileManager.getConnectionInfoById(connectionId);

                    ConnectionInfo editConnection = new ConnectionInfo();
                    editConnection.setId(connectionId);
                    editConnection.setName(connectionPanel.getJTextFieldConnectionName().getText());
                    editConnection.setUserName(connectionPanel.getJTextFieldConnectionUserName().getText());
                    editConnection.setUrl(connectionPanel.getJTextFieldConnectionURL().getText());

                    setEditPassword(oldGuiPass, editConnection, encryptDecrypt.decrypt(oldFileConnection.getPassword()));

                    editConnection.setJar(connectionPanel.getJTextFieldConnectionJar().getText());
                    editConnection.setDriver(connectionPanel.getJTextFieldConnectionDriver().getText());

                    if (!oldConnection.getName().equals(newConnectionName)) {
                        deleteConnectionById(connectionId);
                        profileManager.addConnection(editConnection);
                    } else {
                        profileManager.updateConnection(editConnection);
                    }

                    clearConnectionCase();

                    profileManager.getConnectionInfoList().forEach(connectionInfo -> connectionCase.getDefaultTableModel()
                            .addRow(new Object[]{connectionInfo.getId(), connectionInfo.getName()}));

                    setPanelView(true);
                    connectionCase.getJxTable().setRowSelectionInterval(selectedIndex, selectedIndex);

                } else {
                    throw new EmptyNameException("The name field is empty");
                }
            }
        } else if (e.getSource() == connectionButtonPanel.getBtnCancel()) {

            if (!connectionPanel.getJButtonTemplate().isEnabled()) {
                if (connectionCase.getJxTable().getSelectedRowCount() > 0) {
                    int selectedIndex = connectionCase.getJxTable().getSelectedRow();
                    connectionCase.getJxTable().setRowSelectionInterval(0, 0);
                    setPanelView(true);
                    connectionCase.getJxTable().setRowSelectionInterval(selectedIndex, selectedIndex);

                    int connectionId = getSelectedConnectionId();
                    ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(connectionId);
                    if (Objects.isNull(connectionInfo)) {
                        throw new NotFoundException("Not found task: " + connectionId);
                    }
                    connectionPanel.getJTextFieldConnectionName().setText(connectionInfo.getName());
                    connectionPanel.getJTextFieldConnectionUserName().setText(connectionInfo.getUserName());
                    connectionPanel.getJTextFieldConnectionURL().setText(connectionInfo.getUrl());
                    connectionPanel.getJTextFieldConnectionPassword().setText(connectionInfo.getPassword());
                    connectionPanel.getJTextFieldConnectionJar().setText(connectionInfo.getJar());
                    connectionPanel.getJTextFieldConnectionDriver().setText(connectionInfo.getDriver());
                } else {
                    setPanelView(true);
                    newEmptyPanel();

                }
            } else {
                connectionPanel.getJButtonTemplate().setEnabled(false);
                connectionPanel.getConnectionTemplateCase().getJxTable().clearSelection();
                int selectedIndex = connectionCase.getJxTable().getSelectedRow();

                if (connectionCase.getJxTable().getSelectedRowCount() > 0) {
                    connectionCase.getJxTable().clearSelection();
                    setPanelView(true);
                    connectionCase.getJxTable().setRowSelectionInterval(selectedIndex, selectedIndex);
                } else {
                    setPanelView(true);
                    newEmptyPanel();

                }
            }
        }
    }

    private void setEditPassword(String oldGuiPass, ConnectionInfo editConnection, String oldFilePass) {
        if (isPasswordChanged) {
            if (!oldGuiPass.equals(oldFilePass)) {
                editConnection.setPassword(encryptDecrypt.encrypt(
                        String.valueOf(connectionPanel.getJTextFieldConnectionPassword().getPassword())));
            } else {
                editConnection.setPassword(encryptDecrypt.encrypt(oldFilePass));
            }
        } else {
            editConnection.setPassword(
                    String.valueOf(connectionPanel.getJTextFieldConnectionPassword().getPassword()));
        }

        isPasswordChanged = false;
    }

    public void checkConnectionNameIsBusy(int id, String newConnectionName) {
        List<ConnectionInfo> connectionList = profileManager.getConnectionInfoList();
        for (ConnectionInfo connection : connectionList) {
            if (connection.getName().equals(newConnectionName) && connection.getId() != id) {
                throw new NotFoundException("Name " + newConnectionName
                        + " already exists, please enter another one.");
            }
        }
    }

    public void deleteConnectionById(int id) {
        ConnectionInfo connectionDel = profileManager.getConnectionInfoById(id);
        if (Objects.isNull(connectionDel)) {
            throw new NotFoundException("Not found connection by id: " + id);
        }
        profileManager.deleteConnection(connectionDel.getId(), connectionDel.getName());
    }

    private int getSelectedConnectionId() {
        return (Integer) connectionCase.getDefaultTableModel()
                .getValueAt(connectionCase.getJxTable().getSelectedRow(), 0);
    }


    private void clearConnectionCase() {
        connectionCase.getDefaultTableModel().getDataVector().removeAllElements();
        connectionCase.getDefaultTableModel().fireTableDataChanged();
    }

    private void setPanelView(Boolean isSelected) {
        connectionButtonPanel.setButtonView(isSelected);
        connectionPanel.getJTextFieldConnectionName().setEditable(!isSelected);
        connectionPanel.getJTextFieldConnectionUserName().setEditable(!isSelected);
        connectionPanel.getJTextFieldConnectionPassword().setEditable(!isSelected);
        connectionPanel.getJTextFieldConnectionDriver().setEditable(!isSelected);
        connectionPanel.getJTextFieldConnectionJar().setEditable(!isSelected);
        connectionPanel.getJTextFieldConnectionURL().setEditable(!isSelected);
        jTabbedPaneConfig.setEnabledAt(1, isSelected);
        jTabbedPaneConfig.setEnabledAt(0, isSelected);
        jTabbedPaneConfig.setEnabledAt(3, isSelected);
        profileCase.getJxTable().setEnabled(isSelected);
        taskCase.getJxTable().setEnabled(isSelected);
        connectionCase.getJxTable().setEnabled(isSelected);
        queryCase.getJxTable().setEnabled(isSelected);
        connectionTemplateCase.getJxTable().setEnabled(isSelected);
        checkboxConfig.setEnabled(isSelected);
        connectionPanel.getJarButton().setEnabled(!isSelected);
    }

    private void newEmptyPanel(){
        connectionPanel.getJTextFieldConnectionName().setText("");
        connectionPanel.getJTextFieldConnectionName().setPrompt(bundleDefault.getString("cName"));
        connectionPanel.getJTextFieldConnectionUserName().setText("");
        connectionPanel.getJTextFieldConnectionUserName().setPrompt(bundleDefault.getString("cUserName"));
        connectionPanel.getJTextFieldConnectionURL().setText("");
        connectionPanel.getJTextFieldConnectionURL().setPrompt(bundleDefault.getString("cURL"));
        connectionPanel.getJTextFieldConnectionPassword().setText("");
        connectionPanel.getJTextFieldConnectionDriver().setText("");
        connectionPanel.getJTextFieldConnectionDriver().setPrompt(bundleDefault.getString("cDriver"));
        connectionPanel.getJTextFieldConnectionJar().setText("");
        connectionPanel.getJTextFieldConnectionJar().setPrompt(bundleDefault.getString("cJar"));
    }

    private boolean isUsedOnTask(int connectionId) {
        return !profileManager.getTaskInfoList()
                .stream()
                .anyMatch(task -> task.getConnectionId() == connectionId);
    }
}
