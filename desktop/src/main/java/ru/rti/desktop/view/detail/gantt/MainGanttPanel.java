package ru.rti.desktop.view.detail.gantt;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.output.GanttColumn;
import org.fbase.model.profile.CProfile;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.config.prototype.detail.WorkspaceGanttModule;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.gantt.DrawingScale;
import ru.rti.desktop.model.info.TableInfo;

@Log4j2
public class MainGanttPanel extends GanttPanel implements ListSelectionListener {

    private final WorkspaceQueryComponent workspaceQueryComponent;

    @Inject
    @Named("localDB")
    FStore fStore;

    @Inject
    @Named("executorService")
    ScheduledExecutorService executorService;

    public MainGanttPanel(WorkspaceQueryComponent workspaceQueryComponent,
                          TableInfo tableInfo,
                          CProfile cProfile,
                          long begin,
                          long end,
                          Map<String, Color> seriesColorMap) {
        super(tableInfo, cProfile, begin, end, seriesColorMap);

        super.jxTableCase.getJxTable().getSelectionModel().addListSelectionListener(this);

        this.workspaceQueryComponent = workspaceQueryComponent;
        this.workspaceQueryComponent.initGantt(new WorkspaceGanttModule(this)).inject(this);

        this.jSplitPane.add(this.jxTableCase.getJScrollPane(), JSplitPane.LEFT);
        this.jSplitPane.add(this.valuableGantt(), JSplitPane.RIGHT);

        this.setLayout(new BorderLayout());
        this.add(this.jSplitPane, BorderLayout.CENTER);
    }

    private JPanel valuableGantt() {
        JPanel jPanel = new JPanel();
        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gbl.row()
            .cell(new JXTitledSeparator("Valuable")).fillX();

        gbl.row()
            .cellXYRemainder(loadValuableGantt()).fillXY();

        gbl.done();

        return jPanel;
    }

    protected JScrollPane loadValuableGantt() {
        final JPanel[] panel = {new JPanel()};

        List<JScrollPane> jScrollPaneList = new ArrayList<>();

        try {
            tableInfo.getValuableColumnList().forEach(colName -> {
                try {
                    CProfile firstLevelGroupBy = tableInfo.getCProfiles().stream()
                        .filter(f -> f.getColName().equalsIgnoreCase(colName))
                        .findFirst()
                        .orElseThrow();

                    List<GanttColumn> ganttColumnList =
                        fStore.getGColumnListTwoLevelGroupBy(tableInfo.getTableName(), firstLevelGroupBy, cProfile, begin, end);

                    DrawingScale drawingScale = new DrawingScale();

                    JXTable jxTable = loadGantt(firstLevelGroupBy, ganttColumnList, seriesColorMap, drawingScale, 5, 20);
                    jScrollPaneList.add(getJScrollPane(jxTable));

                } catch (Exception exception) {
                    log.catching(exception);
                }
            });

            if (jScrollPaneList.size() != 0) {
                int columns = 3;
                int rows = (int) Math.ceil((double) jScrollPaneList.size() / columns);

                panel[0] = new JPanel(new GridLayout(rows, columns));

                for (JScrollPane pane : jScrollPaneList) {
                    panel[0].add(pane);
                }
            }

            panel[0].repaint();

        } catch (Exception exception) {
            log.catching(exception);
        }

        JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.setViewportView(panel[0]);
        scrollPane.setVerticalScrollBar(scrollPane.getVerticalScrollBar());

        return scrollPane;
    }


    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        // prevents double events
        if (!e.getValueIsAdjusting()) {

            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing query fields");
            } else {
                int columnId = GUIHelper.getIdByColumnName(jxTableCase.getJxTable(),
                    super.jxTableCase.getDefaultTableModel(), listSelectionModel, TaskColumnNames.ID.getColName());

                executorService.submit(() -> {
                    GUIHelper.addToJSplitPane(jSplitPane, createProgressBar("Loading, please wait..."),
                        JSplitPane.RIGHT, DIVIDER_LOCATION);

                    try {
                        CProfile firstLevelGroupBy = tableInfo.getCProfiles().stream()
                            .filter(f -> f.getColId() == columnId)
                            .findFirst()
                            .orElseThrow();

                        List<GanttColumn> ganttColumnList =
                            fStore.getGColumnListTwoLevelGroupBy(tableInfo.getTableName(),
                                firstLevelGroupBy, cProfile, begin, end);

                        DrawingScale drawingScale = new DrawingScale();

                        JXTable jxTable = loadGantt(firstLevelGroupBy, ganttColumnList, seriesColorMap, drawingScale, 100, 23);

                        GUIHelper.addToJSplitPane(jSplitPane, getJScrollPane(jxTable), JSplitPane.RIGHT, 200);

                    } catch (Exception exception) {
                        log.catching(exception);
                    }
                });

                log.info(columnId);
            }
        }
    }
}