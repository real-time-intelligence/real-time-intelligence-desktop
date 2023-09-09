package ru.rti.desktop.view.structure.workspace.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.cstype.CType;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.db.TimestampType;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;
import ru.rti.desktop.model.textfield.JTextFieldCase;
import ru.rti.desktop.model.view.handler.LifeCycleStatus;
import ru.rti.desktop.view.structure.workspace.query.DetailsControlPanel;

@Log4j2
public class DetailsControlPanelHandler implements ActionListener {

  private final DetailsControlPanel detailsControlPanel;

  @Getter
  private LifeCycleStatus status;

  private Metric metric;

  public DetailsControlPanelHandler(DetailsControlPanel detailsControlPanel) {
    this.detailsControlPanel = detailsControlPanel;

    this.detailsControlPanel.getSaveButton().addActionListener(this);
    this.detailsControlPanel.getEditButton().addActionListener(this);
    this.detailsControlPanel.getCancelButton().addActionListener(this);

    this.detailsControlPanel.getCount().addActionListener(new RadioListenerDetailsUI());
    this.detailsControlPanel.getSum().addActionListener(new RadioListenerDetailsUI());
    this.detailsControlPanel.getAverage().addActionListener(new RadioListenerDetailsUI());

    this.status = LifeCycleStatus.NONE;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == detailsControlPanel.getSaveButton()) {
      log.info("Save metric button clicked");

      JTextFieldCase jTextFieldCase = GUIHelper.getJTextFieldCase("Metric name");

      int input = JOptionPane.showOptionDialog(null,
          jTextFieldCase.getJPanel(),"Create new metric?",
          JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,null,
          new String[]{"Yes", "No"},"No");

      if (input == 0) {
        saveNewMetric(jTextFieldCase.getJTextField().getText());
      } else if (input == 1) {
        cancelToSaveNewMetric();

        setButtonEnabled(false, true, false);
        setRadioButtonEnabled(false, false, false, false);
        setComboBoxEnabled(false, false, false);
      }

      setButtonEnabled(false, true, false);
      setRadioButtonEnabled(false,false, false, false);
      setComboBoxEnabled(false, false, false);

    } else if (e.getSource() == detailsControlPanel.getEditButton()) {
      log.info("Edit button clicked");

      this.status = LifeCycleStatus.EDIT;

      setButtonEnabled(false, false, true);

      if (Arrays.stream(TimestampType.values()).anyMatch((t) -> t.name().equals(metric.getYAxis().getColDbTypeName()))) {
        setRadioButtonEnabled(false, true, true, true);
      } else if (CType.STRING.equals(metric.getYAxis().getCsType().getCType())) {
        setRadioButtonEnabled(false, true, false, false);
      } else {
        setRadioButtonEnabled(false, true, true, true);
      }

      setComboBoxEnabled(false, false, false);

    } else if (e.getSource() == detailsControlPanel.getCancelButton()) {
      log.info("Cancel button clicked");

      cancelToSaveNewMetric();

      setButtonEnabled(false, true, false);
      setRadioButtonEnabled(false,false, false, false);
      setComboBoxEnabled(false, false, false);
    }
  }

  public void clearAll() {
    setYAxis(null);
    setGroup(null);
    setChartType(ChartType.NONE);

    detailsControlPanel.getButtonGroupFunction().clearSelection();
  }

  public void loadMetricToDetails(Metric metric) {
    this.metric = metric;

    setMetricInUI(metric);

    setButtonEnabled(false, false, false);
    setRadioButtonEnabled(false, false, false, false);
    setComboBoxEnabled(false, false, false);
  }

  public void loadColumnToDetails(Metric metric) {
    this.metric = metric;

    loadMetricToDetails(metric);

    setButtonEnabled(false, true, false);
    setRadioButtonEnabled(false, false, false, false);
    setComboBoxEnabled(false, false, false);
  }

  public void setMetricInUI(Metric metric) {
    setYAxis(metric.getYAxis().getColName());
    setGroup(metric.getGroup().getColName());
    setChartType(metric.getChartType());

    setSelectedRadioButton(metric.getMetricFunction());
  }

  public void saveNewMetric(String metricName) {
    log.info("Metric name to create: " + metricName);
    this.status = LifeCycleStatus.NONE;
  }

  public void cancelToSaveNewMetric() {
    this.status = LifeCycleStatus.NONE;

    setMetricInUI(metric);
  }

  public void setSelectedRadioButton(MetricFunction metricFunction) {
    detailsControlPanel.setSelectedRadioButton(metricFunction);
  }

  private void setYAxis(String value) {
    detailsControlPanel.getYAxis().setSelectedItem(value);
  }

  private void setGroup(String value) {
    detailsControlPanel.getGroup().setSelectedItem(value);
  }

  private void setChartType(ChartType chartType) {
    detailsControlPanel.getChartType().setSelectedItem(chartType.toString());
  }

  private void setButtonEnabled(boolean save, boolean edit, boolean cancel) {
    detailsControlPanel.getSaveButton().setEnabled(save);
    detailsControlPanel.getEditButton().setEnabled(edit);
    detailsControlPanel.getCancelButton().setEnabled(cancel);
  }

  private void setRadioButtonEnabled(boolean asIs, boolean count, boolean sum, boolean average) {
    detailsControlPanel.getAsIs().setEnabled(asIs);
    detailsControlPanel.getCount().setEnabled(count);
    detailsControlPanel.getSum().setEnabled(sum);
    detailsControlPanel.getAverage().setEnabled(average);
  }

  private void setComboBoxEnabled(boolean yAxis, boolean group, boolean chartType) {
    detailsControlPanel.getYAxis().setEnabled(yAxis);
    detailsControlPanel.getGroup().setEnabled(group);
    detailsControlPanel.getChartType().setEnabled(chartType);
  }

  private class RadioListenerDetailsUI implements ActionListener {

    public RadioListenerDetailsUI() {}

    public void actionPerformed(ActionEvent e) {
      JRadioButton button = (JRadioButton) e.getSource();

      switch (button.getText()) {
        case "Count" -> setChartType(ChartType.STACKED);
        case "Sum", "Average" -> setChartType(ChartType.LINEAR);
      }
    }
  }

}
