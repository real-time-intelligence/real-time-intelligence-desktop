package ru.rti.desktop.config.prototype.chart;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceChartScope;
import ru.rti.desktop.view.chart.search.SearchStackChartPanel;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;

@WorkspaceChartScope
@Subcomponent(modules = WorkspaceChartModule.class)
public interface WorkspaceChartComponent {
  void inject(StackChartPanel stackChartPanel);
  void inject(SearchStackChartPanel searchStackChartPanel);
}