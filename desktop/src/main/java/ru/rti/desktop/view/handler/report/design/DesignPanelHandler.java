package ru.rti.desktop.view.handler.report.design;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import org.jdesktop.swingx.*;
import org.jfree.chart.ChartUtilities;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.FilesHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.manager.ReportManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.SourceConfig;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.report.CProfileReport;
import ru.rti.desktop.model.report.MetricReport;
import ru.rti.desktop.model.report.QueryReportData;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.RangeChartHistory;
import ru.rti.desktop.model.view.ReportTabbedPane;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.report.StackChartReportPanel;
import ru.rti.desktop.view.panel.report.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;


@Log4j2
@Singleton
public class DesignPanelHandler extends ChartReportHandler implements ActionListener {

    private final Map<ProfileTaskQueryKey, QueryReportData> mapReportData;
    private final ReportTabsPane reportTabsPane;
    private final JXTableCase profileReportCase;
    private final JXTableCase taskReportCase;
    private final JXTableCase queryReportCase;

    private final JXTableCase reportMetricsCase;
    private final JXTableCase reportColumnCase;
    private final DefaultCellEditor cEditor;
    private final DefaultCellEditor mEditor;
    private final JXTaskPaneContainer containerCardDesign;
    private final JXTaskPaneContainer containerChartCardDesign;
    private final FilesHelper filesHelper;
    private final ReportManager reportManager;
    private List<File> designSaveDirs;
    private List<File> designReportDirs;
    private final ReportHelper reportHelper;
    private PathPdfInfo reportPdfPath;


    @Inject
    public DesignPanelHandler(
            @Named("profileReportCase") JXTableCase profileReportCase,
            @Named("taskReportCase") JXTableCase taskReportCase,
            @Named("queryReportCase") JXTableCase queryReportCase,
            @Named("reportMetricsCase") JXTableCase reportMetricsCase,
            @Named("reportColumnCase") JXTableCase reportColumnCase,
            @Named("reportTaskPanel") ReportTabsPane reportTabsPane,
            @Named("profileManager") ProfileManager profileManager,
            @Named("eventListener") EventListener eventListener,
            @Named("mapReportData") Map<ProfileTaskQueryKey, QueryReportData> mapReportData,
            @Named("containerCardDesign") JXTaskPaneContainer containerCardDesign,
            @Named("containerChartCardDesign") JXTaskPaneContainer containerChartCardDesign,
            @Named("reportPdfPath") PathPdfInfo reportPdfPath,
            @Named("localDB") FStore fStore,
            @Named("reportManager") ReportManager reportManager,
            FilesHelper filesHelper,
            ReportHelper reportHelper
    ) {
        super(profileManager, eventListener, fStore);

        this.profileReportCase = profileReportCase;
        this.taskReportCase = taskReportCase;
        this.queryReportCase = queryReportCase;
        this.reportMetricsCase = reportMetricsCase;
        this.reportColumnCase = reportColumnCase;
        this.mapReportData = mapReportData;
        this.containerCardDesign = containerCardDesign;
        this.containerChartCardDesign = containerChartCardDesign;
        this.reportTabsPane = reportTabsPane;
        this.filesHelper = filesHelper;
        this.reportHelper = reportHelper;
        this.reportManager = reportManager;
        this.reportPdfPath = reportPdfPath;

        this.reportTabsPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newHeight = e.getComponent().getHeight();
                int topPanelHeight = newHeight - reportTabsPane.getJspMenuDesign().getDividerLocation();

                reportTabsPane.getJspMenuDesign().setDividerLocation(topPanelHeight);
            }
        });

        updateReportSaveComboBox();
        updateSavedReportCase();

        this.reportTabsPane.getShowBtnDesign().addActionListener(this);
        this.reportTabsPane.getCollapseBtnDesign().addActionListener(this);
        this.reportTabsPane.getSaveBtnDesign().addActionListener(this);
        this.reportTabsPane.getClearBtnDesign().addActionListener(this);
        this.reportTabsPane.getGenerateReportBtnDesign().addActionListener(this);
        this.reportTabsPane.getOpenBtnDesign().addActionListener(this);

        this.cEditor = new DefaultCellEditor(new JCheckBox());
        this.reportColumnCase.getJxTable().getColumnModel().getColumn(0).setCellEditor(cEditor);

        cEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {

                ProfileTaskQueryKey key = getProfileTaskQueryKey();

                mapReportData.putIfAbsent(key, new QueryReportData());

                QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
                TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
                List<CProfile> cProfileList = tableInfo.getCProfiles();

                TableCellEditor editor = (TableCellEditor) e.getSource();
                int row = reportColumnCase.getJxTable().getSelectedRow();
                Boolean cValue = (Boolean) editor.getCellEditorValue();

                if (cValue) {
                    CProfile cProfile = cProfileList
                            .stream()
                            .filter(f -> f.getColName().equals(reportColumnCase.getDefaultTableModel()
                                    .getValueAt(reportColumnCase.getJxTable().getSelectedRow(), 2)))
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found metric by : "));

                    CProfileReport cProfileReport = new CProfileReport();
                    cProfileReport.setComment("");
                    cProfileReport.setColId(cProfile.getColId());
                    cProfileReport.setColIdSql(cProfile.getColIdSql());
                    cProfileReport.setColName(cProfile.getColName());
                    cProfileReport.setColDbTypeName(cProfile.getColDbTypeName());
                    cProfileReport.setColSizeDisplay(cProfile.getColSizeDisplay());
                    cProfileReport.setColSizeSqlType(cProfile.getColSizeSqlType());
                    cProfileReport.setCsType(cProfile.getCsType());

                    if (!mapReportData.get(key)
                            .getCProfileReportList()
                            .contains(cProfileReport)) {
                        mapReportData.get(key)
                                .getCProfileReportList()
                                .add(cProfileReport);
                    }

                } else {
                    int colId = (int) reportColumnCase.getDefaultTableModel()
                            .getValueAt(row, profileReportCase.getDefaultTableModel()
                                    .findColumn(MetricsColumnNames.ID.getColName()));

                    mapReportData.get(key)
                            .getCProfileReportList()
                            .removeIf(c -> c.getColId() == colId);

                    JXTaskPaneWithJXTableCase cardInfo = getCardComponent(key);
                    JXTableCase cTable = cardInfo.getJtcColumn();
                    for (int index = 0; index < cTable.getJxTable().getRowCount(); index++) {
                        int columnId = (int) cTable.getDefaultTableModel()
                                .getValueAt(index, cTable.getDefaultTableModel()
                                        .findColumn(MetricsColumnNames.ID.getColName()));
                        String columnName = String.valueOf(cTable.getDefaultTableModel()
                                .getValueAt(index, 1));
                        if (colId == columnId) {
                            removeCard(key, columnName);
                        }
                    }
                }
                viewCardInfo(key);
            }

            @Override
            public void editingCanceled(ChangeEvent changeEvent) {
            }
        });

        this.mEditor = new DefaultCellEditor(new JCheckBox());
        this.reportMetricsCase.getJxTable().getColumnModel().getColumn(0).setCellEditor(mEditor);

        mEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {

                ProfileTaskQueryKey key = getProfileTaskQueryKey();

                mapReportData.putIfAbsent(key, new QueryReportData());

                QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
                List<Metric> metricList = queryInfo.getMetricList();

                TableCellEditor editor = (TableCellEditor) e.getSource();
                int row = reportMetricsCase.getJxTable().getSelectedRow();
                Boolean mValue = (Boolean) editor.getCellEditorValue();

                if (mValue) {
                    Metric metric = metricList
                            .stream().filter(f -> f.getName()
                                    .equals(reportMetricsCase.getDefaultTableModel()
                                            .getValueAt(reportMetricsCase.getJxTable().getSelectedRow(), 2)))
                            .findAny()
                            .orElseThrow(() -> new NotFoundException("Not found metric by : "));

                    MetricReport metricReport = new MetricReport();
                    metricReport.setId(metric.getId());
                    metricReport.setName(metric.getName());
                    metricReport.setIsDefault(metric.getIsDefault());
                    metricReport.setXAxis(metric.getXAxis());
                    metricReport.setYAxis(metric.getYAxis());
                    metricReport.setGroup(metric.getGroup());
                    metricReport.setMetricFunction(metric.getMetricFunction());
                    metricReport.setChartType(metric.getChartType());
                    metricReport.setColumnGanttList(metric.getColumnGanttList());
                    metricReport.setComment("");

                    if (!mapReportData.get(key)
                            .getMetricReportList()
                            .contains(metricReport)) {
                        mapReportData.get(key)
                                .getMetricReportList()
                                .add(metricReport);
                    }

                } else {
                    int metricId = (int) reportMetricsCase.getDefaultTableModel()
                            .getValueAt(row, profileReportCase.getDefaultTableModel()
                                    .findColumn(MetricsColumnNames.ID.getColName()));

                    mapReportData.get(key)
                            .getMetricReportList()
                            .removeIf(c -> c.getId() == metricId);

                    JXTaskPaneWithJXTableCase cardInfo = getCardComponent(key);
                    JXTableCase mTable = cardInfo.getJtcMetric();
                    for (int index = 0; index < mTable.getJxTable().getRowCount(); index++) {
                        int mId = (int) mTable.getDefaultTableModel()
                                .getValueAt(index, mTable.getDefaultTableModel()
                                        .findColumn(MetricsColumnNames.ID.getColName()));
                        String mName = (String) mTable.getDefaultTableModel()
                                .getValueAt(index, mTable.getDefaultTableModel()
                                        .findColumn(MetricsColumnNames.NAME.getColName()));
                        if (metricId == mId) {
                            removeCard(key, mName);
                        }
                    }
                }
                viewCardInfo(key);
            }

            @Override
            public void editingCanceled(ChangeEvent changeEvent) {

            }
        });

    }

    private ProfileTaskQueryKey getProfileTaskQueryKey() {
        int profileId = (int) profileReportCase.getDefaultTableModel()
                .getValueAt(profileReportCase.getJxTable()
                        .getSelectedRow(), profileReportCase.getDefaultTableModel()
                        .findColumn(ProfileColumnNames.ID.getColName()));
        int taskId = (int) taskReportCase.getDefaultTableModel()
                .getValueAt(taskReportCase.getJxTable()
                        .getSelectedRow(), taskReportCase.getDefaultTableModel()
                        .findColumn(TaskColumnNames.ID.getColName()));
        int queryId = (int) queryReportCase.getDefaultTableModel()
                .getValueAt(queryReportCase.getJxTable()
                        .getSelectedRow(), queryReportCase.getDefaultTableModel()
                        .findColumn(QueryColumnNames.ID.getColName()));
        ProfileTaskQueryKey key = new ProfileTaskQueryKey(profileId, taskId, queryId);
        return key;
    }

    private void removeCard(ProfileTaskQueryKey key, String name) {

        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        ProfileInfo profileInfo = profileManager.getProfileInfoById(key.getProfileId());
        TaskInfo taskInfo = profileManager.getTaskInfoById(key.getTaskId());

        String title = setTitleCard(profileInfo.getName(), taskInfo.getName(), queryInfo.getName());

        ArrayList<Component> list = new ArrayList<>(List.of(containerChartCardDesign.getComponents()));
        for (Component card : list) {
            if (card instanceof JXTaskPaneForChartCard) {
                JXTaskPaneForChartCard cardInfo = (JXTaskPaneForChartCard) card;
                String nameFromTitle = cardInfo.getTitle().substring(cardInfo.getTitle().indexOf(":") + 1).trim();

                if (name.equals(nameFromTitle)) {
                    containerChartCardDesign.remove(card);
                    containerChartCardDesign.revalidate();
                    containerChartCardDesign.repaint();
                }
            }
        }
    }

    private void viewCardInfo(ProfileTaskQueryKey key) {

        if (isVisibleCard(key)) {

            List<MetricReport> metricReportList = mapReportData.get(key).getMetricReportList();
            List<CProfileReport> cProfileReportList = mapReportData.get(key).getCProfileReportList();

            JXTaskPaneWithJXTableCase cardInfo = getCardComponent(key);
            cardInfo.getJtcMetric().getDefaultTableModel().getDataVector().removeAllElements();
            cardInfo.getJtcMetric().getDefaultTableModel().fireTableDataChanged();
            if (metricReportList.size() != 0) {
                for (MetricReport m : metricReportList) {
                    cardInfo.getJtcMetric().getDefaultTableModel()
                            .addRow(new Object[]{m.getId(), m.getName()});
                }
                cardInfo.getJtcMetric().getJScrollPane().setVisible(true);
            } else {
                cardInfo.getJtcMetric().getJScrollPane().setVisible(false);
            }

            cardInfo.getJtcColumn().getDefaultTableModel().getDataVector().removeAllElements();
            cardInfo.getJtcColumn().getDefaultTableModel().fireTableDataChanged();
            if (cProfileReportList.size() != 0) {
                for (CProfileReport c : cProfileReportList) {
                    cardInfo.getJtcColumn().getDefaultTableModel()
                            .addRow(new Object[]{c.getColId(), c.getColName()});
                }
                cardInfo.getJtcColumn().getJScrollPane().setVisible(true);
            } else {
                cardInfo.getJtcColumn().getJScrollPane().setVisible(false);
            }

            if (mapReportData.get(key).getCProfileReportList().isEmpty()
                    && mapReportData.get(key).getMetricReportList().isEmpty()) {
                containerCardDesign.remove(cardInfo);
                containerCardDesign.revalidate();
                containerCardDesign.repaint();
            }

        } else {

            QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
            ProfileInfo profileInfo = profileManager.getProfileInfoById(key.getProfileId());
            TaskInfo taskInfo = profileManager.getTaskInfoById(key.getTaskId());

            List<MetricReport> metricReportList = mapReportData.get(key).getMetricReportList();
            List<CProfileReport> cProfileReportList = mapReportData.get(key).getCProfileReportList();

            JXTaskPaneWithJXTableCase cardInfo = new JXTaskPaneWithJXTableCase(key);
            cardInfo.setTitle(queryInfo.getName());
            cardInfo.setToolTipText(setTitleCard(profileInfo.getName(), taskInfo.getName(), ""));

            if (metricReportList.size() != 0) {
                for (MetricReport m : metricReportList) {
                    cardInfo.getJtcMetric().getDefaultTableModel()
                            .addRow(new Object[]{m.getId(), m.getName()});
                }
                cardInfo.getJtcMetric().getJScrollPane().setVisible(true);
            } else {
                cardInfo.getJtcMetric().getJScrollPane().setVisible(false);
            }

            if (cProfileReportList.size() != 0) {
                for (CProfileReport c : cProfileReportList) {
                    cardInfo.getJtcColumn().getDefaultTableModel()
                            .addRow(new Object[]{c.getColId(), c.getColName()});
                }
                cardInfo.getJtcColumn().getJScrollPane().setVisible(true);
            } else {
                cardInfo.getJtcColumn().getJScrollPane().setVisible(false);
            }

            containerCardDesign.add(cardInfo);
        }
        containerCardDesign.revalidate();
        containerCardDesign.repaint();

    }


    private boolean isVisibleCard(ProfileTaskQueryKey key) {
        List<Component> components = Arrays.asList(containerCardDesign.getComponents());
        return components.stream()
                .filter(card -> card instanceof JXTaskPaneWithJXTableCase)
                .map(card -> (JXTaskPaneWithJXTableCase) card)
                .anyMatch(cardI -> key.equals(cardI.getKey()));
    }

    private JXTaskPaneWithJXTableCase getCardComponent(ProfileTaskQueryKey key) {
        List<Component> components = Arrays.asList(containerCardDesign.getComponents());
        return components.stream()
                .filter(card -> card instanceof JXTaskPaneWithJXTableCase)
                .map(card -> (JXTaskPaneWithJXTableCase) card)
                .filter(card -> key.equals(card.getKey()))
                .findFirst()
                .orElse(new JXTaskPaneWithJXTableCase(key));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reportTabsPane.getShowBtnDesign()) {

            containerChartCardDesign.removeAll();

            viewButtons(true);

            for (Map.Entry<ProfileTaskQueryKey, QueryReportData> entry : mapReportData.entrySet()) {

                ProfileTaskQueryKey key = entry.getKey();

                QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
                ProfileInfo profileInfo = profileManager.getProfileInfoById(key.getProfileId());
                TaskInfo taskInfo = profileManager.getTaskInfoById(key.getTaskId());

                List<MetricReport> metricReportList = entry.getValue().getMetricReportList();
                List<CProfileReport> cProfileReportList = entry.getValue().getCProfileReportList();

                ChartInfo chartInfo = profileManager.getChartInfoById(queryInfo.getId());
                if (Objects.isNull(chartInfo)) {
                    throw new NotFoundException(String.format("Chart info with id=%s not found",
                            queryInfo.getId()));
                }

                chartInfo.setRangeHistory(RangeChartHistory.CUSTOM);

                Date beginDate = reportTabsPane.getDateTimePickerFrom().getDate();
                Date endDate = reportTabsPane.getDateTimePickerTo().getDate();

                LocalDateTime begin = beginDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (!begin.isBefore(end)) {
                    JOptionPane.showMessageDialog(null, "Begin value must be less the end one",
                            "General Error", JOptionPane.ERROR_MESSAGE);
                } else {

                    if (metricReportList.size() == 0 && cProfileReportList.size() == 0) {
                    } else {
                        String title = setTitleCard(profileInfo.getName(), taskInfo.getName(), queryInfo.getName());
                        JPanel jPanel = new JPanel(new BorderLayout());
                        jPanel.add(new JLabel(title), BorderLayout.WEST);
                        jPanel.setBorder(new EtchedBorder());
                        containerChartCardDesign.add(jPanel);
                        containerChartCardDesign.setToolTipText(setTitleCard(profileInfo.getName(), taskInfo.getName(), queryInfo.getName()));

                        chartInfo.setCustomBegin(begin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                        chartInfo.setCustomEnd(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    }


                    if (metricReportList.size() != 0) {
                        for (MetricReport m : metricReportList) {
                            JXTaskPaneForChartCard cardChart = new JXTaskPaneForChartCard(key);
                            cardChart.setTitle("Metric: " + m.getName());
                            cardChart.setToolTipText("Metric: " + m.getName());
                            cardChart.getJtaDescription().setText(m.getComment());

                            cardChart.getJtaDescription().addFocusListener(new FocusListener() {
                                @Override
                                public void focusLost(FocusEvent focusEvent) {
                                    m.setComment(cardChart.getJtaDescription().getText());
                                }

                                @Override
                                public void focusGained(FocusEvent focusEvent) {
                                }
                            });

                            this.loadChart(m.getId(), chartInfo, key, cardChart, SourceConfig.METRICS);
                            containerChartCardDesign.add(cardChart);
                            containerChartCardDesign.repaint();
                            containerChartCardDesign.revalidate();
                        }
                        containerChartCardDesign.setBorder(new EtchedBorder());
                    }
                    if (cProfileReportList.size() != 0) {
                        for (CProfileReport c : cProfileReportList) {
                            JXTaskPaneForChartCard cardChart = new JXTaskPaneForChartCard(key);
                            cardChart.setTitle("Column: " + c.getColName());
                            cardChart.setToolTipText("Column: " + c.getColName());
                            cardChart.getJtaDescription().setText(c.getComment());

                            cardChart.getJtaDescription().addFocusListener(new FocusListener() {
                                @Override
                                public void focusLost(FocusEvent focusEvent) {
                                    c.setComment(cardChart.getJtaDescription().getText());
                                }

                                @Override
                                public void focusGained(FocusEvent focusEvent) {
                                }
                            });

                            this.loadChart(c.getColId(), chartInfo, key, cardChart, SourceConfig.COLUMNS);
                            containerChartCardDesign.add(cardChart);
                            containerChartCardDesign.repaint();
                            containerChartCardDesign.revalidate();
                        }
                        reportTabsPane.getCollapseBtnDesign().setVisible(true);
                        reportTabsPane.getCollapseBtnDesign().setText("Collapse all");
                    }
                }
            }
        }
        if (e.getSource() == reportTabsPane.getCollapseBtnDesign()) {
            ArrayList<Component> containerCards = new ArrayList<>(List.of(containerChartCardDesign.getComponents()));

            boolean collapseAll = reportTabsPane.getCollapseBtnDesign().getText().equals("Collapse all");

            containerCards.stream()
                    .filter(c -> c instanceof JXTaskPaneForChartCard)
                    .map(c -> (JXTaskPaneForChartCard) c)
                    .forEach(cardChart -> cardChart.setCollapsed(collapseAll));

            reportTabsPane.getCollapseBtnDesign().setText(collapseAll ? "Expand all" : "Collapse all");
        }
        if (e.getSource() == reportTabsPane.getClearBtnDesign()) {
            int input = JOptionPane.showConfirmDialog(new JDialog(),// 0=yes, 1=no, 2=cancel
                    "Do you want to clear the design panel ?");
            if (input == 0) {
                containerCardDesign.removeAll();
                containerChartCardDesign.removeAll();
                mapReportData.clear();
                reportTabsPane.getCollapseBtnDesign().setVisible(false);
                for (int row = 0; row < reportColumnCase.getJxTable().getRowCount(); row++) {
                    reportColumnCase.getJxTable().setValueAt(false, row, 0);
                }
                for (int row = 0; row < reportMetricsCase.getJxTable().getRowCount(); row++) {
                    reportMetricsCase.getJxTable().setValueAt(false, row, 0);
                }
                viewButtons(false);
            }
        }
        if (e.getSource() == reportTabsPane.getSaveBtnDesign()) {

            Map<String, QueryReportData> mapKeyString = new HashMap<>();

            mapReportData.entrySet()
                    .forEach(f -> {
                        {
                            ChartInfo chartInfo = profileManager.getChartInfoById(f.getKey().getQueryId());
                            mapKeyString
                                    .put(f.getKey().getProfileId()
                                                    + "_" + f.getKey().getTaskId()
                                                    + "_" + f.getKey().getQueryId()
                                                    + "_" + reportHelper.getDateFormatFused(chartInfo.getCustomBegin())
                                                    + "_" + reportHelper.getDateFormatFused(chartInfo.getCustomEnd()),
                                            f.getValue());
                        }
                    });
            LocalDateTime nowDateTime = LocalDateTime.now();
            String formattedDateForDir = nowDateTime.format(reportHelper.getDateTimeFormatterFused());

            reportManager.addConfig(mapKeyString, formattedDateForDir);
            reportPdfPath.setDirDesignName("design_" + formattedDateForDir);

            updateReportSaveComboBox();

            JOptionPane.showMessageDialog(null,
                    "Design - " + nowDateTime.format(reportHelper.getDateTimeFormatter()) + " has been saved",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
        if (e.getSource() == reportTabsPane.getOpenBtnDesign()) {
            String folderName = "design_" + getFolderDate();
            reportPdfPath.setDirDesignName(folderName);

            Map<String, QueryReportData> mapDesign = reportManager.getConfig(folderName, "design");
            if (!mapDesign.isEmpty()) {
                containerCardDesign.removeAll();
                containerChartCardDesign.removeAll();

                for (Map.Entry<String, QueryReportData> entry : mapDesign.entrySet()) {
                    String[] stringKey = entry.getKey().split("_");

                    int profileId = Integer.parseInt(stringKey[0]);
                    String profileName = profileManager.getProfileInfoById(profileId).getName();
                    int taskId = Integer.parseInt(stringKey[1]);
                    String taskName = profileManager.getTaskInfoById(taskId).getName();
                    int queryId = Integer.parseInt(stringKey[2]);
                    String queryName = profileManager.getQueryInfoById(queryId).getName();
                    QueryInfo queryInfo = profileManager.getQueryInfoById(queryId);

                    String dateFrom = stringKey[3];
                    LocalDateTime begin = LocalDateTime.parse(dateFrom, reportHelper.getDateTimeFormatterFused());
                    Date dateBegin = Date.from(begin.atZone(ZoneId.systemDefault()).toInstant());

                    String dateTo = stringKey[4];
                    LocalDateTime end = LocalDateTime.parse(dateTo, reportHelper.getDateTimeFormatterFused());
                    Date dateEnd = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

                    ProfileTaskQueryKey key = new ProfileTaskQueryKey(profileId, taskId, queryId);

                    reportTabsPane.getCollapseBtnDesign().setVisible(true);
                    reportTabsPane.getCollapseBtnDesign().setText("Collapse all");

                    List<MetricReport> metricReportList = entry.getValue().getMetricReportList();
                    List<CProfileReport> cProfileReportList = entry.getValue().getCProfileReportList();
                    mapReportData.put(key, new QueryReportData(cProfileReportList, metricReportList));

                    ChartInfo chartInfo = profileManager.getChartInfoById(queryInfo.getId());
                    if (Objects.isNull(chartInfo)) {
                        throw new NotFoundException(String.format("Chart info with id=%s not found",
                                queryInfo.getId()));
                    }

                    chartInfo.setRangeHistory(RangeChartHistory.CUSTOM);

                    if (!begin.isBefore(end)) {
                        JOptionPane.showMessageDialog(null, " Begin value must be less the end one",
                                "General Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        reportTabsPane.getDateTimePickerFrom().setDate(dateBegin);
                        reportTabsPane.getDateTimePickerTo().setDate(dateEnd);

                        if (metricReportList.size() == 0 && cProfileReportList.size() == 0) {
                        } else {
                            String title = setTitleCard(profileName, taskName, queryName);
                            JPanel jPanel = new JPanel(new BorderLayout());
                            jPanel.add(new JLabel(title), BorderLayout.WEST);
                            jPanel.setBorder(new EtchedBorder());
                            containerChartCardDesign.add(jPanel);
                            containerChartCardDesign.setToolTipText(setTitleCard(profileName, taskName, queryName));

                            chartInfo.setCustomBegin(begin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            chartInfo.setCustomEnd(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());


                            if (metricReportList.size() != 0) {
                                for (MetricReport m : metricReportList) {
                                    JXTaskPaneForChartCard cardChart = new JXTaskPaneForChartCard(key);
                                    cardChart.setTitle("Metric: " + m.getName());
                                    cardChart.setToolTipText("Metric: " + m.getName());
                                    cardChart.getJtaDescription().setText(m.getComment());

                                    cardChart.getJtaDescription().addFocusListener(new FocusListener() {
                                        @Override
                                        public void focusLost(FocusEvent focusEvent) {
                                            m.setComment(cardChart.getJtaDescription().getText());
                                        }

                                        @Override
                                        public void focusGained(FocusEvent focusEvent) {
                                        }
                                    });

                                    this.loadChart(m.getId(), chartInfo, key, cardChart, SourceConfig.METRICS);
                                    containerChartCardDesign.add(cardChart);
                                    containerChartCardDesign.repaint();
                                    containerChartCardDesign.revalidate();
                                }
                                containerChartCardDesign.setBorder(new EtchedBorder());
                            }
                            if (cProfileReportList.size() != 0) {
                                for (CProfileReport c : cProfileReportList) {
                                    JXTaskPaneForChartCard cardChart = new JXTaskPaneForChartCard(key);
                                    cardChart.setTitle("Column: " + c.getColName());
                                    cardChart.setToolTipText("Column: " + c.getColName());
                                    cardChart.getJtaDescription().setText(c.getComment());

                                    cardChart.getJtaDescription().addFocusListener(new FocusListener() {
                                        @Override
                                        public void focusLost(FocusEvent focusEvent) {
                                            c.setComment(cardChart.getJtaDescription().getText());
                                        }

                                        @Override
                                        public void focusGained(FocusEvent focusEvent) {
                                        }
                                    });

                                    this.loadChart(c.getColId(), chartInfo, key, cardChart, SourceConfig.COLUMNS);
                                    containerChartCardDesign.add(cardChart);
                                    containerChartCardDesign.repaint();
                                    containerChartCardDesign.revalidate();
                                }
                            }
                            reportTabsPane.getCollapseBtnDesign().setVisible(true);
                            reportTabsPane.getCollapseBtnDesign().setText("Collapse all");
                            reportTabsPane.getClearBtnDesign().setEnabled(true);
                            reportTabsPane.getGenerateReportBtnDesign().setEnabled(true);

                            queryReportCase.getJxTable().clearSelection();
                            highlightRowsTables(key);
                            viewCardInfo(key);
                        }
                    }
                }
            }
        }
        if (e.getSource() == reportTabsPane.getGenerateReportBtnDesign()) {

            String folderName = reportPdfPath.getDirDesignName();
            String reportName = reportPdfPath.getReportPdfPath();

            String folderPath = filesHelper.getTemplateDir();
            boolean isEmpty = isFolderEmpty(folderPath);

            if (isEmpty) {
                try {
                    loadFileToFolder("default.ftl", folderPath);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            List<TemplateInfo> templateInfos = new ArrayList<>();
            List<Component> containerCards = Arrays.asList(containerChartCardDesign.getComponents());

            int profileId = 0;
            int taskId = 0;
            int queryId = 0;
            String profileName = "";
            String taskName = "";
            String queryName = "";
            String dateFrom = "";
            String dateTo = "";

            Map<String, QueryReportData> mapKeyString = new HashMap<>();

            LocalDateTime dateTime;
            String formattedDateForDir;
            if (folderName.equals(" ")) {
                dateTime = LocalDateTime.now();
                formattedDateForDir = dateTime.format(reportHelper.getDateTimeFormatterFused());
                folderName = "design_" + formattedDateForDir;
            } else {
                formattedDateForDir = reportPdfPath.getDateTimeFolder();
                dateTime = LocalDateTime.parse(formattedDateForDir, reportHelper.getDateTimeFormatterFused());
            }

            for (Component c : containerCards) {
                Map<String, Object> dataReport = new HashMap<>();
                if (c instanceof JXTaskPaneForChartCard) {
                    JXTaskPaneForChartCard cardChart = (JXTaskPaneForChartCard) c;

                    ProfileTaskQueryKey profileTaskQueryKey = cardChart.getKey();
                    profileId = profileTaskQueryKey.getProfileId();
                    profileName = profileManager.getProfileInfoById(profileId).getName();

                    taskId = profileTaskQueryKey.getTaskId();
                    taskName = profileManager.getTaskInfoById(profileId).getName();

                    queryId = profileTaskQueryKey.getQueryId();
                    queryName = profileManager.getQueryInfoById(profileId).getName();

                    ChartInfo chartInfo = profileManager.getChartInfoById(queryId);

                    dateFrom = reportHelper.getDateFormat(chartInfo.getCustomBegin());
                    dateTo = reportHelper.getDateFormat(chartInfo.getCustomEnd());

                    String fileName = cardChart.getTitle().trim().split(" ")[1].toLowerCase();
                    String description = cardChart.getJtaDescription().getText();

                    mapReportData.entrySet()
                            .forEach(f -> {
                                mapKeyString
                                        .put(f.getKey().getProfileId()
                                                        + "_" + f.getKey().getTaskId()
                                                        + "_" + f.getKey().getQueryId()
                                                        + "_" + reportHelper.getDateFormatFused(chartInfo.getCustomBegin())
                                                        + "_" + reportHelper.getDateFormatFused(chartInfo.getCustomEnd()),
                                                f.getValue());
                            });

                    if (cardChart.getJSplitPane().getTopComponent() instanceof StackChartReportPanel) {
                        StackChartReportPanel chartPanel = (StackChartReportPanel) cardChart.getJSplitPane().getTopComponent();

                        String designDir = filesHelper.getDesignDir() + filesHelper.getFileSeparator() + folderName
                                + filesHelper.getFileSeparator() + "profileId_" + profileId
                                + "_taskId_" + taskId
                                + "_queryId_" + queryId;

                        /**
                         *     todo implement relative path
                         *    String designDir2 = "profileId_" + profileId
                         *                               + "_taskId_" + taskId
                         *                                + "_queryId_" + queryId;
                         */

                        String filePath = designDir + filesHelper.getFileSeparator() + fileName + ".png";
                        try {
                            Files.createDirectories(Paths.get(designDir));
                            ChartUtilities.saveChartAsPNG(new File(filePath), chartPanel.getJFreeChart(), 680, 200);
                            String pathPNG = designDir + filesHelper.getFileSeparator() + fileName + ".png";
                            templateInfos.add(new TemplateInfo(cardChart.getTitle(), description, pathPNG));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    try {
                        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
                        String fileNameTemplate = "default.ftl";
                        Path path = filesHelper.getFilePathTemplate("default.ftl");
                        cfg.setDirectoryForTemplateLoading(path.toFile());
                        cfg.setDefaultEncoding("UTF-8");

                        dataReport.put("dateFrom", dateFrom);
                        dataReport.put("dateTo", dateTo);
                        dataReport.put("profileName", profileName);
                        dataReport.put("taskName", taskName);
                        dataReport.put("queryName", queryName);
                        dataReport.put("templatesInfos", templateInfos);

                        StringWriter stringWriter = new StringWriter();
                        Template template = cfg.getTemplate(fileNameTemplate);
                        template.process(dataReport, stringWriter);
                        String html = stringWriter.toString();

                        String designDir = filesHelper.getDesignDir() + filesHelper.getFileSeparator() + folderName;
                        String templatefileName = String.format("template_%s.html", formattedDateForDir);
                        String filePath = designDir + filesHelper.getFileSeparator() + templatefileName;

                        File file = new File(filePath);
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bf = new BufferedWriter(fw);
                        bf.write(html);
                        bf.close();

                        String reportfileName = String.format("report_%s.pdf", formattedDateForDir);
                        String fileReportPath = designDir + filesHelper.getFileSeparator() + reportfileName;
                        Document document = new Document();
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileReportPath));
                        document.open();
                        XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream(filePath));
                        document.close();

                        viewFilePdf(fileReportPath);

                    } catch (IOException | TemplateException | DocumentException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            if (reportPdfPath.getDirDesignName().equals(" ")) {
                reportPdfPath.setDirDesignName(folderName);
                reportManager.addConfig(mapKeyString, formattedDateForDir);
            } else {
                String fileJson = folderName + filesHelper.getFileSeparator() + "design.json";
                try {
                    reportManager.deleteDesign(fileJson);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                reportManager.addConfig(mapKeyString, formattedDateForDir);
            }
            this.reportTabsPane.getSavedReportCase().getDefaultTableModel().getDataVector().removeAllElements();
            this.reportTabsPane.getSavedReportCase().getDefaultTableModel().fireTableDataChanged();
            this.reportTabsPane.getReportSaveComboBox().removeAllItems();
            updateReportSaveComboBox();
            updateSavedReportCase();

            JOptionPane.showMessageDialog(null,
                    "Design - " + dateTime.format(reportHelper.getDateTimeFormatter()) + " has been saved",
                    "Information", JOptionPane.INFORMATION_MESSAGE);

            if (reportTabsPane.isEnabledAt(1)) {
                reportTabsPane.setSelectedTab(ReportTabbedPane.REPORT);
                reportTabsPane.getSaveBtnPDFReport().setEnabled(true);
            }
        }
    }

    private void updateSavedReportCase() {
        designReportDirs = new ArrayList<>();
        String designName = "";
        File designFolder = new File(filesHelper.getDesignDir());
        if (designFolder.exists() && designFolder.isDirectory()) {
            File[] folders = designFolder.listFiles(File::isDirectory);
            for (File folder : folders) {

                File[] files = folder.listFiles();
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        designReportDirs.add(folder);
                    }
                }
            }

            Collections.reverse(designReportDirs);

            this.reportTabsPane.getSavedReportCase().getDefaultTableModel().getDataVector().removeAllElements();
            this.reportTabsPane.getSavedReportCase().getDefaultTableModel().fireTableDataChanged();
            if (designReportDirs != null) {
                for (File folder : designReportDirs) {
                    if (folder.listFiles() != null && folder.listFiles().length != 0) {
                        String dateStr = folder.getName().substring(folder.getName().indexOf("_") + 1);
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr, reportHelper.getDateTimeFormatterFused());
                        String reportName = "Report - " + dateTime.format(reportHelper.getDateTimeFormatter());
                        reportTabsPane.getSavedReportCase().getDefaultTableModel()
                                .addRow(new Object[]{reportName});

                    }
                }
            }
        }
    }

    private void updateReportSaveComboBox() {
        designSaveDirs = new ArrayList<>();

        this.reportTabsPane.getReportSaveComboBox().removeAllItems();
        String designName = "";
        File designFolder = new File(filesHelper.getDesignDir());
        if (designFolder.exists() && designFolder.isDirectory()) {
            File[] folders = designFolder.listFiles(File::isDirectory);
            for (File folder : folders) {

                File[] files = folder.listFiles();
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".json")) {
                        designSaveDirs.add(folder);
                    }
                }
            }

            Collections.reverse(designSaveDirs);

            if (designSaveDirs != null) {
                for (File folder : designSaveDirs) {
                    if (folder.listFiles() != null && folder.listFiles().length != 0) {
                        String dateStr = folder.getName().substring(folder.getName().indexOf("_") + 1);
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr, reportHelper.getDateTimeFormatterFused());
                        designName = "Design - " + dateTime.format(reportHelper.getDateTimeFormatter());
                        this.reportTabsPane.getReportSaveComboBox().addItem(designName);
                    }
                }
            }
        }

    }

    private void viewButtons(boolean isEnabled) {
        reportTabsPane.getSaveBtnDesign().setEnabled(isEnabled);
        reportTabsPane.getClearBtnDesign().setEnabled(isEnabled);
        reportTabsPane.getGenerateReportBtnDesign().setEnabled(isEnabled);
    }

    private String setTitleCard(String profileName, String taskName, String queryName) {
        String title = "";
        if (!queryName.equals("")) {
            title = "<html><b>Profile:</b> " + profileName + "<br>"
                    + "  <b>Task:</b> " + taskName + "<br>"
                    + "  <b>Query:</b> " + queryName + "</html>";
        } else {
            title = "<html><b>Profile:</b> " + profileName + "<br>"
                    + "  <b>Task:</b> " + taskName + "</html>";
        }

        return title;
    }

    private String getFolderDate() {
        String designFolder = reportTabsPane.getReportSaveComboBox().getSelectedItem().toString();
        String dateStr = designFolder.substring(designFolder.indexOf("-") + 1).trim();
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, reportHelper.getDateTimeFormatter());
        String folderDate = dateTime.format(reportHelper.getDateTimeFormatterFused());
        return folderDate;
    }

    private void highlightRowsTables(ProfileTaskQueryKey profileTaskQueryKey) {
        int profileId = profileTaskQueryKey.getProfileId();
        int taskId = profileTaskQueryKey.getTaskId();
        int queryId = profileTaskQueryKey.getQueryId();

        int profileRow = 0;
        boolean flag = true;
        while (profileRow < profileReportCase.getJxTable().getRowCount() && flag) {
            int profileIdRow = (int) profileReportCase.getDefaultTableModel()
                    .getValueAt(profileRow, profileReportCase.getDefaultTableModel()
                            .findColumn(ProfileColumnNames.ID.getColName()));
            if (profileIdRow == profileId) {
                profileReportCase.getJxTable().setRowSelectionInterval(profileRow, profileRow);
                flag = false;
            } else {
                profileRow++;
            }
        }

        int taskRow = 0;
        flag = true;
        while (taskRow < taskReportCase.getJxTable().getRowCount() && flag) {
            int taskIdRow = (int) taskReportCase.getDefaultTableModel()
                    .getValueAt(taskRow, taskReportCase.getDefaultTableModel()
                            .findColumn(TaskColumnNames.ID.getColName()));
            if (taskIdRow == taskId) {
                taskReportCase.getJxTable().setRowSelectionInterval(taskRow, taskRow);
                flag = false;
            } else {
                taskRow++;
            }
        }
        int queryRow = 0;
        flag = true;
        while (queryRow < queryReportCase.getJxTable().getRowCount() && flag) {
            int queryIdRow = (int) queryReportCase.getDefaultTableModel()
                    .getValueAt(queryRow, queryReportCase.getDefaultTableModel()
                            .findColumn(QueryColumnNames.ID.getColName()));
            if (queryIdRow == queryId) {
                queryReportCase.getJxTable().setRowSelectionInterval(queryRow, queryRow);
                flag = false;
            } else {
                queryRow++;
            }
        }
    }

    public boolean isFolderEmpty(String folderPath) {
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            String[] files = folder.list();
            return (files == null || files.length == 0);
        }
        return true;
    }

    public void loadFileToFolder(String filename, String folderPath) throws IOException {
        InputStream inputStream = DesignPanelHandler.class.getClassLoader()
                .getResourceAsStream("ftl" + filesHelper.getFileSeparator() + filename);

        if (inputStream != null) {
            Path targetPath = Path.of(folderPath, filename);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void viewFilePdf(String fileReportPath) {
        JPanel jPanelPDF = new JPanel(new VerticalLayout());

        try {
            PDDocument document = PDDocument.load(new File(fileReportPath));
            PDFRenderer renderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 150);
                ImageIcon imageIcon = new ImageIcon(image);

                JLabel labelImage = new JLabel();
                labelImage.setIcon(imageIcon);
                jPanelPDF.add(labelImage);

                JLabel emptyLabel = new JLabel(String.valueOf(pageIndex + 1));
                emptyLabel.setSize(new Dimension(25, 25));

                jPanelPDF.add(emptyLabel);
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reportTabsPane.getScrollPanePDF().setViewportView(jPanelPDF);
        reportPdfPath.setReportPdfPath(fileReportPath);
    }

}

