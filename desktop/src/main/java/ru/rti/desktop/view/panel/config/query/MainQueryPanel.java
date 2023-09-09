package ru.rti.desktop.view.panel.config.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.panel.config.ButtonPanel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.util.ResourceBundle;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class MainQueryPanel extends JPanel {

    private final ButtonPanel queryButtonPanel;
    private final JLabel labelQueryName;
    private final JLabel labelQueryDescription;
    private final JLabel labelQueryText;
    private final JXTextField queryName;
    private final JXTextField queryDescription;

    private final JComboBox<?> queryGatherDataComboBox;
    private final JLabel labelGatherDataSql;
    private final JComboBox<?> queryLoadDataModeComboBox;
    private final JLabel labelLoadDataMode;
    private final JXTextArea querySqlText;
    private final Border finalBorder;
    private final ResourceBundle bundleDefault;


    @Inject
    public MainQueryPanel(@Named("queryButtonPanel") ButtonPanel queryButtonPanel,
                          @Named("queryGatherDataComboBox") JComboBox<?> queryGatherDataComboBox,
                          @Named("querySqlText") JXTextArea querySqlText,
                          @Named("queryLoadDataModeComboBox") JComboBox<?> queryLoadDataModeComboBox) {
        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.queryButtonPanel = queryButtonPanel;
        this.labelQueryName = new JLabel("Name");
        this.labelQueryDescription = new JLabel("Description");
        this.labelQueryText = new JLabel("Text");
        this.queryName = new JXTextField();
        this.queryDescription = new JXTextField();
        this.queryDescription.setEditable(false);
        this.queryDescription.setPrompt(bundleDefault.getString("qDesc"));
        this.queryName.setEditable(false);
        this.getQueryName().setPrompt(bundleDefault.getString("qName"));
        this.querySqlText = querySqlText;
        this.querySqlText.setEditable(false);
        this.querySqlText.setBorder(new EtchedBorder());
        this.querySqlText.setPrompt(bundleDefault.getString("qSqlText"));
        this.queryGatherDataComboBox = queryGatherDataComboBox;
        this.queryGatherDataComboBox.setEnabled(false);
        this.labelGatherDataSql = new JLabel("Gather data SQL");
        this.queryLoadDataModeComboBox = queryLoadDataModeComboBox;
        this.queryLoadDataModeComboBox.setEnabled(false);
        this.labelLoadDataMode = new JLabel("Load data mode");

        this.finalBorder = GUIHelper.getBorder();
        this.queryName.setBorder(finalBorder);
        this.queryDescription.setBorder(finalBorder);
        this.querySqlText.setBorder(finalBorder);
        this.queryGatherDataComboBox.setBorder(finalBorder);
        this.queryLoadDataModeComboBox.setBorder(finalBorder);

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row()
                .cellXRemainder(this.queryButtonPanel).fillX();
        gbl.row()
                .cell(labelQueryName).cell(queryName).fillX();
        gbl.row()
                .cell(labelQueryDescription).cell(queryDescription).fillX();
        gbl.row()
                .cell(labelGatherDataSql).cell(queryGatherDataComboBox).fillX();
        gbl.row()
                .cell(labelLoadDataMode).cell(queryLoadDataModeComboBox).fillX();
        gbl.row()
                .cell(labelQueryText).cell(new JScrollPane(querySqlText)).fillXY(1, 2);
        gbl.done();
    }
}
