package ru.rti.desktop.view.structure.workspace.handler;

import static ru.rti.desktop.model.view.RangeChartRealTime.FIVE_MIN;
import static ru.rti.desktop.model.view.RangeChartRealTime.SIXTY_MIN;
import static ru.rti.desktop.model.view.RangeChartRealTime.TEN_MIN;
import static ru.rti.desktop.model.view.RangeChartRealTime.THIRTY_MIN;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JSplitPane;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.info.gui.RangeInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.model.view.RangeChartHistory;
import ru.rti.desktop.model.view.RangeChartRealTime;
import ru.rti.desktop.view.chart.HelperChart;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.panel.RangeChartHistoryPanel;
import ru.rti.desktop.view.panel.RangeChartRealTimePanel;
import ru.rti.desktop.view.structure.workspace.query.CustomHistoryPanel;

@Log4j2
public class TimeRangeQuickHandler extends ChartHandler
    implements ActionListener, HelperChart {

    private final RangeChartRealTimePanel rangeChartRealTimePanel;
    private final RangeChartHistoryPanel rangeChartHistoryPanel;

    private final CustomHistoryPanel customHistoryPanel;

    @Inject
    @Named("appCache")
    AppCache appCache;

    public TimeRangeQuickHandler(JXTableCase jxTableCaseMetrics,
                                 JXTableCase jxTableCaseColumns,
                                 RangeChartRealTimePanel rangeChartRealTimePanel,
                                 RangeChartHistoryPanel rangeChartHistoryPanel,
                                 JSplitPane chartGanttPanelRealTime,
                                 JSplitPane chartGanttPanelHistory,
                                 ChartJTabbedPane chartJTabbedPane,
                                 CustomHistoryPanel customHistoryPanel,
                                 ProfileTaskQueryKey profileTaskQueryKey,
                                 QueryInfo queryInfo,
                                 TableInfo tableInfo,
                                 ChartInfo chartInfo,
                                 WorkspaceQueryComponent workspaceQueryComponent) {
        super(chartJTabbedPane, jxTableCaseMetrics, jxTableCaseColumns, tableInfo, queryInfo, chartInfo,
            profileTaskQueryKey, chartGanttPanelRealTime, chartGanttPanelHistory, workspaceQueryComponent);

        this.customHistoryPanel = customHistoryPanel;

        this.rangeChartRealTimePanel = rangeChartRealTimePanel;
        this.rangeChartRealTimePanel.addActionListener(this);
        this.rangeChartHistoryPanel = rangeChartHistoryPanel;
        this.rangeChartHistoryPanel.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.info("Action command here: " + e.getActionCommand());
        String name = e.getActionCommand().replace("Last ", "");

        if (Stream.of(RangeChartRealTime.values()).anyMatch(v -> v.getName().equals(name))) {

            if (FIVE_MIN.getName().equalsIgnoreCase(name)) {
                this.chartInfo.setRange(5);
                colorButton(5);
            }
            if (TEN_MIN.getName().equalsIgnoreCase(name)) {
                this.chartInfo.setRange(10);
                colorButton(10);
            }
            if (THIRTY_MIN.getName().equalsIgnoreCase(name)) {
                this.chartInfo.setRange(30);
                colorButton(30);
            }
            if (SIXTY_MIN.getName().equalsIgnoreCase(name)) {
                this.chartInfo.setRange(60);
                colorButton(60);
            }

            this.loadChart(ProcessType.REAL_TIME);
        }

        Stream.of(RangeChartHistory.values())
                .filter(v -> v.getName().equals(name))
                .forEach(this.chartInfo::setRangeHistory);

        if (Stream.of(RangeChartHistory.values()).anyMatch(v -> v.getName().equals(name))) {
            chartJTabbedPane.setSelectedTab(ProcessType.HISTORY);
            colorButtonHistory(name);

            if (name.equals(RangeChartHistory.CUSTOM.getName())) {
                customHistoryPanel.setVisible(true);
                return;
            }

            ChartRange chartRange = getRange(chartInfo);
            long begin = chartRange.getBegin();
            long end = chartRange.getEnd();

            RangeInfo rangeInfo = new RangeInfo();
            long selectionIndex = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            rangeInfo.setCreatedAt(selectionIndex);
            rangeInfo.setBegin(begin);
            rangeInfo.setEnd(end);
            rangeInfo.setSourceTab("Q");

            appCache.putRangeInfo(profileTaskQueryKey, rangeInfo);

            eventListener.fireOnAddToAppCache(profileTaskQueryKey);

            this.loadChart(ProcessType.HISTORY);
        }
    }

    private void colorButtonHistory(String name) {
        switch (name) {
            case "Day" -> this.rangeChartHistoryPanel.setButtonColor(colorBlack, colorBlue, colorBlue, colorBlue);
            case "Week" -> this.rangeChartHistoryPanel.setButtonColor(colorBlue, colorBlack, colorBlue, colorBlue);
            case "Month" -> this.rangeChartHistoryPanel.setButtonColor(colorBlue, colorBlue, colorBlack, colorBlue);
            case "Custom" -> this.rangeChartHistoryPanel.setButtonColor(colorBlue, colorBlue, colorBlue, colorBlack);
        }
    }

    private void colorButton(int numMin) {
        switch (numMin) {
            case 5 -> this.rangeChartRealTimePanel.setButtonColor(colorBlack, colorBlue, colorBlue, colorBlue);
            case 10 -> this.rangeChartRealTimePanel.setButtonColor(colorBlue, colorBlack, colorBlue, colorBlue);
            case 30 -> this.rangeChartRealTimePanel.setButtonColor(colorBlue, colorBlue, colorBlack, colorBlue);
            case 60 -> this.rangeChartRealTimePanel.setButtonColor(colorBlue, colorBlue, colorBlue, colorBlack);
        }
    }
}
