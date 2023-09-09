package ru.rti.desktop.view.structure.workspace.handler;

import static ru.rti.desktop.model.view.ProcessType.REAL_TIME;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import org.jdesktop.swingx.JXTable;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.config.prototype.task.WorkspaceTaskComponent;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.QueryMetadataColumnNames;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.function.MetricFunction;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.handler.LifeCycleStatus;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.structure.workspace.query.DetailsControlPanel;
import ru.rti.desktop.view.structure.workspace.task.QueryMetricColumnContainer;

@Log4j2
public class MetricColumnSelectionHandler extends ChartHandler implements ListSelectionListener,
    MouseListener, KeyListener, ActionListener {

  private final ProfileTaskQueryKey profileTaskQueryKey;

  private final QueryInfo queryInfo;
  private final TableInfo tableInfo;

  private final JXTableCase jxTableCaseMetrics;
  private final JXTableCase jxTableCaseColumns;

  private final DetailsControlPanelHandler detailsControlPanelHandler;

  private final DetailsControlPanel detailsControlPanel;
  private final QueryMetricColumnContainer listCardQueryMetricColumn;


  // TODO Resolve bug with double mouse clicks
  private int mouseMetricCounter = 0;
  private boolean alreadyMetricFired = false;
  private int mouseColumnCounter = 0;
  private boolean alreadyColumnFired = false;
  // TODO Resolve bug with double mouse clicks

  private final WorkspaceTaskComponent workspaceTaskComponent;

  public MetricColumnSelectionHandler(ChartJTabbedPane chartJTabbedPane,
                                      ProfileTaskQueryKey profileTaskQueryKey,
                                      QueryInfo queryInfo,
                                      TableInfo tableInfo,
                                      ChartInfo chartInfo,
                                      JXTableCase jxTableCaseMetrics,
                                      JXTableCase jxTableCaseColumns,
                                      JSplitPane chartGanttPanelRealTime,
                                      JSplitPane chartGanttPanelHistory,
                                      QueryMetricColumnContainer listCardQueryMetricColumn,
                                      DetailsControlPanel detailsControlPanel,
                                      DetailsControlPanelHandler detailsControlPanelHandler,
                                      WorkspaceTaskComponent workspaceTaskComponent,
                                      WorkspaceQueryComponent workspaceQueryComponent) {
    super(chartJTabbedPane, jxTableCaseMetrics, jxTableCaseColumns, tableInfo, queryInfo, chartInfo,
        profileTaskQueryKey, chartGanttPanelRealTime, chartGanttPanelHistory, workspaceQueryComponent);

    this.profileTaskQueryKey = profileTaskQueryKey;
    this.queryInfo = queryInfo;
    this.tableInfo = tableInfo;

    this.jxTableCaseMetrics = jxTableCaseMetrics;
    this.jxTableCaseColumns = jxTableCaseColumns;
    this.detailsControlPanel = detailsControlPanel;
    this.detailsControlPanelHandler = detailsControlPanelHandler;

    this.detailsControlPanel.getCount().addActionListener(new RadioListenerColumn());
    this.detailsControlPanel.getSum().addActionListener(new RadioListenerColumn());
    this.detailsControlPanel.getAverage().addActionListener(new RadioListenerColumn());

    this.detailsControlPanel.getSaveButton().addActionListener(this);
    this.detailsControlPanel.getCancelButton().addActionListener(this);

    this.jxTableCaseColumns.getJxTable().getSelectionModel().addListSelectionListener(this);
    this.jxTableCaseColumns.getJxTable().addMouseListener(this);
    this.jxTableCaseColumns.getJxTable().addKeyListener(this);

    this.jxTableCaseMetrics.getJxTable().getSelectionModel().addListSelectionListener(this);
    this.jxTableCaseMetrics.getJxTable().addMouseListener(this);
    this.jxTableCaseMetrics.getJxTable().addKeyListener(this);

    this.listCardQueryMetricColumn = listCardQueryMetricColumn;

    this.workspaceTaskComponent = workspaceTaskComponent;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    JXTable source = (JXTable) e.getSource();

    if (source.equals(jxTableCaseMetrics.getJxTable())) {
      if (!jxTableCaseMetrics.getJxTable().getSelectionModel().getValueIsAdjusting()) {

        mouseMetricCounter++;

        if (alreadyMetricFired) {

          if (mouseMetricCounter == 2) {
            mouseMetricCounter = 0;
            alreadyMetricFired = false;
          }

          return;
        } else {
          if (mouseMetricCounter == 2) {
            mouseMetricCounter = 0;
            return;
          }
        }

        int metricId = GUIHelper.getIdByColumnName(jxTableCaseMetrics.getJxTable(),
            jxTableCaseMetrics.getDefaultTableModel(), jxTableCaseMetrics.getJxTable().getSelectionModel(),
            MetricsColumnNames.ID.getColName());

        this.setSourceConfig(jxTableCaseMetrics.getJxTable());
        this.loadMetric(metricId);
        this.loadChart(REAL_TIME);
      }
    }

    if (source.equals(jxTableCaseColumns.getJxTable())) {
      if (!jxTableCaseColumns.getJxTable().getSelectionModel().getValueIsAdjusting()) {

        mouseColumnCounter++;

        if (alreadyColumnFired) {

          if (mouseColumnCounter == 2) {
            mouseColumnCounter = 0;
            alreadyColumnFired = false;
          }

          return;
        } else {
          if (mouseColumnCounter == 2) {
            mouseColumnCounter = 0;
            return;
          }
        }

        int cProfileId = GUIHelper.getIdByColumnName(jxTableCaseColumns.getJxTable(),
            jxTableCaseColumns.getDefaultTableModel(), jxTableCaseColumns.getJxTable().getSelectionModel(),
            QueryMetadataColumnNames.ID.getColName());

        this.setSourceConfig(jxTableCaseColumns.getJxTable());
        this.loadColumn(cProfileId);
        this.loadChart(REAL_TIME);
      }
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

    // prevents double events
    if (!e.getValueIsAdjusting()) {

      /* Metrics */
      if (e.getSource().equals(jxTableCaseMetrics.getJxTable().getSelectionModel())) {
        log.info("Select metric..");

        int metricId = GUIHelper.getIdByColumnName(jxTableCaseMetrics.getJxTable(),
            jxTableCaseMetrics.getDefaultTableModel(), listSelectionModel,
            MetricsColumnNames.ID.getColName());

        this.setSourceConfig(jxTableCaseMetrics.getJxTable());
        this.loadMetric(metricId);

        this.loadChart(REAL_TIME);
        this.mouseMetricCounter = 0;
        this.alreadyMetricFired = true;
      }

      /* Columns */
      if (e.getSource().equals(jxTableCaseColumns.getJxTable().getSelectionModel())) {
        log.info("Select columns..");

        int cProfileId = GUIHelper.getIdByColumnName(jxTableCaseColumns.getJxTable(),
            jxTableCaseColumns.getDefaultTableModel(), listSelectionModel,
            QueryMetadataColumnNames.ID.getColName());

        this.setSourceConfig(jxTableCaseColumns.getJxTable());
        this.loadColumn(cProfileId);

        this.loadChart(REAL_TIME);
        this.mouseColumnCounter = 0;
        this.alreadyColumnFired = true;
      }

    }
  }

  private void loadMetric(int metricId) {
    log.info("Load metrics..");
    Metric metric = queryInfo.getMetricList() == null ? new Metric() : queryInfo.getMetricList().stream()
        .filter(f -> f.getId() == metricId)
        .findAny()
        .orElseThrow(() -> new NotFoundException("Not found metric by id: " + metricId));

    log.info(metric);

    checkStatusAndDoAction();

    detailsControlPanelHandler.clearAll();
    detailsControlPanelHandler.loadMetricToDetails(metric);
  }

  private void loadColumn(int cProfileId) {
    log.info("Load columns..");
    CProfile cProfile = tableInfo.getCProfiles().stream()
        .filter(f -> f.getColId() == cProfileId)
        .findAny()
        .orElseThrow();

    log.info(cProfile);

    checkStatusAndDoAction();

    detailsControlPanelHandler.clearAll();
    detailsControlPanelHandler.loadColumnToDetails(getMetricByCProfile(cProfile));
  }

  private void checkStatusAndDoAction() {
    if (detailsControlPanelHandler.getStatus().equals(LifeCycleStatus.EDIT)) {
      /**
      TODO Save metric functions implementation in progress
      JTextFieldCase jTextFieldCase = GUIHelper.getJTextFieldCase("Metric name");

      int input = JOptionPane.showOptionDialog(null,
          jTextFieldCase.getJPanel(),"Create new metric?",
          JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,null,
          new String[]{"Yes", "No"},"No");

      if (input == 0) {
        detailsControlPanelHandler.saveNewMetric(jTextFieldCase.getJTextField().getText());
      } else if (input == 1) {
        detailsControlPanelHandler.cancelToSaveNewMetric();
      }
      **/

      detailsControlPanelHandler.cancelToSaveNewMetric();

      metricFunctionOnEdit = MetricFunction.NONE;
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      this.alreadyColumnFired = false;
    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      this.alreadyColumnFired = false;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == detailsControlPanel.getSaveButton()) {
      log.info("Cancel button clicked");
      metricFunctionOnEdit = MetricFunction.NONE;
    } else if (e.getSource() == detailsControlPanel.getCancelButton()) {
      log.info("Cancel button clicked");
      metricFunctionOnEdit = MetricFunction.NONE;
    }
  }

  private class RadioListenerColumn implements ActionListener {

    public RadioListenerColumn() {}

    public void actionPerformed(ActionEvent e){
      JRadioButton button = (JRadioButton) e.getSource();

      switch (button.getText()) {
        case "Count" -> {
          metricFunctionOnEdit = MetricFunction.COUNT;
          loadChart(REAL_TIME);
        }
        case "Sum" -> {
          metricFunctionOnEdit = MetricFunction.SUM;
          loadChart(REAL_TIME);
        }
        case "Average" -> {
          metricFunctionOnEdit = MetricFunction.AVERAGE;
          loadChart(REAL_TIME);
        }
      }
    }

  }

}
