package ru.rti.desktop.view.structure.workspace.handler;

import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.exception.EmptySearchException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.DetailChart;
import ru.rti.desktop.view.chart.search.SearchSCP;
import ru.rti.desktop.view.detail.DetailPanel;
import ru.rti.desktop.view.panel.QuerySearchButtonPanel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;

@Log4j2
public class QuerySearchHandler implements ActionListener {
    private final QuerySearchButtonPanel querySearchButtonPanel;

    private LocalDateTime begin;
    private LocalDateTime end;

    private  JSplitPane chartPanelSearch;

    @Inject
    @Named("eventListener")
    EventListener eventListener;

    @Inject
    @Named("appCache")
    AppCache appCache;
    private final ProfileTaskQueryKey profileTaskQueryKey;
    private final QueryInfo queryInfo;
    private final TableInfo tableInfo;
    private final ChartInfo chartInfo;
    private final WorkspaceQueryComponent workspaceQueryComponent;

    private final ScheduledExecutorService executorService;

    public QuerySearchHandler(JSplitPane chartPanelSearch,
                              QuerySearchButtonPanel querySearchButtonPanel,
                              ProfileTaskQueryKey profileTaskQueryKey,
                              QueryInfo queryInfo,
                              TableInfo tableInfo,
                              ChartInfo chartInfo,
                              WorkspaceQueryComponent workspaceQueryComponent) {
        this.profileTaskQueryKey = profileTaskQueryKey;
        this.queryInfo = queryInfo;
        this.tableInfo = tableInfo;
        this.chartInfo = chartInfo;
        this.workspaceQueryComponent = workspaceQueryComponent;

        this.begin = LocalDateTime.now();
        this.end = LocalDateTime.now();

        this.executorService = Executors.newScheduledThreadPool(1);

        this.chartPanelSearch = chartPanelSearch;

        GUIHelper.addToJSplitPane(this.chartPanelSearch, querySearchButtonPanel, JSplitPane.TOP, 40);
        GUIHelper.addToJSplitPane(this.chartPanelSearch, new JPanel(), JSplitPane.BOTTOM, 40);

        this.querySearchButtonPanel = querySearchButtonPanel;
        this.querySearchButtonPanel.getJButtonSearch().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Go button for ad-hoc search has been pressed..");

        String searchString = querySearchButtonPanel.getJTextFieldSearch().getText();

        if (searchString.length() < 3) {
            throw new EmptySearchException("The search string must not be empty or have a length greater than 3");
        }

        executorService.submit(() -> {
            GUIHelper.addToJSplitPane(chartPanelSearch, createProgressBar("Loading, please wait..."), JSplitPane.BOTTOM, 40);

            JSplitPane chartRawPanelSearch = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 5, 200);

            CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

            SearchSCP stackChartPanel = new SearchSCP(workspaceQueryComponent, categoryTableXYDatasetRealTime,
                    profileTaskQueryKey, queryInfo, tableInfo, chartInfo, ProcessType.SEARCH, searchString);
            stackChartPanel.initialize();
            stackChartPanel.loadData();

            DetailPanel detailPanel = getDetailPanel(null, stackChartPanel.getSeriesColorMap(),
                    stackChartPanel, ProcessType.SEARCH, ChartType.NONE);

            chartRawPanelSearch.add(stackChartPanel, JSplitPane.TOP);
            chartRawPanelSearch.add(detailPanel, JSplitPane.BOTTOM);
            chartRawPanelSearch.setDividerLocation(210);

            GUIHelper.addToJSplitPane(chartPanelSearch, chartRawPanelSearch, JSplitPane.BOTTOM, 40);
        });

    }

    protected DetailPanel getDetailPanel(CProfile cProfile, Map<String, Color> seriesColorMap,
                                         DetailChart dynamicChart, ProcessType processType, ChartType chartType) {
        DetailPanel detailPanel =
                new DetailPanel(workspaceQueryComponent, queryInfo, tableInfo, cProfile,
                        seriesColorMap, processType, chartType);

        dynamicChart.addChartListenerReleaseMouse(detailPanel);

        return detailPanel;
    }
}