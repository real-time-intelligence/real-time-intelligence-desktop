package ru.rti.desktop.view.structure.report;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.FilesHelper;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.manager.ConfigurationManager;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.config.ConfigClasses;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.config.Profile;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.report.CProfileReport;
import ru.rti.desktop.model.report.MetricReport;
import ru.rti.desktop.model.report.QueryReportData;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ReportState;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.ReportListener;
import ru.rti.desktop.view.panel.report.ReportTabsPane;
import ru.rti.desktop.view.structure.ReportView;

@Log4j2
@Singleton
public class ReportPresenter extends WindowAdapter implements ReportListener, ListSelectionListener {
    private final ProfileManager profileManager;
    private final ReportView reportView;
    private final JXTableCase profileReportCase;
    private final JXTableCase taskReportCase;
    private final JXTableCase queryReportCase;
    private final ConfigurationManager configurationManager;
    private final EventListener eventListener;
    private final JXTableCase reportMetricsCase;
    private final JXTableCase reportColumnCase;
    private final Map<ProfileTaskQueryKey, QueryReportData> mapReportData;
    private final List<File> designSaveDirs;
    private final List<File> designReportDirs;
    private final ReportTabsPane reportTabsPane;
    private final FilesHelper filesHelper;
    private final ReportHelper reportHelper;

    @Inject
    public ReportPresenter(@Named("profileManager") ProfileManager profileManager,
                           @Named("reportView") ReportView reportView,
                           @Named("profileReportCase") JXTableCase profileReportCase,
                           @Named("taskReportCase") JXTableCase taskReportCase,
                           @Named("queryReportCase") JXTableCase queryReportCase,
                           @Named("configurationManager") ConfigurationManager configurationManager,
                           @Named("eventListener") EventListener eventListener,
                           @Named("reportMetricsCase") JXTableCase reportMetricsCase,
                           @Named("reportColumnCase") JXTableCase reportColumnCase,
                           @Named("mapReportData") Map<ProfileTaskQueryKey, QueryReportData> mapReportData,
                           @Named("reportTaskPanel") ReportTabsPane reportTabsPane,
                           FilesHelper filesHelper,
                           ReportHelper reportHelper) {
        this.profileManager = profileManager;
        this.reportView = reportView;
        this.profileReportCase = profileReportCase;
        this.taskReportCase = taskReportCase;
        this.queryReportCase = queryReportCase;
        this.reportMetricsCase = reportMetricsCase;
        this.reportColumnCase = reportColumnCase;
        this.configurationManager = configurationManager;
        this.eventListener = eventListener;
        this.eventListener.addReportStateListener(this);
        this.profileReportCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.taskReportCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.queryReportCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.mapReportData = mapReportData;
        //this.profileCase.getJxTable().addMouseListener(this);

        this.reportTabsPane = reportTabsPane;
        this.designSaveDirs = new ArrayList<>();
        this.designReportDirs = new ArrayList<>();
        this.filesHelper = filesHelper;
        this.reportHelper = reportHelper;

    }

    @Override
    public void windowClosing(WindowEvent e) {
        log.info("Window report closing event received");
    }

    @Override
    public void fireShowReport(ReportState reportState) {
        if (reportState == ReportState.SHOW) {
            this.reportView.showReport();
        }
        if (reportState == ReportState.HIDE) {
            this.reportView.hideReport();
        }
    }

    public <T> void fillModel(Class<T> clazz) {
        if (ConfigClasses.Profile.equals(ConfigClasses.fromClass(clazz))) {
            log.info("Profile..");
            configurationManager.getConfigList(Profile.class)
                    .forEach(e -> profileReportCase.getDefaultTableModel().addRow(new Object[]{e.getId(), e.getName()}));

            if (profileReportCase.getDefaultTableModel().getRowCount() != 0) {
                profileReportCase.getJxTable().setRowSelectionInterval(0, 0);
            }

        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");
            } else {
                if (e.getSource() == profileReportCase.getJxTable().getSelectionModel()) {

                    taskReportCase.getDefaultTableModel().getDataVector().removeAllElements();
                    taskReportCase.getDefaultTableModel().fireTableDataChanged();
                    int profileId = GUIHelper.getIdByColumnName(profileReportCase.getJxTable(),
                            profileReportCase.getDefaultTableModel(),
                            listSelectionModel, ProfileColumnNames.ID.getColName());
                    if (profileId >= 0) {
                        profileManager.getProfileInfoById(profileId).getTaskInfoList()
                                .forEach(taskId -> {
                                    TaskInfo taskIn = profileManager.getTaskInfoById(taskId);
                                    if (Objects.isNull(taskIn)) {
                                        throw new NotFoundException("Not found profile: " + taskId);
                                    }
                                    taskReportCase.getDefaultTableModel().addRow(new Object[]{taskIn.getId(), taskIn.getName()});
                                });

                        if (taskReportCase.getDefaultTableModel().getRowCount() > 0) {
                            taskReportCase.getJxTable().setRowSelectionInterval(0, 0);
                        } else {
                            queryReportCase.getDefaultTableModel().getDataVector().removeAllElements();
                            queryReportCase.getDefaultTableModel().fireTableDataChanged();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Profile is not selected", "General Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                if (e.getSource() == taskReportCase.getJxTable().getSelectionModel()) {
                    queryReportCase.getDefaultTableModel().getDataVector().removeAllElements();
                    queryReportCase.getDefaultTableModel().fireTableDataChanged();

                    int taskId = GUIHelper.getIdByColumnName(taskReportCase.getJxTable(),
                            taskReportCase.getDefaultTableModel(), taskReportCase.getJxTable().getSelectionModel(),
                            TaskColumnNames.ID.getColName());

                    if (profileReportCase.getDefaultTableModel().getRowCount() > 0) {
                        if (Objects.isNull(profileManager.getTaskInfoById(taskId))) {
                            throw new NotFoundException("Not found task: " + taskId);
                        } else {
                            profileManager.getTaskInfoById(taskId)
                                    .getQueryInfoList()
                                    .forEach(queryId -> {
                                        QueryInfo queryIn = profileManager.getQueryInfoById(queryId);
                                        if (Objects.isNull(queryIn)) {
                                            throw new NotFoundException("Not found query: " + queryId);
                                        }
                                        queryReportCase.getDefaultTableModel().addRow(new Object[]{queryIn.getId(), queryIn.getName()});
                                    });
                        }
                        if (queryReportCase.getDefaultTableModel().getRowCount() > 0) {
                            queryReportCase.getJxTable().setRowSelectionInterval(0, 0);
                        } else {
                            queryReportCase.getDefaultTableModel().getDataVector().removeAllElements();
                            queryReportCase.getDefaultTableModel().fireTableDataChanged();
                        }
                    } else {
                        profileManager.getQueryInfoList()
                                .forEach(f -> queryReportCase.getDefaultTableModel().addRow(new Object[]{f.getId(), f.getName()}));
                    }
                }
                if (e.getSource() == queryReportCase.getJxTable().getSelectionModel()) {
                    reportMetricsCase.getDefaultTableModel().getDataVector().removeAllElements();
                    reportMetricsCase.getDefaultTableModel().fireTableDataChanged();

                    reportColumnCase.getDefaultTableModel().getDataVector().removeAllElements();
                    reportColumnCase.getDefaultTableModel().fireTableDataChanged();

                    int profileId = (int) profileReportCase.getDefaultTableModel()
                            .getValueAt(profileReportCase.getJxTable()
                                    .getSelectedRow(), profileReportCase.getDefaultTableModel()
                                    .findColumn(ProfileColumnNames.ID.getColName()));
                    int taskId = (int) taskReportCase.getDefaultTableModel()
                            .getValueAt(taskReportCase.getJxTable()
                                    .getSelectedRow(), taskReportCase.getDefaultTableModel()
                                    .findColumn(TaskColumnNames.ID.getColName()));
                    int queryId = GUIHelper.getIdByColumnName(queryReportCase.getJxTable(),
                            queryReportCase.getDefaultTableModel(),
                            listSelectionModel, TaskColumnNames.ID.getColName());

                    QueryInfo query = profileManager.getQueryInfoById(queryId);
                    List<Metric> metricList = query.getMetricList();

                    for (int i = 0; i < metricList.size(); i++) {
                        reportMetricsCase.getDefaultTableModel().addRow(new Object[0]);
                        reportMetricsCase.getDefaultTableModel().setValueAt(false, i, 1);
                        reportMetricsCase.getDefaultTableModel().setValueAt(metricList.get(i).getId(), i, 0);
                        reportMetricsCase.getDefaultTableModel().setValueAt(metricList.get(i).getName(), i, 2);
                    }


                    TableInfo table = profileManager.getTableInfoByTableName(query.getName());
                    if (table != null && table.getCProfiles() != null) {
                        table.getCProfiles().stream()
                                .filter(f -> !f.getCsType().isTimeStamp())
                                .forEach(cProfile -> reportColumnCase.getDefaultTableModel()
                                        .addRow(new Object[]{cProfile.getColId(), new Object[]{}, cProfile.getColName()}));
                    }

                    ProfileTaskQueryKey key = new ProfileTaskQueryKey(profileId, taskId, queryId);

                    if (!mapReportData.isEmpty()) {
                        if (mapReportData.containsKey(key)) {
                            QueryReportData val = mapReportData.get(key);
                            List<MetricReport> listMetric = val.getMetricReportList();
                            List<CProfileReport> listColumn = val.getCProfileReportList();

                            for (MetricReport m : listMetric) {
                                for (int row = 0; row < reportMetricsCase.getJxTable().getRowCount(); row++) {
                                    int metricId = (int) reportMetricsCase.getDefaultTableModel()
                                            .getValueAt(row, reportMetricsCase.getDefaultTableModel()
                                                    .findColumn(MetricsColumnNames.ID.getColName()));
                                    if (m.getId() == metricId) {
                                        reportMetricsCase.getJxTable().setValueAt(true, row, 0);
                                    }
                                }
                            }
                            reportMetricsCase.getJxTable().repaint();
                            reportMetricsCase.getJxTable().revalidate();

                            for (CProfileReport c : listColumn) {
                                for (int row = 0; row < reportColumnCase.getJxTable().getRowCount(); row++) {
                                    int columnId = (int) reportColumnCase.getDefaultTableModel()
                                            .getValueAt(row, reportColumnCase.getDefaultTableModel()
                                                    .findColumn(MetricsColumnNames.ID.getColName()));
                                    if (c.getColId() == columnId) {
                                        reportColumnCase.getJxTable().setValueAt(true, row, 0);
                                    }
                                }
                            }
                            reportColumnCase.getJxTable().repaint();
                            reportColumnCase.getJxTable().revalidate();
                        }
                    }

                }

            }
        }

    }
}

