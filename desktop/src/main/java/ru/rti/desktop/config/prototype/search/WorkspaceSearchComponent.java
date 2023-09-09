package ru.rti.desktop.config.prototype.search;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceChartScope;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;

@WorkspaceChartScope
@Subcomponent(modules = WorkspaceSearchModule.class)
public interface WorkspaceSearchComponent {
  void inject(StackChartPanel stackChartPanel);
}