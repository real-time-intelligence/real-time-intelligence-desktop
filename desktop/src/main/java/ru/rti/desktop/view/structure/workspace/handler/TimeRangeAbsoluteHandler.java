package ru.rti.desktop.view.structure.workspace.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JSplitPane;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.info.gui.RangeInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.model.view.RangeChartHistory;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.panel.TimeRangeAbsolutePanel;

@Log4j2
public class TimeRangeAbsoluteHandler extends ChartHandler implements ActionListener {
    private final TimeRangeAbsolutePanel timeRangeAbsolutePanel;

    private LocalDateTime begin;
    private LocalDateTime end;

    @Inject
    @Named("eventListener")
    EventListener eventListener;

    @Inject
    @Named("appCache")
    AppCache appCache;

    public TimeRangeAbsoluteHandler(JXTableCase jxTableCaseMetrics,
                                    JXTableCase jxTableCaseColumns,
                                    TimeRangeAbsolutePanel timeRangeAbsolutePanel,
                                    ChartJTabbedPane chartJTabbedPane,
                                    ProfileTaskQueryKey profileTaskQueryKey,
                                    JSplitPane chartGanttPanelRealTime,
                                    JSplitPane chartGanttPanelHistory,
                                    QueryInfo queryInfo,
                                    TableInfo tableInfo,
                                    ChartInfo chartInfo,
                                    WorkspaceQueryComponent workspaceQueryComponent) {
        super(chartJTabbedPane, jxTableCaseMetrics, jxTableCaseColumns, tableInfo, queryInfo, chartInfo,
            profileTaskQueryKey, chartGanttPanelRealTime, chartGanttPanelHistory, workspaceQueryComponent);

        this.timeRangeAbsolutePanel = timeRangeAbsolutePanel;

        this.begin = LocalDateTime.now();
        this.end = LocalDateTime.now();

        this.timeRangeAbsolutePanel.getJButtonFrom().addActionListener(this);
        this.timeRangeAbsolutePanel.getJButtonTo().addActionListener(this);
        this.timeRangeAbsolutePanel.getJButtonGo().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timeRangeAbsolutePanel.getJButtonFrom()) {
            timeRangeAbsolutePanel.getDatePickerFrom().setDate(LocalDate.now());
        }

        if (e.getSource() == timeRangeAbsolutePanel.getJButtonTo()) {
            timeRangeAbsolutePanel.getDatePickerTo().setDate(LocalDate.now());
        }

        if (e.getSource() == timeRangeAbsolutePanel.getJButtonGo()) {

            chartJTabbedPane.setSelectedTab(ProcessType.HISTORY);
            chartInfo.setRangeHistory(RangeChartHistory.CUSTOM);

            LocalDate dateBegin = timeRangeAbsolutePanel.getDatePickerFrom().getDate();
            LocalDate dateEnd = timeRangeAbsolutePanel.getDatePickerTo().getDate();

            LocalTime startOfDay = LocalTime.MIN;
            LocalTime endOfDay = LocalTime.MAX;

            begin = LocalDateTime.of(dateBegin, startOfDay);
            end = LocalDateTime.of(dateEnd, endOfDay);

            this.chartInfo.setCustomBegin(begin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            this.chartInfo.setCustomEnd(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            this.loadChart(ProcessType.HISTORY);

            RangeInfo rangeInfo = new RangeInfo();
            long selectionIndex = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            rangeInfo.setCreatedAt(selectionIndex);
            rangeInfo.setBegin(begin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            rangeInfo.setEnd(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            rangeInfo.setSourceTab("A");

            appCache.putRangeInfo(profileTaskQueryKey, rangeInfo);

            eventListener.fireOnAddToAppCache(profileTaskQueryKey);
        }
    }

}