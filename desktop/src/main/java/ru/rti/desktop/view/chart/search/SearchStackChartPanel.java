package ru.rti.desktop.view.chart.search;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.IDetailPanel;
import ru.rti.desktop.config.prototype.chart.WorkspaceChartModule;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.CollectStartStopListener;
import ru.rti.desktop.router.listener.ShowLocalHistoryListener;
import ru.rti.desktop.state.SqlQueryState;
import ru.rti.desktop.view.chart.DetailChart;
import ru.rti.desktop.view.chart.HelperChart;
import ru.rti.desktop.view.chart.StackedChart;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Log4j2
public abstract class SearchStackChartPanel extends JPanel implements DetailChart, HelperChart,
        CollectStartStopListener, ShowLocalHistoryListener {

    protected static int MAX_POINT_PER_GRAPH = 300;

    protected StackedChart stackedChart;
    private ChartPanel chartPanel;
    private JFreeChart jFreeChart;
    private DateAxis dateAxis;

    private String name;
    private String xAxisLabel = "xAxisLabel";
    private String yAxisLabel = "Value";
    protected int legendFontSize = 12;

    protected Set<String> series;

    private final WorkspaceQueryComponent workspaceQueryComponent;

    protected final CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime;

    protected final ProfileTaskQueryKey profileTaskQueryKey;
    protected final QueryInfo queryInfo;
    protected final TableInfo tableInfo;
    protected final ChartInfo chartInfo;
    protected final ProcessType processType;

    @Inject
    @Named("eventListener")
    EventListener eventListener;

    @Inject
    @Named("localDB")
    FStore fStore;

    @Inject
    @Named("sqlQueryState")
    SqlQueryState sqlQueryState;

    public SearchStackChartPanel(WorkspaceQueryComponent workspaceQueryComponent,
                                 CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime,
                                 ProfileTaskQueryKey profileTaskQueryKey,
                                 QueryInfo queryInfo,
                                 TableInfo tableInfo,
                                 ChartInfo chartInfo,
                                 ProcessType processType) {
        this.workspaceQueryComponent = workspaceQueryComponent;
        this.workspaceQueryComponent.initChart(new WorkspaceChartModule(this)).inject(this);

        this.categoryTableXYDatasetRealTime = categoryTableXYDatasetRealTime;
        this.profileTaskQueryKey = profileTaskQueryKey;
        this.queryInfo = queryInfo;
        this.tableInfo = tableInfo;
        this.chartInfo = chartInfo;
        this.processType = processType;

        this.series = new LinkedHashSet<>();

        this.stackedChart = new StackedChart(getChartPanel(this.categoryTableXYDatasetRealTime));
        this.stackedChart.setLegendFontSize(legendFontSize);
        this.stackedChart.initialize();

        this.setLayout(new BorderLayout());
    }

    public abstract void initialize();

    protected void initializeSearch() {
        this.setLayout(new BorderLayout());
        this.add("Center", stackedChart.getChartPanel());
    }

    protected abstract void loadData();

    public Map<String, Color> getSeriesColorMap() {
        return stackedChart.getSeriesColorMap();
    }

    protected ChartPanel getChartPanel(CategoryTableXYDatasetRealTime categoryTableXYDataset) {
        xAxisLabel = "Query: " + queryInfo.getName();
        dateAxis = new DateAxis();
        jFreeChart = ChartFactory.createStackedXYAreaChart("", xAxisLabel, yAxisLabel, categoryTableXYDataset,
                PlotOrientation.VERTICAL, dateAxis, false, true, false);
        chartPanel = new ChartPanel(jFreeChart);

        return chartPanel;
    }

    @Override
    public void fireOnStartCollect(ProfileTaskQueryKey profileTaskQueryKey) {
        log.info("fireOnStartCollect for profileTaskQueryKey: " + profileTaskQueryKey);
    }

    @Override
    public void fireOnStopCollect(ProfileTaskQueryKey profileTaskQueryKey) {
        log.info("fireOnStopCollect for profileTaskQueryKey:" + profileTaskQueryKey);
    }

    @Override
    public void addChartListenerReleaseMouse(IDetailPanel l) {
        stackedChart.addChartListenerReleaseMouse(l);
    }

    @Override
    public void removeChartListenerReleaseMouse(IDetailPanel l) {
        stackedChart.removeChartListenerReleaseMouse(l);
    }

    @Override
    public void fireOnShowHistory(QueryInfo queryInfo, CProfile cProfile, long begin, long end) {
        log.info("fireOnShowHistory " + cProfile);
    }
}
