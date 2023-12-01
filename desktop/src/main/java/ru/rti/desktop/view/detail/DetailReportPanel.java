package ru.rti.desktop.view.detail;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.jfree.chart.util.IDetailPanel;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.view.detail.gantt.ReportGanttPanel;

@Log4j2
public class DetailReportPanel extends JPanel implements IDetailPanel {

    private JPanel mainPanel;

    private final QueryInfo queryInfo;
    private final TableInfo tableInfo;
    private final ChartType chartType;

    private final Metric metric;
    private final ExecutorService executorService;

    private final FStore fStore;

    private final Map<String, Color> seriesColorMap;

    public DetailReportPanel(FStore fStore,
                             QueryInfo queryInfo,
                             TableInfo tableInfo,
                             Metric metric,
                             Map<String, Color> seriesColorMap,
                             ChartType chartType) {

        this.fStore = fStore;
        this.queryInfo = queryInfo;
        this.tableInfo = tableInfo;
        this.metric = metric;
        this.seriesColorMap = seriesColorMap;
        this.chartType = chartType;

        this.executorService = Executors.newSingleThreadExecutor();

        this.setLayout(new BorderLayout());

        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new GridLayout(1, 1, 3, 3));

        this.add(this.mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void loadDataToDetail(long begin, long end) {
        executorService.submit(() -> {

            mainPanel.removeAll();
            mainPanel.add(createProgressBar("Loading, please wait..."));
            mainPanel.repaint();
            mainPanel.revalidate();

            try {
                JTabbedPane mainJTabbedPane = new JTabbedPane();

                if (ChartType.STACKED.equals(chartType)) {
                    ReportGanttPanel ganttDataPanel = new ReportGanttPanel(fStore, tableInfo, metric.getYAxis(), begin, end, seriesColorMap);
                    mainJTabbedPane.add("Gantt", ganttDataPanel);
                }

                RawDataReportPanel rawDataPanel = new RawDataReportPanel(fStore, queryInfo, tableInfo, metric.getYAxis(), begin, end, true);
                mainJTabbedPane.add("Raw", rawDataPanel);

                mainPanel.removeAll();
                mainPanel.repaint();
                mainPanel.revalidate();

                mainPanel.add(mainJTabbedPane);

            } catch (Exception exception) {
                log.catching(exception);
            }

        });
    }

}
