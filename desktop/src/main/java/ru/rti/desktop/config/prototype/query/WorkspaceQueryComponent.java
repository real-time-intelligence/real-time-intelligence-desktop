package ru.rti.desktop.config.prototype.query;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceQueryScope;
import ru.rti.desktop.config.prototype.chart.WorkspaceChartComponent;
import ru.rti.desktop.config.prototype.chart.WorkspaceChartModule;
import ru.rti.desktop.config.prototype.detail.WorkspaceDetailComponent;
import ru.rti.desktop.config.prototype.detail.WorkspaceDetailModule;
import ru.rti.desktop.config.prototype.detail.WorkspaceGanttComponent;
import ru.rti.desktop.config.prototype.detail.WorkspaceGanttModule;
import ru.rti.desktop.config.prototype.detail.WorkspaceRawComponent;
import ru.rti.desktop.config.prototype.detail.WorkspaceRawModule;
import ru.rti.desktop.view.structure.workspace.handler.*;
import ru.rti.desktop.view.structure.workspace.query.WorkspaceQueryView;

@WorkspaceQueryScope
@Subcomponent(modules = WorkspaceQueryModule.class)
public interface WorkspaceQueryComponent {
  void inject(WorkspaceQueryView workspaceQueryView);
  void injectMetricColumnSelectionHandler(MetricColumnSelectionHandler metricColumnSelectionHandler);
  void injectTimeRangeQuickHandler(TimeRangeQuickHandler timeRangeQuickHandler);
  void injectTimeRangeRelativeHandler(TimeRangeRelativeHandler timeRangeRelativeHandler);
  void injectTimeRangeAbsoluteHandler(TimeRangeAbsoluteHandler timeRangeAbsoluteHandler);
  void injectTimeRangeRecentHandler(TimeRangeRecentHandler timeRangeRecentHandler);
  void injectSearchHandler(QuerySearchHandler querySearchHandler);

  WorkspaceChartComponent initChart(WorkspaceChartModule workspaceChartModule);
  WorkspaceDetailComponent initDetail(WorkspaceDetailModule workspaceDetailModule);
  WorkspaceRawComponent initRaw(WorkspaceRawModule workspaceRawModule);
  WorkspaceGanttComponent initGantt(WorkspaceGanttModule workspaceGanttModule);
}