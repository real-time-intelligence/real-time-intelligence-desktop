package ru.rti.desktop.view.structure.workspace.handler;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;
import static ru.rti.desktop.model.SourceConfig.COLUMNS;
import static ru.rti.desktop.model.SourceConfig.METRICS;
import static ru.rti.desktop.model.function.ChartType.*;
import static ru.rti.desktop.model.view.ProcessType.HISTORY;
import static ru.rti.desktop.model.view.ProcessType.REAL_TIME;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;

import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import org.fbase.model.profile.cstype.CType;
import org.jdesktop.swingx.JXTable;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.SourceConfig;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.db.TimestampType;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.DetailChart;
import ru.rti.desktop.view.chart.HelperChart;
import ru.rti.desktop.view.chart.stacked.ClientHistorySCP;
import ru.rti.desktop.view.chart.stacked.ClientRealTimeSCP;
import ru.rti.desktop.view.chart.stacked.ServerClientHistoryOneSCP;
import ru.rti.desktop.view.chart.stacked.ServerClientRealTimeOneSCP;
import ru.rti.desktop.view.chart.stacked.ServerHistorySCP;
import ru.rti.desktop.view.chart.stacked.ServerRealTimeSCP;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;
import ru.rti.desktop.view.detail.DetailPanel;
import ru.rti.desktop.view.handler.MouseListenerImpl;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.structure.workspace.query.DetailsControlPanel;

@Log4j2
public abstract class ChartHandler extends MouseListenerImpl implements HelperChart {
  protected ChartJTabbedPane chartJTabbedPane;

  protected QueryInfo queryInfo;
  private final TableInfo tableInfo;

  protected ChartInfo chartInfo;
  protected SourceConfig sourceConfig = METRICS;

  protected final JXTableCase jxTableCaseMetrics;
  protected final JXTableCase jxTableCaseColumns;

  protected WorkspaceQueryComponent workspaceQueryComponent;
  protected ProfileTaskQueryKey profileTaskQueryKey;

  protected JSplitPane chartGanttPanelRealTime;
  protected JSplitPane chartGanttPanelHistory;

  protected ExecutorService executorService;

  protected final ResourceBundle bundleDefault;

  protected Color colorBlack;
  protected Color colorBlue;

  protected MetricFunction metricFunctionOnEdit = MetricFunction.NONE;

  protected DetailsControlPanel detailsControlPanel;

  @Inject
  @Named("eventListener")
  EventListener eventListener;

  @Inject
  @Named("profileManager")
  ProfileManager profileManager;

  public ChartHandler(ChartJTabbedPane chartJTabbedPane,
                      JXTableCase jxTableCaseMetrics,
                      JXTableCase jxTableCaseColumns,
                      TableInfo tableInfo,
                      QueryInfo queryInfo,
                      ChartInfo chartInfo,
                      ProfileTaskQueryKey profileTaskQueryKey,
                      JSplitPane chartGanttPanelRealTime,
                      JSplitPane chartGanttPanelHistory,
                      DetailsControlPanel detailsControlPanel,
                      WorkspaceQueryComponent workspaceQueryComponent) {
    this.chartJTabbedPane = chartJTabbedPane;
    this.jxTableCaseMetrics = jxTableCaseMetrics;
    this.jxTableCaseColumns = jxTableCaseColumns;
    this.tableInfo = tableInfo;
    this.queryInfo = queryInfo;
    this.chartInfo = chartInfo;
    this.profileTaskQueryKey = profileTaskQueryKey;
    this.chartGanttPanelRealTime = chartGanttPanelRealTime;
    this.chartGanttPanelHistory = chartGanttPanelHistory;
    this.detailsControlPanel = detailsControlPanel;
    this.workspaceQueryComponent = workspaceQueryComponent;
    this.executorService = Executors.newSingleThreadExecutor();

    this.bundleDefault = Internationalization.getInternationalizationBundle();

    this.colorBlack = Color.BLACK;
    this.colorBlue = (Color) bundleDefault.getObject("colorBlue");

    this.detailsControlPanel.getCount().addActionListener(new RadioListenerColumn());
    this.detailsControlPanel.getSum().addActionListener(new RadioListenerColumn());
    this.detailsControlPanel.getAverage().addActionListener(new RadioListenerColumn());

    this.jxTableCaseMetrics.getJxTable().addMouseListener(this);
    this.jxTableCaseColumns.getJxTable().addMouseListener(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    JXTable source = (JXTable) e.getSource();

    this.setSourceConfig(source);

    log.info("Source config: " + sourceConfig);
  }

  protected void setSourceConfig(JXTable source) {
    if (source.equals(jxTableCaseMetrics.getJxTable())) {
      sourceConfig = METRICS;
    }

    if (source.equals(jxTableCaseColumns.getJxTable())) {
      sourceConfig = COLUMNS;
    }

    log.info("Source config: " + sourceConfig);
  }

  protected void loadChart(ProcessType processType) {
    log.info("Load chart method call");
    this.chartJTabbedPane.setSelectedTab(processType);

    this.checkTimestampColumn();

    if (METRICS.equals(sourceConfig)) {
      this.loadChartMetric(processType);
    } else if (COLUMNS.equals(sourceConfig)) {
      this.loadChartColumn(processType);
    }

    log.info("Query: " + queryInfo.getName());
  }

  private void loadChartMetric(ProcessType processType) {

    Metric metric = getMetricFromUI();

    executorService.submit(() -> {
      GUIHelper.addToJSplitPane(getChartGanttPanel(processType),
          createProgressBar("Loading, please wait..."), JSplitPane.TOP, 250);

      try {
        loadChartByMetric(metric, processType);

        ProfileInfo profileInfo = profileManager.getProfileInfoByQueryId(queryInfo.getId());

        if (profileInfo.getStatus().equals(RunStatus.NOT_RUNNING)) {
          ChartRange chartRange = getRange(chartInfo);
          eventListener.fireOnShowHistory(queryInfo, metric.getYAxis(), chartRange.getBegin(), chartRange.getEnd());
        }

      } catch (Exception exception) {
        log.catching(exception);
        throw new RuntimeException(exception);
      }
    });
  }

  protected Metric getMetricFromUI() {
    int metricId = GUIHelper.getIdByColumnName(jxTableCaseMetrics.getJxTable(),
        jxTableCaseMetrics.getDefaultTableModel(),
        jxTableCaseMetrics.getJxTable().getSelectionModel(),
        MetricsColumnNames.ID.getColName());

    log.info("Metric id: " + metricId);

    return queryInfo.getMetricList()
        .stream().filter(f -> f.getId() == metricId)
        .findAny()
        .orElseThrow(() -> new NotFoundException("Not found metric by id: " + metricId));
  }

  private void loadChartColumn(ProcessType processType) {
    CProfile cProfile = getCProfileFromUI();

    executorService.submit(() -> {
      GUIHelper.addToJSplitPane(getChartGanttPanel(processType),
              createProgressBar("Loading, please wait..."), JSplitPane.TOP, 250);

      try {
        Metric metric = getMetricByCProfile(cProfile);

        setMetricFunctionAndChartOnEdit(metric);

        loadChartByMetric(metric, processType);

        ProfileInfo profileInfo = profileManager.getProfileInfoByQueryId(queryInfo.getId());

        if (profileInfo.getStatus().equals(RunStatus.NOT_RUNNING)) {
          ChartRange chartRange = getRange(chartInfo);
          eventListener.fireOnShowHistory(queryInfo, cProfile, chartRange.getBegin(), chartRange.getEnd());
        }

      } catch (Exception exception) {
        log.catching(exception);
        throw new RuntimeException(exception);
      }
    });
  }

  protected CProfile getCProfileFromUI() {
    int cProfileId = GUIHelper.getIdByColumnName(jxTableCaseColumns.getJxTable(),
        jxTableCaseColumns.getDefaultTableModel(),
        jxTableCaseColumns.getJxTable().getSelectionModel(),
        ProfileColumnNames.ID.getColName());

    log.info("Column profile id: " + cProfileId);

    return tableInfo.getCProfiles()
        .stream()
        .filter(f -> f.getColId() == cProfileId)
        .findAny()
        .orElseThrow(() -> new NotFoundException("Not found CProfile: " + cProfileId));
  }

  protected Metric getMetricByCProfile(CProfile cProfile) {
    Metric metric = new Metric();
    metric.setXAxis(tableInfo.getCProfiles().stream().filter(f -> f.getCsType().isTimeStamp()).findAny().orElseThrow());
    metric.setYAxis(cProfile);
    metric.setGroup(cProfile);

    if (MetricFunction.NONE.equals(metricFunctionOnEdit)) {
      setMetricFunctionAndChartNotEdit(cProfile, metric);
    } else {
      setMetricFunctionAndChartOnEdit(metric);
    }

    return metric;
  }

  private void setMetricFunctionAndChartOnEdit(Metric metric) {
    switch (metricFunctionOnEdit) {
      case COUNT -> {
        metric.setMetricFunction(metricFunctionOnEdit);
        metric.setChartType(STACKED);
      }
      case SUM, AVERAGE -> {
        metric.setMetricFunction(metricFunctionOnEdit);
        metric.setChartType(LINEAR);
      }
    }
  }

  private void setMetricFunctionAndChartNotEdit(CProfile cProfile, Metric metric) {
    if (CType.STRING.equals(cProfile.getCsType().getCType())) {
      metric.setMetricFunction(MetricFunction.COUNT);
      metric.setChartType(STACKED);
    } else {
      if (Arrays.stream(TimestampType.values()).anyMatch((t) -> t.name().equals(cProfile.getColDbTypeName()))) {
        metric.setMetricFunction(MetricFunction.COUNT);
        metric.setChartType(STACKED);
      } else {
        metric.setMetricFunction(MetricFunction.AVERAGE);
        metric.setChartType(LINEAR);
      }
    }
  }

  private void loadChartByMetric(Metric metric, ProcessType processType) {
    if (metric.isStackedYAxisSameCount()) {
      StackChartPanel stackChartPanel = getStackChartPanel(metric, processType);
      DetailPanel detailPanel = getDetailPanel(metric.getYAxis(), stackChartPanel.getSeriesColorMap(),
              stackChartPanel, processType, metric.getChartType());

      GUIHelper.addToJSplitPane(getChartGanttPanel(processType), stackChartPanel, JSplitPane.TOP, 250);
      GUIHelper.addToJSplitPane(getChartGanttPanel(processType), detailPanel, JSplitPane.BOTTOM, 250);
    }

    if (metric.isLinearYAxisSameSum() | metric.isLinearYAxisAvg()) {
      StackChartPanel stackChartPanel = getStackChartPanel(metric, processType);
      DetailPanel detailPanel = getDetailPanel(metric.getYAxis(), stackChartPanel.getSeriesColorMap(),
              stackChartPanel, processType, metric.getChartType());

      GUIHelper.addToJSplitPane(getChartGanttPanel(processType), stackChartPanel, JSplitPane.TOP, 250);
      GUIHelper.addToJSplitPane(getChartGanttPanel(processType), detailPanel, JSplitPane.BOTTOM, 250);
    }
  }

  protected DetailPanel getDetailPanel(CProfile cProfile, Map<String, Color> seriesColorMap,
      DetailChart dynamicChart, ProcessType processType, ChartType chartType) {
    DetailPanel detailPanel =
        new DetailPanel(workspaceQueryComponent, queryInfo, tableInfo, cProfile,
            seriesColorMap, processType, chartType);

    dynamicChart.addChartListenerReleaseMouse(detailPanel);

    return detailPanel;
  }

  protected StackChartPanel getStackChartPanel(Metric metric, ProcessType processType) {
    CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

    StackChartPanel stackChartPanel = null;

    if (REAL_TIME.equals(processType)) {
      this.eventListener.clearListenerByKey(profileTaskQueryKey);

      if (GatherDataSql.BY_CLIENT.equals(queryInfo.getGatherDataSql())) {
        stackChartPanel =
            new ClientRealTimeSCP(workspaceQueryComponent, categoryTableXYDatasetRealTime,
                profileTaskQueryKey,
                queryInfo, chartInfo, processType, metric);
      } else if (GatherDataSql.BY_SERVER.equals(queryInfo.getGatherDataSql())) {
        stackChartPanel =
            new ServerRealTimeSCP(workspaceQueryComponent, categoryTableXYDatasetRealTime,
                profileTaskQueryKey,
                queryInfo, chartInfo, processType, metric);
      }

      assert stackChartPanel != null;
      stackChartPanel.initialize();

    } else if (ProcessType.HISTORY.equals(processType)) {
      if (GatherDataSql.BY_CLIENT.equals(queryInfo.getGatherDataSql())) {
        stackChartPanel =
            new ClientHistorySCP(workspaceQueryComponent, categoryTableXYDatasetRealTime, profileTaskQueryKey,
                queryInfo, chartInfo, processType, metric);
      } else if (GatherDataSql.BY_SERVER.equals(queryInfo.getGatherDataSql())) {
        stackChartPanel =
            new ServerHistorySCP(workspaceQueryComponent, categoryTableXYDatasetRealTime, profileTaskQueryKey,
                queryInfo, chartInfo, processType, metric);
      }

      assert stackChartPanel != null;
      stackChartPanel.initialize();
    }

    return stackChartPanel;
  }

  protected StackChartPanel getOneStackChartPanel(Metric metric, ProcessType processType) {
    CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

    StackChartPanel oneStackChartPanel = null;

    if (REAL_TIME.equals(processType)) {
      this.eventListener.clearListenerByKey(profileTaskQueryKey);
      oneStackChartPanel = new ServerClientRealTimeOneSCP(workspaceQueryComponent,
          categoryTableXYDatasetRealTime,
          profileTaskQueryKey, queryInfo, chartInfo, processType, metric);
    } else if (ProcessType.HISTORY.equals(processType)) {
      oneStackChartPanel = new ServerClientHistoryOneSCP(workspaceQueryComponent,
          categoryTableXYDatasetRealTime,
          profileTaskQueryKey, queryInfo, chartInfo, processType, metric);
    }

    assert oneStackChartPanel != null;
    oneStackChartPanel.initialize();

    return oneStackChartPanel;
  }


  private void checkTimestampColumn() {
    if (Objects.isNull(tableInfo.getCProfiles())) {
      throw new NotFoundException("Metadata not found, need to reload query metadata in configuration");
    }

    tableInfo.getCProfiles().stream()
        .filter(f -> f.getCsType().isTimeStamp())
        .findAny()
        .orElseThrow(
            () -> new NotFoundException("Not found column timestamp for table: " + tableInfo.getTableName()));
  }

  private JSplitPane getChartGanttPanel(ProcessType processType) {
    if (REAL_TIME.equals(processType)) {
      return chartGanttPanelRealTime;
    } else if (HISTORY.equals(processType)) {
      return chartGanttPanelHistory;
    } else {
      return new JSplitPane();
    }
  }

  private class RadioListenerColumn implements ActionListener {

    public RadioListenerColumn() {}

    public void actionPerformed(ActionEvent e) {
      JRadioButton button = (JRadioButton) e.getSource();

      switch (button.getText()) {
        case "Count" -> metricFunctionOnEdit = MetricFunction.COUNT;
        case "Sum" -> metricFunctionOnEdit = MetricFunction.SUM;
        case "Average" -> metricFunctionOnEdit = MetricFunction.AVERAGE;
      }
    }
  }

}
