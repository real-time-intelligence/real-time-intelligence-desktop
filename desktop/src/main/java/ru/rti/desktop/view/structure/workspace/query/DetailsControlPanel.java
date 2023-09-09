package ru.rti.desktop.view.structure.workspace.query;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import lombok.Data;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;

@Data
public class DetailsControlPanel extends JPanel {

  private final JComboBox<String> yAxis;
  private final JComboBox<String> group;
  private final JComboBox<?> chartType;

  private final JRadioButton asIs;
  private final JRadioButton count;
  private final JRadioButton sum;
  private final JRadioButton average;

  private final JButton saveButton;
  private final JButton editButton;
  private final JButton cancelButton;

  private final ButtonGroup buttonGroupFunction;

  public DetailsControlPanel() {
    this.yAxis = new JComboBox<>();
    this.group = new JComboBox<>();
    this.chartType = new JComboBox<>(ChartType.values());

    AutoCompleteDecorator.decorate(yAxis);
    AutoCompleteDecorator.decorate(group);
    AutoCompleteDecorator.decorate(chartType);

    this.asIs = new JRadioButton(MetricFunction.ASIS.getName(), false);
    this.count = new JRadioButton(MetricFunction.COUNT.getName(), false);
    this.sum = new JRadioButton(MetricFunction.SUM.getName(), false);
    this.average = new JRadioButton(MetricFunction.AVERAGE.getName(), false);

    this.saveButton = new JButton("Save");
    this.editButton = new JButton("Edit");
    this.cancelButton = new JButton("Cancel");

    buttonGroupFunction = new ButtonGroup();
    buttonGroupFunction.add(asIs);
    buttonGroupFunction.add(count);
    buttonGroupFunction.add(sum);
    buttonGroupFunction.add(average);

    Box boxFunction = Box.createHorizontalBox();
    boxFunction.add(asIs);
    boxFunction.add(count);
    boxFunction.add(sum);
    boxFunction.add(average);

    JPanel jPanelSettings = new JPanel();
    PainlessGridBag gblSettings = new PainlessGridBag(jPanelSettings, GUIHelper.getPainlessGridbagConfiguration(), false);
    gblSettings.row()
        .cell(new JLabel("Y Axis")).cell(yAxis).fillX();
    gblSettings.row()
        .cell(new JLabel("Group")).cell(group).fillX();
    gblSettings.row()
        .cell(new JLabel("Chart")).cell(chartType).fillX();
    gblSettings.row()
        .cell(new JLabel()).cell(new JLabel()).fillXY();

    setConstrainsInsets(gblSettings, yAxis, 1);
    setConstrainsInsets(gblSettings, group, 1);
    setConstrainsInsets(gblSettings, chartType, 1);
    gblSettings.done();

    JPanel jPanelFunction = new JPanel();
    PainlessGridBag gblFunction = new PainlessGridBag(jPanelFunction, GUIHelper.getPainlessGridbagConfiguration(), false);
    gblFunction.row()
        .cell(asIs).fillX();
    gblFunction.row()
        .cell(count).fillX();
    gblFunction.row()
        .cell(sum).fillX();
    gblFunction.row()
        .cell(average).fillX();
    gblFunction.row()
        .cell(new JLabel()).fillXY();

    setConstrainsInsets(gblFunction, asIs, 0);
    setConstrainsInsets(gblFunction, count, 0);
    setConstrainsInsets(gblFunction, sum, 0);
    setConstrainsInsets(gblFunction, average, 0);
    gblFunction.done();

    JPanel jPanelButton = new JPanel();
    PainlessGridBag gblButton = new PainlessGridBag(jPanelButton, GUIHelper.getPainlessGridbagConfiguration(), false);
    gblButton.row()
        .cell(saveButton).fillX();
    gblButton.row()
        .cell(editButton).fillX();
    gblButton.row()
        .cell(cancelButton).fillX();
    gblButton.row()
        .cell(new JLabel()).fillXY();

    setConstrainsInsets(gblButton, saveButton, 1);
    setConstrainsInsets(gblButton, editButton, 1);
    setConstrainsInsets(gblButton, cancelButton, 1);
    gblButton.done();

    PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
    gbl.row()
        .cellX(jPanelFunction, 1).fillXY(1, 1)
        .cellX(jPanelSettings, 5).fillXY(5, 5)
        .cellX(jPanelButton, 1).fillXY(1, 1);
    gbl.done();

    setEnabled(false);
  }

  public void setEnabled(boolean flag) {
    yAxis.setEnabled(flag);
    group.setEnabled(flag);
    chartType.setEnabled(flag);

    asIs.setEnabled(false);
    count.setEnabled(flag);
    sum.setEnabled(flag);
    average.setEnabled(flag);

    saveButton.setEnabled(flag);
    editButton.setEnabled(flag);
    cancelButton.setEnabled(flag);
  }

  public void setSelectedRadioButton(MetricFunction metricFunction) {
    switch (metricFunction) {
      case ASIS -> buttonGroupFunction.setSelected(asIs.getModel(), true);
      case COUNT -> buttonGroupFunction.setSelected(count.getModel(), true);
      case SUM -> buttonGroupFunction.setSelected(sum.getModel(), true);
      case AVERAGE -> buttonGroupFunction.setSelected(average.getModel(), true);
    }
  }

  private void setConstrainsInsets(PainlessGridBag gbl, JComponent component, int value) {
    gbl.constraints(component).insets.top = value;
    gbl.constraints(component).insets.bottom = value;
    gbl.constraints(component).insets.left = value;
    gbl.constraints(component).insets.right = value;
  }

}
