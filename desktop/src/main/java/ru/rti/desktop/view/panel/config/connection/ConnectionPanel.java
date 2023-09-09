package ru.rti.desktop.view.panel.config.connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;

import javax.inject.Named;
import javax.swing.*;
import javax.swing.border.Border;

import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.panel.config.ButtonPanel;

import java.util.ResourceBundle;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class ConnectionPanel extends JPanel {

    private final ButtonPanel connectionButtonPanel;
    private final JLabel labelConnectionName;
    private final JLabel labelConnectionURL;
    private final JLabel labelConnectionUserName;
    private final JLabel labelConnectionPassword;
    private final JLabel labelConnectionJar;
    private final JLabel labelConnectionDriver;
    private final JXTextField jTextFieldConnectionName;
    private final JXTextField jTextFieldConnectionURL;
    private final JXTextField jTextFieldConnectionUserName;
    private final JPasswordField jTextFieldConnectionPassword;
    private final JXTextField jTextFieldConnectionJar;
    private final JXTextField jTextFieldConnectionDriver;
    private final JButton jButtonTemplate;
    private final JXTableCase connectionTemplateCase;
    private final JButton jarButton;
    private final ResourceBundle bundleDefault;

    @Inject
    public ConnectionPanel(@Named("connectionButtonPanel") ButtonPanel connectionButtonPanel,
                           @Named("connectionTemplateCase") JXTableCase connectionTemplateCase) {
        this.connectionButtonPanel = connectionButtonPanel;
        this.bundleDefault = Internationalization.getInternationalizationBundle();
        this.labelConnectionName = new JLabel("Name");
        this.labelConnectionURL = new JLabel("URL");
        this.labelConnectionUserName = new JLabel("User name");
        this.labelConnectionPassword = new JLabel("Password");
        this.labelConnectionJar = new JLabel("Jar");
        this.labelConnectionDriver = new JLabel("Driver");
        this.jTextFieldConnectionName = new JXTextField();
        this.jTextFieldConnectionName.setPrompt(bundleDefault.getString("cName"));
        this.jTextFieldConnectionName.setEditable(false);
        this.jTextFieldConnectionURL = new JXTextField();
        this.jTextFieldConnectionURL.setPrompt(bundleDefault.getString("cURL"));
        this.jTextFieldConnectionURL.setEditable(false);
        this.jTextFieldConnectionUserName = new JXTextField();
        this.jTextFieldConnectionUserName.setPrompt(bundleDefault.getString("cUserName"));
        this.jTextFieldConnectionUserName.setEditable(false);
        this.jTextFieldConnectionPassword = new JPasswordField();
        this.jTextFieldConnectionPassword.setEditable(false);
        this.jTextFieldConnectionJar = new JXTextField();
        this.jTextFieldConnectionJar.setPrompt(bundleDefault.getString("cJar"));
        this.jTextFieldConnectionJar.setEditable(false);
        this.jTextFieldConnectionDriver = new JXTextField();
        this.jTextFieldConnectionDriver.setPrompt(bundleDefault.getString("cDriver"));
        this.jTextFieldConnectionDriver.setEditable(false);
        this.jButtonTemplate = new JButton("Copy from template");
        this.jButtonTemplate.setEnabled(false);
        this.jarButton = new JButton("...");
        this.jarButton.setEnabled(false);

        this.connectionTemplateCase = connectionTemplateCase;

        Border finalBorder = GUIHelper.getBorder();
        this.jTextFieldConnectionName.setBorder(finalBorder);
        this.jTextFieldConnectionURL.setBorder(finalBorder);
        this.jTextFieldConnectionUserName.setBorder(finalBorder);
        this.jTextFieldConnectionPassword.setBorder(finalBorder);
        this.jTextFieldConnectionJar.setBorder(finalBorder);
        this.jTextFieldConnectionDriver.setBorder(finalBorder);


        JPanel jarPanel = new JPanel();
        int height = Math.max(jTextFieldConnectionJar.getPreferredSize().height, jarButton.getPreferredSize().height);

        GroupLayout layout = new GroupLayout(jarPanel);
        jarPanel.setLayout(layout);

        layout.setAutoCreateGaps(false);
        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(jTextFieldConnectionJar)
                .addComponent(jarButton));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldConnectionJar, height, height, height)
                .addComponent(jarButton));

        JPanel connectionPanel = new JPanel();
        PainlessGridBag gblCon = new PainlessGridBag(connectionPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblCon.row()
                .cellXRemainder(connectionButtonPanel).fillX();
        gblCon.row()
                .cell(labelConnectionName).cell(jTextFieldConnectionName).fillX();
        gblCon.row()
                .cell(labelConnectionURL).cell(jTextFieldConnectionURL).fillX();
        gblCon.row()
                .cell(labelConnectionUserName).cell(jTextFieldConnectionUserName).fillX();
        gblCon.row()
                .cell(labelConnectionPassword).cell(jTextFieldConnectionPassword).fillX();
        gblCon.row()
                .cell(labelConnectionJar).cellXRemainder(jarPanel).fillX();
        gblCon.row()
                .cell(labelConnectionDriver).cell(jTextFieldConnectionDriver).fillX();
        gblCon.row().cell(new JLabel()).cell(new JLabel()).fillX();

        gblCon.done();

        JPanel templatePanel = new JPanel();
        PainlessGridBag gblTemplate = new PainlessGridBag(templatePanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        gblTemplate.row()
                .cellXRemainder(new JXTitledSeparator("List of templates")).fillX();
        gblTemplate.row()
                .cell(jButtonTemplate)
                .cellXRemainder(new JLabel()).fillX();
        gblTemplate.done();

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row().cellXRemainder(connectionPanel).fillX();
        gbl.row().cellXRemainder(templatePanel).fillX();
        gbl.row().cellXYRemainder(this.connectionTemplateCase.getJScrollPane()).fillXY();
        gbl.done();
    }
}
