package ru.rti.desktop.config.view;

import static ru.rti.desktop.model.view.TemplateAction.LOAD;
import static ru.rti.desktop.model.view.TemplateAction.SAVE;

import dagger.Module;
import dagger.Provides;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import org.fbase.model.profile.table.IType;
import org.fbase.model.profile.table.TType;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTextArea;
import org.jetbrains.annotations.NotNull;
import ru.rti.desktop.custom.DetailedComboBox;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.column.ConnectionColumnNames;
import ru.rti.desktop.model.column.MetadataColumnNames;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.ProfileColumnNames;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.column.ReportColumnNames;
import ru.rti.desktop.model.column.TaskColumnNames;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;
import ru.rti.desktop.model.local.LoadDataMode;
import ru.rti.desktop.model.report.QueryReportData;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.pane.JTabbedPaneConfig;
import ru.rti.desktop.view.panel.DateTimePicker;
import ru.rti.desktop.view.panel.config.ButtonPanel;
import ru.rti.desktop.view.panel.config.profile.MultiSelectTaskPanel;
import ru.rti.desktop.view.panel.config.query.MainQueryPanel;
import ru.rti.desktop.view.panel.config.query.MetadataQueryPanel;
import ru.rti.desktop.view.panel.config.query.MetricQueryPanel;
import ru.rti.desktop.view.panel.config.task.MultiSelectQueryPanel;
import ru.rti.desktop.view.panel.report.PathPdfInfo;


@Module
public class ConfigurationConfig {

    @Provides
    @Singleton
    @Named("profileSqlTextJScrollPane")
    public JScrollPane getSqlTextInJScrollPane(@Named("profileSqlTextJTextArea") JTextArea jTextArea) {
        return GUIHelper.getTextInJScrollPane(jTextArea);
    }

    @Provides
    @Singleton
    @Named("profileSqlTextJTextArea")
    public JTextArea getProfileSqlTextJTextArea() {
        return GUIHelper.getJTextArea(10, 60);
    }

    @Provides
    @Singleton
    @Named("profileSqlDescJScrollPane")
    public JScrollPane getSqlDescInJScrollPane(@Named("profileSqlDescJTextArea") JTextArea jTextArea) {
        return GUIHelper.getTextInJScrollPane(jTextArea);
    }

    @Provides
    @Singleton
    @Named("profileSqlDescJTextArea")
    public JTextArea getProfileSqlDescJTextArea() {
        return GUIHelper.getJTextArea(4, 7);
    }


    @Provides
    @Singleton
    @Named("profileUrlJTextField")
    public JTextField getProfileUrlJTextField() {
        return new JTextField();
    }


    @Provides
    @Singleton
    @Named("profileSqlViewCase")
    public JXTableCase getProfileSqlViewCase() {
        return GUIHelper.getJXTableCase(6,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
    }

    @Provides
    @Singleton
    @Named("profileConfigCase")
    public JXTableCase getProfileConfigCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(10,
                new String[]{ProfileColumnNames.ID.getColName(), ProfileColumnNames.NAME.getColName(),});
        jxTableCase.getJxTable().getTableHeader().setVisible(false);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("taskConfigCase")
    public JXTableCase getTaskConfigCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(10,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
        jxTableCase.getJxTable().getTableHeader().setVisible(false);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("connectionConfigCase")
    public JXTableCase getConnectionConfigCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(10,
                new String[]{TaskColumnNames.ID.getColName(),
                        TaskColumnNames.NAME.getColName(),});
        jxTableCase.getJxTable().getTableHeader().setVisible(false);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("queryConfigCase")
    public JXTableCase getQueryConfigCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(10,
                new String[]{TaskColumnNames.ID.getColName(),
                        TaskColumnNames.NAME.getColName(),});
        jxTableCase.getJxTable().getTableHeader().setVisible(false);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("templateTaskCase")
    public JXTableCase getTemplateTaskCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{TaskColumnNames.ID.getColName(),
                        TaskColumnNames.NAME.getColName(),
                        TaskColumnNames.PULL_TIMEOUT.getColName()});
        jxTableCase.getJxTable().getTableHeader().setVisible(true);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("templateTaskDescription")
    public JXTextArea getTemplateTaskDescription() {
        JXTextArea taskDescription = new JXTextArea("Task description...");
        taskDescription.setEditable(false);
        taskDescription.setBorder(GUIHelper.getBorder());
        taskDescription.setRows(5);
        taskDescription.setColumns(1);
        taskDescription.setLineWrap(true);
        return taskDescription;
    }

    @Provides
    @Singleton
    @Named("templateQueryDescription")
    public JXTextArea getTemplateQueryDescription() {
        JXTextArea queryDescription = new JXTextArea("Query description...");
        queryDescription.setEditable(false);
        queryDescription.setBorder(GUIHelper.getBorder());
        queryDescription.setRows(5);
        queryDescription.setColumns(1);
        queryDescription.setLineWrap(true);
        return queryDescription;
    }

    @Provides
    @Singleton
    @Named("templateProfileDescSave")
    public JXTextArea getTemplateProfileDescSave() {
        JXTextArea profileDescription = new JXTextArea("Profile description");
        profileDescription.setRows(6);
        profileDescription.setColumns(1);
        profileDescription.setLineWrap(true);
        return profileDescription;
    }

    @Provides
    @Singleton
    @Named("templateTaskDescSave")
    public JXTextArea getTemplateTaskDescSave() {
        JXTextArea taskDescription = new JXTextArea("Task description");
        taskDescription.setRows(6);
        taskDescription.setColumns(1);
        taskDescription.setLineWrap(true);
        return taskDescription;
    }

    @Provides
    @Singleton
    @Named("templateQueryText")
    public JXTextArea getTemplateQueryText() {
        JXTextArea queryText = new JXTextArea("Text...");
        queryText.setEditable(false);
        queryText.setBorder(GUIHelper.getBorder());
        queryText.setRows(15);
        queryText.setColumns(1);
        queryText.setLineWrap(true);
        queryText.setAutoscrolls(true);
        return queryText;
    }

    @Provides
    @Singleton
    @Named("templateConnCase")
    public JXTableCase getTemplateConnCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{ConnectionColumnNames.ID.getColName(), ConnectionColumnNames.NAME.getColName(),});
        jxTableCase.getJxTable().getTableHeader().setVisible(true);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("templateQueryCase")
    public JXTableCase getTemplateQueryCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{QueryColumnNames.ID.getColName(),
                        QueryColumnNames.NAME.getColName(),
                        QueryColumnNames.GATHER.getColName(),
                        QueryColumnNames.MODE.getColName()});
        jxTableCase.getJxTable().getTableHeader().setVisible(true);
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("templateLoadJButton")
    public JButton getTemplateLoadJButton() {
        JButton jButton = new JButton();
        jButton.setActionCommand(LOAD.name());
        jButton.setMnemonic('L');
        jButton.setText("Load");
        jButton.setPreferredSize(new Dimension(120, 30));
        return jButton;
    }

    @Provides
    @Singleton
    @Named("templateSaveJButton")
    public JButton getTemplateSaveJButton() {
        JButton jButton = new JButton();
        jButton.setActionCommand(SAVE.name());
        jButton.setMnemonic('S');
        jButton.setText("Save");
        jButton.setPreferredSize(new Dimension(120, 30));
        return jButton;
    }

    @Provides
    @Singleton
    @Named("jTabbedPaneConfig")
    public JTabbedPaneConfig getJTabbedPaneConfig() {
        return new JTabbedPaneConfig();
    }

    @Provides
    @Singleton
    @Named("profileButtonPanel")
    public ButtonPanel getProfileButtonPanel() {
        return new ButtonPanel();
    }

    @Provides
    @Singleton
    @Named("taskButtonPanel")
    public ButtonPanel getTaskButtonPanel() {
        return new ButtonPanel();
    }

    @Provides
    @Singleton
    @Named("connectionButtonPanel")
    public ButtonPanel getConnectionButtonPanel() {
        return new ButtonPanel();
    }

    @Provides
    @Singleton
    @Named("queryButtonPanel")
    public ButtonPanel getQueryButtonPanel() {
        return new ButtonPanel();
    }

    @Provides
    @Singleton
    @Named("metricQueryButtonPanel")
    public ButtonPanel getMetricQueryButtonPanel() {
        return new ButtonPanel();
    }

    @Provides
    @Singleton
    @Named("taskConnectionComboBox")
    public DetailedComboBox getTaskConnectionDetailedComboBox() {
        String[] columns = new String[]{"Name", "Username", "Url", "Jar", "Driver"};
        int[] widths = new int[]{131, 131, 132, 132, 132};
        return new DetailedComboBox(columns, widths, 0);
    }

    @Provides
    @Singleton
    @Named("queryConnectionMetadataComboBox")
    public DetailedComboBox getQueryConnectionMetadataComboBox() {
        String[] columns = new String[]{"Name", "Username", "Url", "Jar", "Driver"};
        int[] widths = new int[]{131, 131, 132, 132, 132};
        DetailedComboBox detailedComboBox = new DetailedComboBox(columns, widths, 0);
        detailedComboBox.setPreferredSize(new Dimension(new Dimension(120, 26)));
        return detailedComboBox;
    }

    @Provides
    @Singleton
    @Named("taskListCase")
    public JXTableCase getTaskListEditCase() {
        return GUIHelper.getJXTableCase(6,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
    }

    @Provides
    @Singleton
    @Named("templateListTaskCase")
    public JXTableCase getTemplateListTaskCase() {
        return GUIHelper.getJXTableCase(6,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
    }

    @Provides
    @Singleton
    @Named("selectedTaskCase")
    public JXTableCase getSelectedTaskCase() {
        return GUIHelper.getJXTableCase(6,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
    }

    @Provides
    @Singleton
    @Named("multiSelectPanel")
    public MultiSelectTaskPanel getMultiSelectTaskPanel(
            @Named("taskListCase") JXTableCase taskListEditCase,
            @Named("selectedTaskCase") JXTableCase selectedTaskEditCase,
            @Named("templateListTaskCase") JXTableCase templateListTaskCase
    ) {
        return new MultiSelectTaskPanel(taskListEditCase, selectedTaskEditCase, templateListTaskCase);
    }

    @Provides
    @Singleton
    @Named("queryListCase")
    public JXTableCase getQueryListEditCase() {
        return GUIHelper.getJXTableCase(10,
                new String[]{QueryColumnNames.ID.getColName(), QueryColumnNames.NAME.getColName(),
                        QueryColumnNames.DESCRIPTION.getColName(),
                        QueryColumnNames.TEXT.getColName()});
    }

    @Provides
    @Singleton
    @Named("selectedQueryCase")
    public JXTableCase getSelectedQueryEditCase() {
        return GUIHelper.getJXTableCase(10,
                new String[]{QueryColumnNames.ID.getColName(), QueryColumnNames.NAME.getColName(),
                        QueryColumnNames.DESCRIPTION.getColName(),
                        QueryColumnNames.TEXT.getColName()});
    }

    @Provides
    @Singleton
    @Named("multiSelectQueryPanel")
    public MultiSelectQueryPanel getMultiSelectQueryPanel(
            @Named("queryListCase") JXTableCase queryListCase,
            @Named("selectedQueryCase") JXTableCase selectedQueryCase,
            @Named("templateListQueryCase") JXTableCase templateListQueryCase) {
        return new MultiSelectQueryPanel(queryListCase, selectedQueryCase, templateListQueryCase);
    }

    @Provides
    @Singleton
    @Named("templateListQueryCase")
    public JXTableCase getTemplateListQueryCase() {
        return GUIHelper.getJXTableCase(10,
                new String[]{QueryColumnNames.ID.getColName(), QueryColumnNames.NAME.getColName(),
                        QueryColumnNames.DESCRIPTION.getColName(),
                        QueryColumnNames.TEXT.getColName()});
    }

    @Provides
    @Singleton
    @Named("mainQueryTab")
    public JTabbedPane getMainQueryTab() {
        return new JTabbedPane();
    }

    @Provides
    @Singleton
    @Named("configMetadataCase")
    public JXTableCase getProfileMetadataCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCaseCheckBox(10, new String[]{MetadataColumnNames.COLUMN_ID.getColName(),
                MetadataColumnNames.COLUMN_ID_SQL.getColName(), MetadataColumnNames.COLUMN.getColName(),
                MetadataColumnNames.COLUMN_DB_TYPE_NAME.getColName(), MetadataColumnNames.STORAGE.getColName(),
                MetadataColumnNames.COLUMN_TYPE.getColName(), MetadataColumnNames.VALUABLE.getColName()}, 6);
        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);
        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);

        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("querySqlText")
    public JXTextArea getQuerySqlText() {
        return GUIHelper.getJXTextArea(5, 60);
    }

    @Provides
    @Singleton
    @Named("queryGatherDataComboBox")
    public JComboBox<?> getGatherDataSqlEditComboBox() {
        return new JComboBox(GatherDataSql.values());
    }

    @Provides
    @Singleton
    @Named("timestampComboBox")
    public DetailedComboBox getTimestampComboBox() {
        String[] columns = new String[]{"Name", "Storage type"};
        int[] widths = new int[]{120, 120};
        return new DetailedComboBox(columns, widths, 0);
    }

    @Provides
    @Singleton
    @Named("queryLoadDataModeComboBox")
    public JComboBox<?> getLoadDataModeEditComboBox() {
        return new JComboBox(LoadDataMode.values());
    }

    @Provides
    @Singleton
    @Named("checkboxConfig")
    public JCheckBox getCheckboxEdit() {
        return new JCheckBox("View the hierarchy");
    }

    @Provides
    @Singleton
    @Named("tableType")
    public JComboBox<TType> getTableType() {
        return new JComboBox<>(TType.values());
    }

    @Provides
    @Singleton
    @Named("indexType")
    public JComboBox<IType> getIndexType() {
        return new JComboBox<>(IType.values());
    }

    @Provides
    @Singleton
    @Named("mainQueryPanel")
    public MainQueryPanel getMainQueryPanel(
            @Named("queryButtonPanel") ButtonPanel queryEditButtonPanel,
            @Named("queryGatherDataComboBox") JComboBox<?> gatherDataSqlEditComboBox,
            @Named("querySqlText") JXTextArea querySqlText,
            @Named("queryLoadDataModeComboBox") JComboBox<?> loadDataModeEditComboBox
    ) {
        return new MainQueryPanel(queryEditButtonPanel, gatherDataSqlEditComboBox,
                querySqlText, loadDataModeEditComboBox);
    }

    @Provides
    @Singleton
    @Named("metadataQueryPanel")
    public MetadataQueryPanel getMetadataQueryPanel(
            @Named("configMetadataCase") JXTableCase configMetadataCase,
            @Named("queryConnectionMetadataComboBox") DetailedComboBox queryConnectionMetadataComboBox,
            @Named("timestampComboBox") DetailedComboBox timestampComboBox,
            @Named("tableType") JComboBox<TType> tableType,
            @Named("indexType") JComboBox<IType> tableIndex
    ) {
        return new MetadataQueryPanel(configMetadataCase,
                queryConnectionMetadataComboBox,
                timestampComboBox, tableType, tableIndex);
    }

    @Provides
    @Singleton
    @Named("metricQueryPanel")
    public MetricQueryPanel getMetricQueryPanel(
            @Named("metricQueryButtonPanel") ButtonPanel metricQueryButtonPanel,
            @Named("configMetricCase") JXTableCase profileMetricsEditCase,
            @Named("metricFunction") JComboBox<?> yAisFunComboBox,
            @Named("chartType") JComboBox<?> chartType
    ) {
        return new MetricQueryPanel(metricQueryButtonPanel, profileMetricsEditCase, yAisFunComboBox, chartType);
    }

    @Provides
    @Singleton
    @Named("configMetricCase")
    public JXTableCase getProfileMetricsEditCase() {
        return getMetricsCase();
    }

    @Provides
    @Singleton
    @Named("templateMetricsCase")
    public JXTableCase getTemplateMetricsCase() {
        return getMetricsCase();
    }

    @NotNull
    private JXTableCase getMetricsCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCaseCheckBox(7, new String[]{MetricsColumnNames.ID.getColName(),
                MetricsColumnNames.NAME.getColName(), MetricsColumnNames.IS_DEFAULT.getColName(),
                MetricsColumnNames.X_AXIS.getColName(), MetricsColumnNames.Y_AXIS.getColName(),
                MetricsColumnNames.GROUP.getColName(), MetricsColumnNames.METRIC_FUNCTION.getColName(),
                MetricsColumnNames.CHART_TYPE.getColName()}, 2);
        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);
        jxTableCase.getJxTable().setEditable(false);

        TableColumn col = jxTableCase.getJxTable().getColumnModel().getColumn(1);
        col.setMinWidth(30);
        col.setMaxWidth(50);

        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("metricFunction")
    public JComboBox<?> getYAisFunComboBox() {
        return new JComboBox(MetricFunction.values());
    }

    @Provides
    @Singleton
    @Named("chartType")
    public JComboBox<?> getChartType() {
        return new JComboBox(ChartType.values());
    }

    @Provides
    @Singleton
    @Named("connectionTemplateCase")
    public JXTableCase getConnectionTemplateEditCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7, new String[]{ConnectionColumnNames.ID.getColName(),
                ConnectionColumnNames.NAME.getColName(), ConnectionColumnNames.USER_NAME.getColName(),
                ConnectionColumnNames.PASSWORD.getColName(), ConnectionColumnNames.URL.getColName(),
                ConnectionColumnNames.JAR.getColName(), ConnectionColumnNames.DRIVER.getColName()});
        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);

        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("profileReportCase")
    public JXTableCase getProfileReportCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{ProfileColumnNames.ID.getColName(), ProfileColumnNames.NAME.getColName(),});
        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("taskReportCase")
    public JXTableCase getTaskReportCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{TaskColumnNames.ID.getColName(), TaskColumnNames.NAME.getColName(),});
        return jxTableCase;
    }


    @Provides
    @Singleton
    @Named("queryReportCase")
    public JXTableCase getQueryReportCase() {
        return getReportQueryCase();
    }

    @NotNull
    private JXTableCase getReportQueryCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCaseCheckBox(7,
                new String[]{QueryColumnNames.ID.getColName(),
                             QueryColumnNames.PICK.getColName(),
                             QueryColumnNames.NAME.getColName()}, 1);

        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);

        TableColumn col = jxTableCase.getJxTable().getColumnModel().getColumn(0);
        col.setMinWidth(30);
        col.setMaxWidth(35);

        return jxTableCase;
    }


    @Provides
    @Singleton
    @Named("metricsCheckBox")
    public List<JCheckBox> getMetricsCheckBox() {
        List<JCheckBox> jCheckBoxList = new ArrayList<>();
        return jCheckBoxList;
    }

    @Provides
    @Singleton
    @Named("columnsCheckBox")
    public List<JCheckBox> getColumnsCheckBox() {
        List<JCheckBox> jCheckBoxList = new ArrayList<>();
        return jCheckBoxList;
    }

    @Provides
    @Singleton
    @Named("dateTimePickerFrom")
    public DateTimePicker getDateTimePickerFrom() {
        return new DateTimePicker();
    }

    @Provides
    @Singleton
    @Named("dateTimePickerTo")
    public DateTimePicker getDateTimePickerTo() {
        return new DateTimePicker();
    }

    @Provides
    @Singleton
    @Named("mapReportData")
    public Map<ProfileTaskQueryKey, QueryReportData> getMapReportData() {
        Map<ProfileTaskQueryKey, QueryReportData> map = new HashMap<>();
        return map;
    }

    @Provides
    @Singleton
    @Named("containerCardDesign")
    public JXTaskPaneContainer getContainerCardDesign() {
        JXTaskPaneContainer container = new JXTaskPaneContainer();
        return container;
    }

    @Provides
    @Singleton
    @Named("reportSaveComboBox")
    public JComboBox<String> getReportSaveComboBox() {
        JComboBox<String> jComboBox = new JComboBox();
        jComboBox.setPreferredSize(new Dimension(200, 26));
        return jComboBox;
    }

    @Provides
    @Singleton
    @Named("designSaveDirs")
    public List<File> getDesignSaveDirs() {
        List<File> dirsList = new ArrayList<>();
        return dirsList;
    }


    @Provides
    @Singleton
    @Named("reportPdfPath")
    public PathPdfInfo getReportPdfPathInfo() {
        return new PathPdfInfo(" ");
    }

    @Provides
    @Singleton
    @Named("reportComboBox")
    public JComboBox<String> getReportComboBox() {
        JComboBox<String> jComboBox = new JComboBox();
        jComboBox.setPreferredSize(new Dimension(200, 26));
        return jComboBox;
    }

    @Provides
    @Singleton
    @Named("savedReportCase")
    public JXTableCase getSavedReportCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(10,
                new String[]{ReportColumnNames.REPORT_NAME.getColName(),});

        return jxTableCase;
    }

    @Provides
    @Singleton
    @Named("designReportCase")
    public JXTableCase getDesignReportCase() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(7,
                new String[]{"Design name",});

        return jxTableCase;
    }
}
