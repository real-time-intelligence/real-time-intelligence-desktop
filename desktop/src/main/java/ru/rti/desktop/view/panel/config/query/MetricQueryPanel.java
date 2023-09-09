package ru.rti.desktop.view.panel.config.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.panel.config.ButtonPanel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.ResourceBundle;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class MetricQueryPanel extends JPanel {

    private final ButtonPanel metricQueryButtonPanel;
    private final JXTextField nameMetric;
    private final JCheckBox defaultCheckBox;
    private final JXTextField xTextFile;
    private final JComboBox<String> yComboBox;
    private final JComboBox<String> dimensionComboBox;
    private final JComboBox<?> metricFunction;
    private final JComboBox<?> chartType;
    private final JXTableCase configMetricCase;
    private final ResourceBundle bundleDefault;

    @Inject
    public MetricQueryPanel(@Named("metricQueryButtonPanel") ButtonPanel metricQueryButtonPanel,
                            @Named("configMetricCase") JXTableCase configMetricCase,
                            @Named("metricFunction") JComboBox<?> metricFunction,
                            @Named("chartType") JComboBox<?> chartType) {
        this.metricQueryButtonPanel = metricQueryButtonPanel;
        this.bundleDefault = Internationalization.getInternationalizationBundle();
        this.nameMetric = new JXTextField();
        this.nameMetric.setPrompt(bundleDefault.getString("metricName"));
        this.defaultCheckBox = new JCheckBox(bundleDefault.getString("metricDef"), false);
        this.xTextFile = new JXTextField();
        this.yComboBox = new JComboBox<String>();
        this.dimensionComboBox = new JComboBox<>();
        this.metricFunction = metricFunction;
        this.chartType = chartType;
        this.configMetricCase = configMetricCase;

        Border finalBorder = GUIHelper.getBorder();
        this.nameMetric.setBorder(finalBorder);
        this.xTextFile.setBorder(finalBorder);
        this.yComboBox.setBorder(finalBorder);
        this.dimensionComboBox.setBorder(finalBorder);
        this.metricFunction.setBorder(finalBorder);
        this.chartType.setBorder(finalBorder);

        AutoCompleteDecorator.decorate(yComboBox);
        AutoCompleteDecorator.decorate(dimensionComboBox);
        AutoCompleteDecorator.decorate(this.metricFunction);
        AutoCompleteDecorator.decorate(this.chartType);

        defaultCheckBox.setEnabled(false);
        nameMetric.setEditable(false);
        xTextFile.setEnabled(false);
        yComboBox.setEnabled(false);
        dimensionComboBox.setEnabled(false);
        this.metricFunction.setEnabled(false);
        this.chartType.setEnabled(false);

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row()
                .cellXRemainder(metricQueryButtonPanel).fillX();

        gbl.row()
                .cell(defaultCheckBox).cell(new JLabel()).fillX();

        gbl.row()
                .cell(new JLabel("Name")).cell(nameMetric).fillX();
        gbl.row()
                .cell(new JLabel(bundleDefault.getString("xAxis"))).cell(xTextFile).fillX();
        gbl.row()
                .cell(new JLabel("Y axis")).cell(yComboBox).fillX();
        gbl.row()
                .cell(new JLabel("Group")).cell(dimensionComboBox).fillX();

        gbl.row()
                .cell(new JLabel("Function"))
                .cell(metricFunction).fillX();

        gbl.row()
                .cell(new JLabel("Chart"))
                .cell(chartType).fillX();

        gbl.row()
                .cellXYRemainder(this.configMetricCase.getJScrollPane())
                .fillXY();

        gbl.done();
    }
}
