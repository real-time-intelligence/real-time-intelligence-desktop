package ru.rti.desktop.view.handler.query;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import org.fbase.model.profile.table.IType;
import org.fbase.model.profile.table.TType;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.local.LoadDataMode;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.EditTabbedPane;
import ru.rti.desktop.prompt.Internationalization;
import ru.rti.desktop.view.handler.CommonViewHandler;
import ru.rti.desktop.view.handler.MouseListenerImpl;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.query.MainQueryPanel;
import ru.rti.desktop.view.panel.config.query.MetadataQueryPanel;
import ru.rti.desktop.view.panel.config.query.MetricQueryPanel;
import ru.rti.desktop.view.panel.config.query.QueryPanel;

@Log4j2
@Singleton
public class QuerySelectionHandler extends MouseListenerImpl implements ListSelectionListener, ItemListener,
    CommonViewHandler {

    private final ProfileManager profileManager;
    private final JXTableCase queryCase;
    private final JXTableCase taskCase;
    private final JXTableCase connectionCase;
    private final JXTableCase profileCase;
    private final MainQueryPanel mainQueryPanel;
    private final MetadataQueryPanel metadataQueryPanel;
    private final MetricQueryPanel metricQueryPanel;
    private final JTabbedPaneConfig jTabbedPaneConfig;
    private final QueryPanel queryPanel;
    private final JXTableCase configMetadataCase;
    private final ButtonPanel queryButtonPanel;
    private final JCheckBox checkboxConfig;
    private Boolean isSelected;
    private final ResourceBundle bundleDefault;

    @Inject
    public QuerySelectionHandler(@Named("profileManager") ProfileManager profileManager,
                                 @Named("jTabbedPaneConfig") JTabbedPaneConfig jTabbedPaneConfig,
                                 @Named("queryConfigPanel") QueryPanel queryPanel,
                                 @Named("queryConfigCase") JXTableCase queryCase,
                                 @Named("taskConfigCase") JXTableCase taskCase,
                                 @Named("connectionConfigCase") JXTableCase connectionCase,
                                 @Named("profileConfigCase") JXTableCase profileCase,
                                 @Named("configMetadataCase") JXTableCase configMetadataCase,
                                 @Named("queryButtonPanel") ButtonPanel queryButtonPanel,
                                 @Named("checkboxConfig") JCheckBox checkboxConfig,
                                 @Named("mainQueryPanel") MainQueryPanel mainQueryPanel,
                                 @Named("metadataQueryPanel") MetadataQueryPanel metadataQueryPanel,
                                 @Named("metricQueryPanel") MetricQueryPanel metricQueryPanel) {
        this.profileManager = profileManager;
        this.queryCase = queryCase;
        this.profileCase = profileCase;
        this.taskCase = taskCase;
        this.connectionCase = connectionCase;
        this.mainQueryPanel = mainQueryPanel;
        this.metadataQueryPanel = metadataQueryPanel;
        this.metricQueryPanel = metricQueryPanel;
        this.queryCase.getJxTable().getSelectionModel().addListSelectionListener(this);
        this.queryCase.getJxTable().addMouseListener(this);
        this.jTabbedPaneConfig = jTabbedPaneConfig;
        this.queryPanel = queryPanel;
        this.configMetadataCase = configMetadataCase;
        this.queryButtonPanel = queryButtonPanel;
        this.checkboxConfig = checkboxConfig;
        this.checkboxConfig.addItemListener(this);
        this.isSelected = false;

        this.bundleDefault = Internationalization.getInternationalizationBundle();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

        if (jTabbedPaneConfig.isEnabledAt(3)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.QUERY);
        }

        if (!e.getValueIsAdjusting()) {
            if (listSelectionModel.isSelectionEmpty()) {
                log.info("Clearing profile fields");
                mainQueryPanel.getQueryName().setEditable(false);
                mainQueryPanel.getQueryDescription().setEditable(false);
                mainQueryPanel.getQuerySqlText().setEditable(false);
                mainQueryPanel.getQueryGatherDataComboBox().setEnabled(false);
                mainQueryPanel.getQueryLoadDataModeComboBox().setEnabled(false);
                mainQueryPanel.getQueryName().setText("");
                mainQueryPanel.getQueryName().setPrompt(bundleDefault.getString("qName"));
                mainQueryPanel.getQueryDescription().setText("");
                mainQueryPanel.getQueryDescription().setPrompt(bundleDefault.getString("qDesc"));
                mainQueryPanel.getQuerySqlText().setText("");
                mainQueryPanel.getQuerySqlText().setPrompt(bundleDefault.getString("qSqlText"));
                metadataQueryPanel.getEditMetadata().setEnabled(false);
                metadataQueryPanel.getSaveMetadata().setEnabled(false);
                metadataQueryPanel.getConfigMetadataCase().getJxTable().setEditable(false);
                configMetadataCase.getDefaultTableModel().getDataVector().removeAllElements();
                configMetadataCase.getDefaultTableModel().fireTableDataChanged();
                metricQueryPanel.getConfigMetricCase().getDefaultTableModel().getDataVector().removeAllElements();
                metricQueryPanel.getConfigMetricCase().getDefaultTableModel().fireTableDataChanged();
                metricQueryPanel.getNameMetric().setText("");
                metricQueryPanel.getNameMetric().setPrompt(bundleDefault.getString("metricName"));
                metricQueryPanel.getDefaultCheckBox().setSelected(false);
            } else {
                int queryId = getSelectedQueryId(listSelectionModel);
                QueryInfo queryInfo = profileManager.getQueryInfoById(queryId);

                if (Objects.isNull(queryInfo)) {
                    throw new NotFoundException("Not found query: " + queryId);
                }

                mainQueryPanel.getQueryName().setText(queryInfo.getName());
                mainQueryPanel.getQueryDescription().setText(queryInfo.getDescription());
                mainQueryPanel.getQueryGatherDataComboBox().setSelectedItem(queryInfo.getGatherDataSql());

                if (queryInfo.getLoadDataMode() != null) {
                    mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(queryInfo.getLoadDataMode());
                } else {
                    mainQueryPanel.getQueryLoadDataModeComboBox().setSelectedItem(LoadDataMode.JDBC_REAL_TIME);
                }

                mainQueryPanel.getQuerySqlText().setText(queryInfo.getText());

                TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());

                List<CProfile> cProfileList = tableInfo.getCProfiles();

                List<String> xAxisList = new ArrayList<>();
                List<String> yAxisList = new ArrayList<>();
                List<String> dimensionList = new ArrayList<>();

                configMetadataCase.getDefaultTableModel().getDataVector().removeAllElements();
                configMetadataCase.getDefaultTableModel().fireTableDataChanged();

                if (cProfileList != null) {
                    fillConfigMetadata(tableInfo, configMetadataCase);

                    cProfileList.stream()
                            .filter(f -> !f.getCsType().isTimeStamp())
                            .forEach(cProfile -> xAxisList.add(cProfile.getColName()));

                    cProfileList.stream()
                            .filter(f -> !f.getCsType().isTimeStamp())
                            .forEach(cProfile -> yAxisList.add(cProfile.getColName()));

                    cProfileList.stream()
                            .filter(f -> !f.getCsType().isTimeStamp())
                            .forEach(cProfile -> dimensionList.add(cProfile.getColDbTypeName()));
                }

                DefaultComboBoxModel<String> cbModelY = new DefaultComboBoxModel<>();
                for (String s : yAxisList) {
                    cbModelY.addElement(s);
                }
                metricQueryPanel.getYComboBox().setModel(cbModelY);

                DefaultComboBoxModel<String> cbModelDimension = new DefaultComboBoxModel<>();
                for (String s : yAxisList) {
                    cbModelDimension.addElement(s);
                }
                metricQueryPanel.getDimensionComboBox().setModel(cbModelDimension);

                List<List<?>> connectionData = new LinkedList<>();
                profileManager.getTaskInfoList().stream()
                        .filter(f -> f.getQueryInfoList().stream().anyMatch(qId -> qId == queryId))
                        .findAny()
                        .ifPresentOrElse(t -> {
                                    ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(t.getConnectionId());
                                    if (Objects.isNull(connectionInfo)) {
                                        throw new NotFoundException("Not found task: " + t.getConnectionId());
                                    }
                                    connectionData.add(
                                            new ArrayList<>(Arrays.asList(connectionInfo.getName(),
                                                    connectionInfo.getUserName(),
                                                    connectionInfo.getUrl(),
                                                    connectionInfo.getJar(),
                                                    connectionInfo.getDriver())));
                                },
                                () -> log.info("No found query by query id: " + queryId));

                metadataQueryPanel.getEditMetadata().setEnabled(true);
                metadataQueryPanel.getQueryConnectionMetadataComboBox().setTableData(connectionData);
                metadataQueryPanel.getTableName().setText(tableInfo.getTableName());
                if (tableInfo.getTableType() != null) {
                    metadataQueryPanel.getTableType().setSelectedItem(tableInfo.getTableType());
                } else {
                    metadataQueryPanel.getTableType().setSelectedItem(TType.TIME_SERIES);
                }
                if (tableInfo.getIndexType() != null) {
                    metadataQueryPanel.getTableIndex().setSelectedItem(tableInfo.getIndexType());
                } else {
                    metadataQueryPanel.getTableIndex().setSelectedItem(IType.LOCAL);
                }

                metadataQueryPanel.getCompression().setEnabled(Boolean.FALSE);
                metadataQueryPanel.getCompression().setSelected(Boolean.TRUE.equals(tableInfo.getCompression()));

                List<List<?>> timestampListAll = new LinkedList<>();
                List<List<?>> timestampList = new LinkedList<>();
                timestampList.add(new ArrayList<>(Arrays.asList(" ", " ")));

                if (cProfileList != null) {
                    cProfileList.forEach(cProfile -> timestampListAll.add(
                            new ArrayList<>(Arrays.asList(cProfile.getColName(), cProfile.getCsType().getSType(),
                                    cProfile.getCsType().isTimeStamp()))));
                }

                timestampListAll.stream().filter(f -> f.get(2).equals(true))
                        .forEach(t -> timestampList.set(0, new ArrayList<>(Arrays.asList(t.get(0), t.get(1)))));
                timestampListAll.stream().filter(f -> f.get(2).equals(false))
                        .forEach(t -> timestampList.add(new ArrayList<>(Arrays.asList(t.get(0), t.get(1)))));

                metadataQueryPanel.getTimestampComboBox().setTableData(timestampList);
                metricQueryPanel.getXTextFile().setText((String) timestampList.get(0).get(0));

                metricQueryPanel.getConfigMetricCase().getDefaultTableModel().getDataVector().removeAllElements();
                metricQueryPanel.getConfigMetricCase().getDefaultTableModel().fireTableDataChanged();
                List<Metric> metrics = queryInfo.getMetricList();
                for (Metric m : metrics) {
                    metricQueryPanel.getConfigMetricCase().getDefaultTableModel()
                            .addRow(new Object[]{m.getId(),
                                    m.getName(),
                                    m.getIsDefault(),
                                    m.getXAxis().getColName(),
                                    m.getYAxis().getColName(),
                                    m.getGroup().getColName(),
                                    m.getMetricFunction().toString(),
                                    m.getChartType().toString()});
                }
                if (metricQueryPanel.getConfigMetricCase().getJxTable().getRowCount() != 0) {
                    metricQueryPanel.getConfigMetricCase().getJxTable().setRowSelectionInterval(0, 0);
                } else {
                    metricQueryPanel.getNameMetric().setText("");
                    metricQueryPanel.getNameMetric().setPrompt(bundleDefault.getString("metricName"));
                }

                GUIHelper.disableButton(queryButtonPanel, !isSelected);
                metadataQueryPanel.getEditMetadata().setEnabled(!isSelected);
                metadataQueryPanel.getLoadMetadata().setEnabled(!isSelected);
                GUIHelper.disableButton(metricQueryPanel.getMetricQueryButtonPanel(), !isSelected);

                if (queryCase.getDefaultTableModel().getRowCount() > 0) {
                    profileManager.getTaskInfoList()
                            .stream()
                            .flatMap(t -> t.getQueryInfoList().stream())
                            .distinct()
                            .filter(f -> f == queryInfo.getId())
                            .findAny()
                            /*.ifPresentOrElse(q -> {
                                        metadataQueryPanel.getEditMetadata().setEnabled(true);
                                        metadataQueryPanel.getLoadMetadata().setEnabled(true);
                                    },
                                    () -> {
                                        metadataQueryPanel.getEditMetadata().setEnabled(false);
                                        metadataQueryPanel.getLoadMetadata().setEnabled(false);
                                    })*/;
                }

            }
        }
    }

    private int getSelectedQueryId(ListSelectionModel listSelectionModel) {
        return GUIHelper.getIdByColumnName(queryCase.getJxTable(),
                queryCase.getDefaultTableModel(),
                listSelectionModel, TaskColumnNames.ID.getColName());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (jTabbedPaneConfig.isEnabledAt(3)) {
            jTabbedPaneConfig.setSelectedTab(EditTabbedPane.QUERY);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            GUIHelper.disableButton(queryButtonPanel, false);
            metadataQueryPanel.getEditMetadata().setEnabled(false);
            metadataQueryPanel.getLoadMetadata().setEnabled(false);
            GUIHelper.disableButton(metricQueryPanel.getMetricQueryButtonPanel(), false);
            isSelected = true;
        } else {
            GUIHelper.disableButton(queryButtonPanel, true);
            metadataQueryPanel.getEditMetadata().setEnabled(true);
            metadataQueryPanel.getLoadMetadata().setEnabled(true);
            GUIHelper.disableButton(metricQueryPanel.getMetricQueryButtonPanel(), true);
            isSelected = false;
        }
    }

}


