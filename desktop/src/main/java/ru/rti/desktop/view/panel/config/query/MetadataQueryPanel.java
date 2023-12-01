package ru.rti.desktop.view.panel.config.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.fbase.model.profile.table.IType;
import org.fbase.model.profile.table.TType;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.custom.DetailedComboBox;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.prompt.Internationalization;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ResourceBundle;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class MetadataQueryPanel extends JPanel{
    private final JXTableCase configMetadataCase;
    private final JButton loadMetadata;
    private final DetailedComboBox queryConnectionMetadataComboBox;

    private final JButton editMetadata;
    private final JButton saveMetadata;
    private final DetailedComboBox timestampComboBox;
    private final JLabel timestamp;
    private final JButton cancelMetadata;
    private final Border finalBorder;
    private final JCheckBox compression;
    private final JXTextField tableName;
    private final JComboBox<TType> tableType;
    private final JComboBox<IType> tableIndex;
    private final ResourceBundle bundleDefault;

    @Inject
    public MetadataQueryPanel( @Named("configMetadataCase") JXTableCase configMetadataCase,
                               @Named("queryConnectionMetadataComboBox") DetailedComboBox queryConnectionMetadataComboBox,
                               @Named("timestampComboBox") DetailedComboBox timestampComboBox,
                               @Named("tableType") JComboBox<TType> tableType,
                               @Named("indexType") JComboBox<IType> tableIndex) {
        this.timestampComboBox = timestampComboBox;
        this.timestampComboBox.setEnabled(false);

        this.bundleDefault = Internationalization.getInternationalizationBundle();

        this.configMetadataCase = configMetadataCase;

        this.loadMetadata = new JButton("Load");
        this.loadMetadata.setToolTipText(bundleDefault.getString("loadMeta"));
        this.loadMetadata.setMnemonic('L');
        this.queryConnectionMetadataComboBox = queryConnectionMetadataComboBox;
        this.saveMetadata = new JButton("Save");
        this.saveMetadata.setEnabled(false);
        this.saveMetadata.setMnemonic('V');
        this.editMetadata = new JButton("Edit");
        this.editMetadata.setEnabled(false);
        this.editMetadata.setMnemonic('D');
        this.timestamp = new JLabel("Timestamp");
        this.cancelMetadata = new JButton("Cancel");
        this.cancelMetadata.setEnabled(false);
        this.cancelMetadata.setMnemonic('C');

        this.compression = new JCheckBox("Compression");
        this.compression.setToolTipText("Compression");

        this.tableName = new JXTextField(bundleDefault.getString("metaName"));
        this.tableName.setEditable(false);

        this.tableType = tableType;
        this.tableType.setToolTipText("TableType");
        this.tableType.setEnabled(false);
        AutoCompleteDecorator.decorate(this.tableType);
        this.tableIndex = tableIndex;
        this.tableIndex.setEnabled(false);
        AutoCompleteDecorator.decorate(this.tableIndex);

        this.finalBorder = GUIHelper.getBorder();
        this.configMetadataCase.getJxTable().setBorder(finalBorder);
        this.queryConnectionMetadataComboBox.setBorder(finalBorder);
        this.timestampComboBox.setBorder(finalBorder);
        this.tableName.setBorder(finalBorder);
        this.tableType.setBorder(finalBorder);
        this.tableIndex.setBorder(finalBorder);

        JPanel btnPanel = new JPanel();
        JPanel tablePanel = new JPanel();
        JPanel tableTitlePanel = new JPanel();
        JPanel tableDataPanel = new JPanel();
        tableDataPanel.setLayout(new GridLayout(2, 4, 5, 0));
        JPanel columnPanel = new JPanel();
        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        PainlessGridBag gblBtn = new PainlessGridBag(btnPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gblTable = new PainlessGridBag(tablePanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        PainlessGridBag gblCol = new PainlessGridBag(columnPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblBtn.row()
                .cell(loadMetadata)
                .cell(this.queryConnectionMetadataComboBox)
                .cell(GUIHelper.verticalSeparator())
                .cell(editMetadata)
                .cell(saveMetadata)
                .cell(cancelMetadata)
                .cell(new JLabel())
                .fillX();
        gblBtn.done();

        tableTitlePanel.setLayout(new BorderLayout());
        JXTitledSeparator tableTitle = new JXTitledSeparator("Table");
        tableTitle.setPreferredSize(new Dimension(1345, 5));
        tableTitlePanel.add(tableTitle, BorderLayout.WEST);
        tableTitlePanel.add(compression, BorderLayout.EAST);

        tableDataPanel.add(new JLabel("Name"));
        tableDataPanel.add(new JLabel("Type"));
        tableDataPanel.add(new JLabel("Index"));
        tableDataPanel.add(timestamp);

        tableDataPanel.add(tableName);
        tableDataPanel.add(tableType);
        tableDataPanel.add(tableIndex);
        tableDataPanel.add(timestampComboBox);

        gblTable.row()
                .cell(tableTitlePanel).fillX();
        gblTable.row()
                .cell(tableDataPanel).fillX();

        gblTable.done();

        gblCol.row()
                .cell(new JXTitledSeparator("Column")).fillX();
        gblCol.row()
                .cellXYRemainder(configMetadataCase.getJScrollPane()).fillXY();
        gblCol.done();

        gbl.row()
                .cell(btnPanel).fillX();
        gbl.row()
                .cell(tablePanel).fillX();
        gbl.row()
                .cellXYRemainder(columnPanel).fillXY();
        gbl.done();
    }
}
