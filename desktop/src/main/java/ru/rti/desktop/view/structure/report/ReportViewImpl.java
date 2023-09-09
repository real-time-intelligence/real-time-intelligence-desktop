package ru.rti.desktop.view.structure.report;

import dagger.Lazy;
import lombok.extern.log4j.Log4j2;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;

import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.config.*;
import ru.rti.desktop.model.report.QueryReportData;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.BaseFrame;
import ru.rti.desktop.view.handler.report.design.DesignPanelHandler;
import ru.rti.desktop.view.handler.report.report.ReportPanelHandler;
import ru.rti.desktop.view.panel.report.ReportTabsPane;
import ru.rti.desktop.view.structure.ReportView;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;


@Log4j2
@Singleton
public class ReportViewImpl extends JDialog implements ReportView {
    private final Lazy<BaseFrame> jFrame;
    private final Lazy<ReportPresenter> reportPresenter;
    private final JXTableCase profileReportCase;
    private final JXTableCase taskReportCase;
    private final JXTableCase queryReportCase;
    private final JTabbedPane metricAndColReport;
    private final ReportTabsPane reportTabsPane;
    private final JXTableCase reportMetricsCase;
    private final JXTableCase reportColumnCase;
    private final DesignPanelHandler designPanelHandler;
    private final ReportPanelHandler reportPanelHandler;
    private final ProfileManager profileManager;
    private final Map<ProfileTaskQueryKey, QueryReportData> mapReportData;


    @Inject
    public ReportViewImpl(Lazy<BaseFrame> jFrame,
                          Lazy<ReportPresenter> reportPresenter,
                          @Named("profileReportCase") JXTableCase profileReportCase,
                          @Named("taskReportCase") JXTableCase taskReportCase,
                          @Named("queryReportCase") JXTableCase queryReportCase,
                          @Named("reportMetricsCase") JXTableCase reportMetricsCase,
                          @Named("reportColumnCase") JXTableCase reportColumnCase,
                          @Named("reportTaskPanel") ReportTabsPane reportTabsPane,
                          @Named("metricAndColReport") JTabbedPane metricAndColReport,
                          @Named("profileManager") ProfileManager profileManager,
                          @Named("mapReportData") Map<ProfileTaskQueryKey, QueryReportData> mapReportData,
                          @Named("designPanelHandler") DesignPanelHandler designPanelHandler,
                          @Named("reportPanelHandler") ReportPanelHandler reportPanelHandler
    ) {
        this.jFrame = jFrame;
        this.reportPresenter = reportPresenter;

        this.profileReportCase = profileReportCase;
        this.taskReportCase = taskReportCase;
        this.queryReportCase = queryReportCase;
        this.metricAndColReport = metricAndColReport;
        this.reportMetricsCase = reportMetricsCase;
        this.reportColumnCase = reportColumnCase;
        this.metricAndColReport.add("Metrics", reportMetricsCase.getJScrollPane());
        this.metricAndColReport.setMnemonicAt(0, KeyEvent.VK_M);
        this.metricAndColReport.add("Columns", reportColumnCase.getJScrollPane());
        this.metricAndColReport.setMnemonicAt(1, KeyEvent.VK_N);
        this.profileManager = profileManager;
        this.mapReportData = mapReportData;

        this.reportTabsPane = reportTabsPane;
        this.reportTabsPane.setMnemonicAt(0, KeyEvent.VK_D);
        this.reportTabsPane.setMnemonicAt(1, KeyEvent.VK_R);

        this.designPanelHandler = designPanelHandler;
        this.reportPanelHandler = reportPanelHandler;

        PainlessGridBag gbl = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row()
                .cell(fillEntitiesPane()).fillX();
        gbl.row()
                .cellXYRemainder(fillReportPane()).fillXY();
        gbl.done();

        this.setTitle("Report");

        this.packReport(false);
    }

    private JPanel fillReportPane() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(new EtchedBorder());
        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gbl.row()
                .cell(reportTabsPane).fillXY();

        gbl.done();
        return jPanel;
    }


    private JPanel fillEntitiesPane() {
        JPanel panel = new JPanel();
        panel.setBorder(new EtchedBorder());

        PainlessGridBag gbl = new PainlessGridBag(panel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gbl.row()
                .cell(new JXTitledSeparator("Profile")).fillX()
                .cell(new JXTitledSeparator("Task")).fillX()
                .cell(new JXTitledSeparator("Query")).fillX()
                .cell(new JXTitledSeparator("Metrics and Columns")).fillX();
        gbl.row()
                .cell(profileReportCase.getJScrollPane()).fillXY()
                .cell(taskReportCase.getJScrollPane()).fillXY()
                .cell(queryReportCase.getJScrollPane()).fillXY()
                .cell(metricAndColReport).fillXY();

        gbl.done();

        return panel;
    }


    @Override
    public void hideReport() {
        this.packReport(false);
    }

    @Override
    public void showReport() {
        this.packReport(true);
    }


    private void packReport(boolean visible) {
        this.reportTabsPane.refresh();
        this.setVisible(visible);
        this.setModal(true);
        this.setResizable(true);
        this.pack();

        this.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 400,
                Toolkit.getDefaultToolkit().getScreenSize().height - 100));
        this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    }


    @Override
    public void bindPresenter() {

        this.reportPresenter.get().fillModel(Profile.class);
        profileReportCase.getJxTable().getColumnExt(0).setVisible(false);
        profileReportCase.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());

        this.reportPresenter.get().fillModel(Task.class);
        taskReportCase.getJxTable().getColumnExt(0).setVisible(false);
        taskReportCase.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());


        this.reportPresenter.get().fillModel(Query.class);
        queryReportCase.getJxTable().getColumnExt(0).setVisible(false);
        queryReportCase.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());

        addWindowListener(reportPresenter.get());

    }


}
