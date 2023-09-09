package ru.rti.desktop.view.panel.template;

import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.config.Query;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.prompt.Internationalization;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class TemplateEditPanel extends JDialog {

    private final JButton templateSaveJButton;

    private final JLabel labelProfileName;
    private final JLabel labelProfileDesc;

    private final JLabel labelTaskName;
    private final JLabel labelTaskDesc;

    private final JLabel labelQueryName;
    private final JLabel labelQueryDesc;

    private final JLabel labelConnName;
    private final JLabel labelConnUserName;
    private final JLabel labelConnPassword;
    private final JLabel labelConnUrl;
    private final JLabel labelConnPort;
    private final JLabel labelConnJar;

    private final JXTextField profileName;
    private final JXTextArea profileDesc;
    private final JLabel statusProfile;

    private final JXTextField taskName;
    private final JXTextArea taskDesc;
    private final JLabel statusTask;

    private final JXTextField connName;
    private final JXTextField connUserName;
    private final JPasswordField connPassword;
    private final JXTextField connUrl;
    private final JXTextField connJar;
    private final JLabel statusConn;

    private final JXTextField queryName;
    private final JXTextField queryDesc;
    private final JXLabel statusQuery;

    private final JXTableCase templateQueryCase;
    private final ResourceBundle bundleDefault;

    @Inject
    public TemplateEditPanel(@Named("templateSaveJButton") JButton templateSaveJButton,
                             @Named("templateProfileDescSave") JXTextArea profileDesc,
                             @Named("templateTaskDescSave") JXTextArea taskDesc) {
        this.bundleDefault = Internationalization.getInternationalizationBundle();
        this.templateSaveJButton = templateSaveJButton;

        this.labelProfileName = new JLabel("Name");
        this.labelProfileDesc = new JLabel("Description");

        this.labelTaskName = new JLabel("Name");
        this.labelTaskDesc = new JLabel("Description");

        this.labelQueryName = new JLabel("Name");
        this.labelQueryDesc = new JLabel("Description");

        this.labelConnName = new JLabel("Name");
        this.labelConnUserName = new JLabel("Username");
        this.labelConnPassword = new JLabel("Password");
        this.labelConnUrl = new JLabel("Hostname");
        this.labelConnPort = new JLabel("Port");
        this.labelConnJar = new JLabel("Jar");

        this.profileName = new JXTextField();
        this.profileName.setPrompt("Profile name");
        this.profileDesc = profileDesc;
        this.statusProfile = new JLabel("Profile already exist");
        this.statusProfile.setForeground(new Color(218, 43, 43));
        this.statusProfile.setVisible(false);

        this.taskName = new JXTextField();
        this.taskName.setPrompt("Task name");
        this.taskDesc = taskDesc;
        this.statusTask = new JLabel("Task already exist");
        this.statusTask.setVisible(false);
        this.statusTask.setForeground(new Color(218, 43, 43));

        this.connName = new JXTextField();
        this.connName.setPrompt("Connection name");
        this.connUserName = new JXTextField();
        this.connUserName.setPrompt("Database user name");
        this.connPassword = new JPasswordField();
        this.connJar = new JXTextField();
        this.connJar.setPrompt("Path to JDBC jar file");
        this.connUrl = new JXTextField();
        this.connUrl.setPrompt("Hostname for data source");
        this.statusConn = new JLabel("Connection already exist");
        this.statusConn.setVisible(false);
        this.statusConn.setForeground(new Color(218, 43, 43));

        this.queryName = new JXTextField();
        this.queryName.setPrompt("Query name");
        this.queryDesc = new JXTextField();
        this.queryDesc.setPrompt("Query description");
        this.statusQuery = new JXLabel("Query already exist");
        this.statusQuery.setForeground(new Color(218, 43, 43));
        this.statusQuery.setVisible(false);
        this.statusQuery.setAutoscrolls(true);

        this.statusQuery.setLineWrap(true);

        this.templateQueryCase = loadTemplateEditQueryCase();

        JPanel panelEntities = new JPanel();
        JPanel panelSave = new JPanel(new FlowLayout(FlowLayout.CENTER));
        PainlessGridBag gblEntity = new PainlessGridBag(panelEntities, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblEntity.row()
                .cell(new JXTitledSeparator("Profile")).fillX()
                .cell(new JXTitledSeparator("Task")).fillX()
                .cell(new JXTitledSeparator("Connection")).fillX()
                .cell(new JXTitledSeparator("Query")).fillX();

        gblEntity.row()
                .cell(fillProfilePanel()).fillXY()
                .cell(fillTaskPanel()).fillXY()
                .cell(fillConnectionPanel()).fillXY()
                .cell(fillQueryPanel()).fillXY();

        gblEntity.done();

        panelSave.add(templateSaveJButton);

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row().cellXRemainder(panelEntities).fillXY();
        gbl.row().cell(panelSave).fillX();

        gbl.done();

        this.setTitle("Edit template values");

        this.packTemplate(false);
    }

    private void packTemplate(boolean visible) {
        this.setVisible(visible);
        this.setResizable(false);
        this.setModal(true);
        this.setResizable(true);
        this.pack();

        this.setSize(new Dimension(1200, 300));
        this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    }

    public JXTableCase loadTemplateEditQueryCase() {
        JXTableCase jxTableCase =
                GUIHelper.getEditJXTableCase(3,
                        new String[]{
                                QueryColumnNames.ID.getColName(),
                                QueryColumnNames.NAME.getColName(),
                                QueryColumnNames.DESCRIPTION.getColName()
                        });

        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);
        jxTableCase.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());

        jxTableCase.getJxTable().setSortable(false);
        return jxTableCase;
    }

    public void updateModelTemplateEditQueryCase(List<Query> queryList) {
        templateQueryCase.getDefaultTableModel().getDataVector().removeAllElements();
        templateQueryCase.getDefaultTableModel().fireTableDataChanged();

        queryList.forEach(e ->
                templateQueryCase
                        .getDefaultTableModel()
                        .addRow(new Object[]{e.getId(), e.getName(), e.getDescription()}));
    }


    private JPanel fillProfilePanel() {
        JPanel panelProfile = new JPanel();
        JPanel statusPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();


        PainlessGridBag gblProfile = new PainlessGridBag(panelProfile, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gblStatus = new PainlessGridBag(statusPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gbl = new PainlessGridBag(panel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblProfile.row()
                .cell(labelProfileName).cellXRemainder(profileName).fillX();
        gblProfile.row()
                .cell(labelProfileDesc).cellXRemainder(new JScrollPane(profileDesc)).fillXY();
        gblProfile.row()
                .cell(new JLabel()).cell(new JLabel()).fillXY();
        statusPanel.add(new JLabel("Status"), BorderLayout.SOUTH);

        gbl.row().cell(panelProfile).fillX();
        gbl.row().cell(statusPanel).fillXY();


        gblProfile.done();
        gbl.done();

        return panel;
    }

    private JPanel fillTaskPanel() {
        JPanel panelTask = new JPanel();
        JPanel statusPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();


        PainlessGridBag gblTask = new PainlessGridBag(panelTask, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gbl = new PainlessGridBag(panel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblTask.row()
                .cell(labelTaskName).cell(taskName).fillX();
        gblTask.row()
                .cell(labelTaskDesc).cell(new JScrollPane(taskDesc)).fillXY();
        gblTask.row()
                .cell(new JLabel()).cell(new JLabel()).fillXY();
        statusPanel.add(statusTask,BorderLayout.SOUTH);

        gbl.row().cell(panelTask).fillX();
        gbl.row().cell(statusPanel).fillXY();

        gblTask.done();
        gbl.done();

        return panel;
    }

    private JPanel fillConnectionPanel() {
        JPanel panelConn = new JPanel();
        JPanel statusPanel = new JPanel( new BorderLayout());
        JPanel panel = new JPanel();

        PainlessGridBag gbl = new PainlessGridBag(panel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gblConn = new PainlessGridBag(panelConn, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblConn.row()
                .cell(labelConnName).cell(connName).fillX();
        gblConn.row()
                .cell(labelConnUserName).cell(connUserName).fillX();
        gblConn.row()
                .cell(labelConnPassword).cell(connPassword).fillX();
        gblConn.row()
                .cell(labelConnUrl).cell(connUrl).fillX();
        gblConn.row()
                .cell(labelConnJar).cell(connJar).fillX();
        gblConn.row()
                .cell(new JLabel()).cell(new JLabel()).fillXY();
        statusPanel.add(statusConn,BorderLayout.SOUTH);

        gbl.row().cell(panelConn).fillXY();
        gbl.row().cell(statusPanel).fillXY();

        gblConn.done();
        gbl.done();

        return panel;
    }

    private JPanel fillQueryPanel() {
        JPanel panelQuery = new JPanel();
        JPanel statusPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();

        PainlessGridBag gbl = new PainlessGridBag(panel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gblQuery = new PainlessGridBag(panelQuery, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        gblQuery.row()
                .cell(templateQueryCase.getJScrollPane()).fillXY();
        statusPanel.add(statusQuery,BorderLayout.SOUTH);

        gbl.row().cell(panelQuery).fillXY();
        gbl.row().cell(statusPanel).fillXY();

        gblQuery.done();
        gbl.done();

        return panel;
    }
}
