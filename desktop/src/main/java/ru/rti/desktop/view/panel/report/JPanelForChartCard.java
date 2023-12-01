package ru.rti.desktop.view.panel.report;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import org.fbase.model.profile.cstype.CType;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.SourceConfig;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.db.TimestampType;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.report.QueryReportData;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.DetailChart;
import ru.rti.desktop.view.chart.report.ClientHistorySCRP;
import ru.rti.desktop.view.chart.report.ServerClientHistoryOneSCRP;
import ru.rti.desktop.view.chart.report.ServerHistorySCRP;
import ru.rti.desktop.view.chart.report.StackChartReportPanel;
import ru.rti.desktop.view.detail.DetailReportPanel;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;
import static ru.rti.desktop.model.SourceConfig.COLUMNS;
import static ru.rti.desktop.model.SourceConfig.METRICS;
import static ru.rti.desktop.model.function.ChartType.*;

@Data
@Log4j2
public class JPanelForChartCard extends JPanel {
    @EqualsAndHashCode.Include
    private final ProfileTaskQueryKey key;
    private final JXLabel jlTitle;
    private final JXLabel jlYAxis;
    private final JTextField jtfYAxis;
    private final JXLabel jlGroup;
    private final JTextField jtfGroup;
    private final JXLabel jlChartType;
    private final JTextField jtfChartType;
    private final JXTextArea jtaDescription;
    private final JSplitPane jSplitPane;

    private final JRadioButton asIs;
    private final JRadioButton count;
    private final JRadioButton sum;
    private final JRadioButton average;
    private final ButtonGroup buttonGroupFunction;
    private final JPanel jPanelFunction;
    protected MetricFunction metricFunctionOnEdit = MetricFunction.NONE;
    private final Metric metric;

    protected ExecutorService executorService;
    protected final ProfileManager profileManager;
    protected final EventListener eventListener;
    protected final FStore fStore;
    private final ReportHelper reportHelper;

    private int id;
    private ChartInfo chartInfo;
    private SourceConfig sourceConfig;

    private final Map<ProfileTaskQueryKey, QueryReportData> mapReportData;
    private String descriptionFrom;
    private String descriptionTo;

    public JPanelForChartCard(int id,
                              ChartInfo chartInfo,
                              ProfileTaskQueryKey key,
                              SourceConfig sourceConfig,
                              Metric metric,
                              ProfileManager profileManager,
                              EventListener eventListener,
                              FStore fStore,
                              ReportHelper reportHelper,
                              Map<ProfileTaskQueryKey, QueryReportData> mapReportData) {

        this.profileManager = profileManager;
        this.eventListener = eventListener;
        this.fStore = fStore;
        this.executorService = Executors.newSingleThreadExecutor();
        this.reportHelper = reportHelper;
        this.mapReportData = mapReportData;

        this.id = id;
        this.key = key;
        this.chartInfo = chartInfo;
        this.sourceConfig = sourceConfig;

        this.metric = metric;

        this.jlTitle = new JXLabel(getTitle(sourceConfig));
        jlTitle.setForeground(new Color(0x0A8D0A));
        Font font = jlTitle.getFont();
        Font newFont = font.deriveFont(Font.PLAIN, 14);
        jlTitle.setFont(newFont);

        this.setToolTipText(getTooltip());

        this.jlYAxis = new JXLabel("Y Axis");
        jlYAxis.setForeground(Color.GRAY);

        this.jtfYAxis = new JTextField();
        jtfYAxis.setEnabled(false);

        this.jlGroup = new JXLabel("Group");
        jlGroup.setForeground(Color.GRAY);

        this.jtfGroup = new JTextField();
        jtfGroup.setEnabled(false);

        this.jlChartType = new JXLabel("Chart");
        jlChartType.setForeground(Color.GRAY);

        this.jtfChartType = new JTextField();
        jtfChartType.setEnabled(false);

        this.asIs = new JRadioButton(MetricFunction.ASIS.getName(), false);
        this.count = new JRadioButton(MetricFunction.COUNT.getName(), false);
        this.sum = new JRadioButton(MetricFunction.SUM.getName(), false);
        this.average = new JRadioButton(MetricFunction.AVERAGE.getName(), false);

        this.count.addActionListener(new RadioListenerColumn());
        this.sum.addActionListener(new RadioListenerColumn());
        this.average.addActionListener(new RadioListenerColumn());

        buttonGroupFunction = new ButtonGroup();
        buttonGroupFunction.add(asIs);
        buttonGroupFunction.add(count);
        buttonGroupFunction.add(sum);
        buttonGroupFunction.add(average);

        this.jtaDescription = GUIHelper.getJXTextArea(2, 1);
        this.jtaDescription.setPrompt("Enter a comment...");

        this.jSplitPane = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 250);

        JPanel bottom = new JPanel();
        JPanel top = new JPanel();
        jSplitPane.setBottomComponent(bottom);
        jSplitPane.setTopComponent(top);
        jSplitPane.setDividerLocation(250);

        jSplitPane.setResizeWeight(0.5);
        jSplitPane.setContinuousLayout(true);
        bottom.setMaximumSize(new Dimension(680, 100));
        bottom.setMinimumSize(new Dimension(680, 300));
        bottom.setPreferredSize(new Dimension(680, 200));

        top.setMaximumSize(new Dimension(680, 100));
        top.setMinimumSize(new Dimension(680, 300));
        top.setPreferredSize(new Dimension(680, 200));

        this.setBorder(new EtchedBorder());

        JPanel labelPanel = new JPanel();
        PainlessGridBag gblLabel = new PainlessGridBag(labelPanel, GUIHelper.getPainlessGridbagConfigurationNull(), false);

        gblLabel.row()
                .cell(jlTitle)
                .cell(GUIHelper.verticalSeparator())
                .cell(jlYAxis).cell(jtfYAxis).fillX()
                .cell(GUIHelper.verticalSeparator())
                .cell(jlGroup).cell(jtfGroup).fillX()
                .cell(GUIHelper.verticalSeparator())
                .cell(jlChartType).cell(jtfChartType).fillX()
                .cellXRemainder(new JLabel()).fillX();
        gblLabel.done();

        JPanel radioBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioBtnPanel.add(asIs);
        radioBtnPanel.add(count);
        radioBtnPanel.add(sum);
        radioBtnPanel.add(average);

        this.jPanelFunction = new JPanel();
        PainlessGridBag gblFunction = new PainlessGridBag(jPanelFunction, GUIHelper.getPainlessGridbagConfigurationNull(), false);
        gblFunction.row()
                .cell(new JLabel()).fillX().cell(new JLabel()).fillX()
                .cell(new JLabel()).fillX().cell(new JLabel()).fillX();
        gblFunction.row()
                .cellX(new JXTitledSeparator("Description"), 3).fillX()
                .cell(new JXTitledSeparator("Details")).fillX();
        gblFunction.row()
                .cellX(new JScrollPane(jtaDescription), 3).fillX()
                .cell(radioBtnPanel).fillX();

        setEnabled(true);

        gblFunction.done();

        PainlessGridBag gblChart = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);

        gblChart.row()
                .cell(labelPanel).fillX();
        gblChart.row()
                .cell(jPanelFunction).fillX();
        gblChart.row()
                .cell(jSplitPane).fillXY();
        gblChart.done();


        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem insertItem = new JMenuItem("Insert");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem selectAllItem = new JMenuItem("Select All");

        insertItem.addActionListener(e -> {
            String clipboardText = getClipboardText();
            if (clipboardText != null) {
                int caretPosition = jtaDescription.getCaretPosition();
                jtaDescription.insert(clipboardText, caretPosition);
            }
        });

        copyItem.addActionListener(e -> jtaDescription.copy());

        cutItem.addActionListener(e -> jtaDescription.cut());

        selectAllItem.addActionListener(e -> jtaDescription.selectAll());

        popupMenu.add(insertItem);
        popupMenu.add(copyItem);
        popupMenu.add(cutItem);
        popupMenu.addSeparator();
        popupMenu.add(selectAllItem);

        jtaDescription.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupMenu(e);
            }

            private void showPopupMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });


        this.getJtaDescription().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                descriptionTo = jtaDescription.getText();
                if (!descriptionFrom.trim().equals(descriptionTo.trim())) {
                    reportHelper.setMadeChanges(true);
                }
            }

            @Override
            public void focusGained(FocusEvent focusEvent) {
                descriptionFrom = jtaDescription.getText();
            }
        });

    }

    private String getClipboardText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setEnabled(boolean flag) {
        asIs.setEnabled(false);
        count.setEnabled(flag);
        sum.setEnabled(flag);
        average.setEnabled(flag);

    }

    public void setSelectedRadioButton(MetricFunction metricFunction) {
        switch (metricFunction) {
            case ASIS -> buttonGroupFunction.setSelected(asIs.getModel(), true);
            case COUNT -> buttonGroupFunction.setSelected(count.getModel(), true);
            case SUM -> buttonGroupFunction.setSelected(sum.getModel(), true);
            case AVERAGE -> buttonGroupFunction.setSelected(average.getModel(), true);
        }
    }

    public class RadioListenerColumn implements ActionListener {

        public RadioListenerColumn() {
        }

        public void actionPerformed(ActionEvent e) {
            JRadioButton button = (JRadioButton) e.getSource();

            switch (button.getText()) {
                case "Count" -> {
                    metricFunctionOnEdit = MetricFunction.COUNT;
                    jtfChartType.setText(String.valueOf(ChartType.STACKED));
                    loadChart(id, chartInfo, key, JPanelForChartCard.this, sourceConfig);
                }
                case "Sum" -> {
                    metricFunctionOnEdit = MetricFunction.SUM;
                    jtfChartType.setText(String.valueOf(ChartType.LINEAR));
                    loadChart(id, chartInfo, key, JPanelForChartCard.this, sourceConfig);
                }
                case "Average" -> {
                    metricFunctionOnEdit = MetricFunction.AVERAGE;
                    jtfChartType.setText(String.valueOf(ChartType.LINEAR));
                    loadChart(id, chartInfo, key, JPanelForChartCard.this, sourceConfig);
                }
            }

        }
    }

    public void loadChart(int cId, ChartInfo chartInfo, ProfileTaskQueryKey key,
                          JPanelForChartCard cardChart, SourceConfig sourceConfig) {

        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }

        this.checkTimestampColumn(tableInfo);

        if (METRICS.equals(sourceConfig)) {
            this.loadChartMetric(cId, chartInfo, key, cardChart);
        } else if (COLUMNS.equals(sourceConfig)) {
            this.loadChartColumn(cId, chartInfo, key, cardChart);
        }

        log.info("Query: " + key.getQueryId());
    }

    private void loadChartMetric(int metricId, ChartInfo chartInfo, ProfileTaskQueryKey key,
                                 JPanelForChartCard cardChart) {
        log.info("Metric ID: " + metricId);
        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }
        Metric metric = queryInfo.getMetricList()
                .stream().filter(f -> f.getId() == metricId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("Not found metric by id: " + metricId));

        executorService.submit(() -> {
            try {
                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(),
                        createProgressBar("Loading, please wait..."), JSplitPane.TOP, 200);

                loadChartByMetric(metric, queryInfo, key, chartInfo, tableInfo, cardChart);

                cardChart.repaint();
                cardChart.revalidate();

            } catch (Exception exception) {
                log.catching(exception);
                throw new RuntimeException(exception);
            }
        });

        log.info("Metric id: " + metricId);
    }

    private void loadChartColumn(int cProfileId, ChartInfo chartInfo, ProfileTaskQueryKey key, JPanelForChartCard cardChart) {

        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }

        executorService.submit(() -> {
            try {

                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(),
                        createProgressBar("Loading, please wait..."), JSplitPane.TOP, 200);

                CProfile cProfile = tableInfo.getCProfiles()
                        .stream()
                        .filter(f -> f.getColId() == cProfileId)
                        .findAny()
                        .orElseThrow(() -> new NotFoundException("Not found CProfile: " + cProfileId));

                Metric metric = getMetricByCProfile(cProfile, tableInfo);

                setMetricFunctionAndChartOnEdit(metric);

                loadChartByMetric(metric, queryInfo, key, chartInfo, tableInfo, cardChart);

                cardChart.repaint();
                cardChart.revalidate();

            } catch (Exception exception) {
                log.catching(exception);
                throw new RuntimeException(exception);
            }
        });

        log.info("Column profile id: " + cProfileId);
    }

    protected Metric getMetricByCProfile(CProfile cProfile, TableInfo tableInfo) {
        Metric metric = new Metric();
        metric.setXAxis(tableInfo.getCProfiles().stream().filter(f -> f.getCsType().isTimeStamp()).findAny().orElseThrow());
        metric.setYAxis(cProfile);
        metric.setGroup(cProfile);

        if (MetricFunction.NONE.equals(metricFunctionOnEdit)) {
            setMetricFunctionAndChartNotEdit(cProfile, metric);
        } else {
            setMetricFunctionAndChartOnEdit(metric);
        }

        return metric;
    }

    private void setMetricFunctionAndChartOnEdit(Metric metric) {
        switch (metricFunctionOnEdit) {
            case COUNT -> {
                metric.setMetricFunction(metricFunctionOnEdit);
                metric.setChartType(STACKED);
            }
            case SUM, AVERAGE -> {
                metric.setMetricFunction(metricFunctionOnEdit);
                metric.setChartType(LINEAR);
            }
        }
    }

    private void setMetricFunctionAndChartNotEdit(CProfile cProfile, Metric metric) {
        if (CType.STRING.equals(cProfile.getCsType().getCType())) {
            metric.setMetricFunction(MetricFunction.COUNT);
            metric.setChartType(STACKED);
        } else {
            if (Arrays.stream(TimestampType.values()).anyMatch((t) -> t.name().equals(cProfile.getColDbTypeName()))) {
                metric.setMetricFunction(MetricFunction.COUNT);
                metric.setChartType(STACKED);
            } else {
                metric.setMetricFunction(MetricFunction.AVERAGE);
                metric.setChartType(LINEAR);
            }
        }
    }

    private void loadChartByMetric(Metric metric, QueryInfo queryInfo, ProfileTaskQueryKey key, ChartInfo chartInfo,
                                   TableInfo tableInfo, JPanelForChartCard cardChart) {
        if (metric.isStackedYAxisSameCount()) {
            StackChartReportPanel stackChartPanel = getStackChartPanel(metric, queryInfo, key, chartInfo);

            DetailReportPanel detailPanel = getDetailPanel(fStore, metric, queryInfo, tableInfo, stackChartPanel.getSeriesColorMap(),
                    stackChartPanel, metric.getChartType());

            GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), stackChartPanel, JSplitPane.TOP, 200);
            GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), detailPanel, JSplitPane.BOTTOM, 200);
        }

        if (metric.isLinearYAxisSameSum() | metric.isLinearYAxisAvg()) {
            StackChartReportPanel stackChartPanel = getStackChartPanel(metric, queryInfo, key, chartInfo);

            DetailReportPanel detailPanel = getDetailPanel(fStore, metric, queryInfo, tableInfo, stackChartPanel.getSeriesColorMap(),
                    stackChartPanel, metric.getChartType());

            GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), stackChartPanel, JSplitPane.TOP, 200);
            GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), detailPanel, JSplitPane.BOTTOM, 200);
        }
    }


    protected StackChartReportPanel getStackChartPanel(Metric metric, QueryInfo queryInfo,
                                                       ProfileTaskQueryKey profileTaskQueryKey, ChartInfo chartInfo) {
        CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

        StackChartReportPanel stackChartReportPanel = null;


        if (GatherDataSql.BY_CLIENT.equals(queryInfo.getGatherDataSql())) {
            stackChartReportPanel =
                    new ClientHistorySCRP(categoryTableXYDatasetRealTime, profileTaskQueryKey,
                            queryInfo, chartInfo, metric, fStore);
        } else if (GatherDataSql.BY_SERVER.equals(queryInfo.getGatherDataSql())) {
            stackChartReportPanel =
                    new ServerHistorySCRP(categoryTableXYDatasetRealTime, profileTaskQueryKey,
                            queryInfo, chartInfo, metric, fStore);
        }

        assert stackChartReportPanel != null;
        stackChartReportPanel.initialize();

        return stackChartReportPanel;
    }

    protected DetailReportPanel getDetailPanel(FStore fStore, Metric metric, QueryInfo queryInfo, TableInfo tableInfo,
                                               Map<String, Color> seriesColorMap,
                                               DetailChart dynamicChart, ChartType chartType) {
        DetailReportPanel detailPanel =
                new DetailReportPanel(fStore, queryInfo, tableInfo, metric, seriesColorMap, chartType);

        dynamicChart.addChartListenerReleaseMouse(detailPanel);

        return detailPanel;
    }

    protected StackChartReportPanel getOneStackChartPanel(Metric metric, QueryInfo queryInfo,
                                                          ProfileTaskQueryKey profileTaskQueryKey, ChartInfo chartInfo) {
        CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

        StackChartReportPanel oneStackChartPanel = null;

        oneStackChartPanel = new ServerClientHistoryOneSCRP(categoryTableXYDatasetRealTime,
                profileTaskQueryKey, queryInfo, chartInfo, metric, fStore);

        assert oneStackChartPanel != null;
        oneStackChartPanel.initialize();

        return oneStackChartPanel;
    }


    private void checkTimestampColumn(TableInfo tableInfo) {
        if (Objects.isNull(tableInfo.getCProfiles())) {
            throw new NotFoundException("Metadata not found, need to reload query metadata in configuration");
        }

        tableInfo.getCProfiles().stream()
                .filter(f -> f.getCsType().isTimeStamp())
                .findAny()
                .orElseThrow(
                        () -> new NotFoundException("Not found column timestamp for table: " + tableInfo.getTableName()));
    }

    private String getTitle(SourceConfig sourceConfig) {
        if (sourceConfig == SourceConfig.METRICS) {
            return "<html><b>Metric: </b>" + metric.getName() + "</html>";
        } else if (sourceConfig == COLUMNS) {
            return "<html><b>Column: </b>" + metric.getName() + "</html>";
        }

        return "";
    }

    private String getTooltip() {
        return "<html><b>Metric:</b> " + metric.getName() + " <br>"
                + "  <b> Y Axis: </b> " + metric.getYAxis().getColName()
                + ";  <b>Group:</b>" + metric.getGroup().getColName() +
                ";  <b>Chart:</b> " + metric.getChartType() + " </html>";
    }
}
