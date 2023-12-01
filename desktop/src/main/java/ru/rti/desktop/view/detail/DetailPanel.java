package ru.rti.desktop.view.detail;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import org.jfree.chart.util.IDetailPanel;
import ru.rti.desktop.config.prototype.detail.WorkspaceDetailModule;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.view.ProcessType;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.detail.gantt.MainGanttPanel;

@Log4j2
public class DetailPanel extends JPanel implements IDetailPanel {

  private JPanel mainPanel;

  private final QueryInfo queryInfo;
  private final TableInfo tableInfo;

  private final ProcessType processType;

  private final ChartType chartType;

  private final CProfile cProfile;
  private final ExecutorService executorService;

  private final WorkspaceQueryComponent workspaceQueryComponent;

  @Inject
  @Named("eventListener")
  EventListener eventListener;

  private final Map<String, Color> seriesColorMap;

  public DetailPanel(WorkspaceQueryComponent workspaceQueryComponent,
                     QueryInfo queryInfo,
                     TableInfo tableInfo,
                     CProfile cProfile,
                     Map<String, Color> seriesColorMap,
                     ProcessType processType,
                     ChartType chartType) {
    this.workspaceQueryComponent = workspaceQueryComponent;
    this.workspaceQueryComponent.initDetail(new WorkspaceDetailModule(this)).inject(this);

    this.queryInfo = queryInfo;
    this.tableInfo = tableInfo;
    this.cProfile = cProfile;
    this.seriesColorMap = seriesColorMap;
    this.processType = processType;
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
          MainGanttPanel mainGanttPanel = new MainGanttPanel(workspaceQueryComponent, tableInfo, cProfile, begin, end, seriesColorMap);
          mainJTabbedPane.add("Gantt", mainGanttPanel);
        }

        RawDataPanel rawDataPanel;
        if (ProcessType.REAL_TIME.equals(processType)) {
          rawDataPanel = new RawDataPanel(workspaceQueryComponent, queryInfo, tableInfo, cProfile, begin, end, false);
        } else {
          rawDataPanel = new RawDataPanel(workspaceQueryComponent, queryInfo, tableInfo, cProfile, begin, end, true);
        }
        mainJTabbedPane.add("Raw", rawDataPanel);

        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();

        mainPanel.add(mainJTabbedPane);

      } catch (Exception exception){
        log.catching(exception);
      }

    });
  }
}
