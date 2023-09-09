package ru.rti.desktop.view.panel.report;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ReportTabbedPane;
import ru.rti.desktop.view.panel.DateTimePicker;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class ReportTabsPane extends JTabbedPane {

    private final JButton saveBtnPDFReport;
    private final JXTaskPaneContainer containerCardDesign;
    private final JXTaskPaneContainer containerChartCardDesign;
    private final JSplitPane jspDesign;
    private final JSplitPane jspMenuDesign;
    private final JSplitPane jspReportV;
    private final JSplitPane jspReportH;
    private final DateTimePicker dateTimePickerFrom;
    private final DateTimePicker dateTimePickerTo;
    private final JLabel lblFromDesign;
    private final JLabel lblToDesign;
    private final JButton saveBtnDesign;
    private final JButton openBtnDesign;
    private final JButton clearBtnDesign;
    private final JButton showBtnDesign;
    private final JButton collapseBtnDesign;
    private final JButton generateReportBtnDesign;
    private final JXTableCase savedReportCase;
    private final JComboBox<String> reportSaveComboBox;
    private final ReportHelper reportHelper;
    private final JScrollPane scrollPanePDF;
    private String reportPdfPath;


    @Inject
    public ReportTabsPane(@Named("dateTimePickerFrom") DateTimePicker dateTimePickerFrom,
                          @Named("dateTimePickerTo") DateTimePicker dateTimePickerTo,
                          @Named("containerCardDesign") JXTaskPaneContainer containerCardDesign,
                          @Named("containerChartCardDesign") JXTaskPaneContainer containerChartCardDesign,
                          @Named("reportSaveComboBox") JComboBox<String> reportSaveComboBox,
                          @Named("savedReportCase") JXTableCase savedReportCase,
                          ReportHelper reportHelper) {

        this.reportHelper = reportHelper;
        this.reportPdfPath = " ";

        //Component of the design panel
        this.jspDesign = GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 200);
        this.jspDesign.setResizeWeight(0);
        this.jspDesign.setResizeWeight(1);
        this.jspMenuDesign = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 35);

        this.containerCardDesign = containerCardDesign;

        SimpleDateFormat format = new SimpleDateFormat(reportHelper.getFormatPattern());
        this.dateTimePickerFrom = dateTimePickerFrom;

        Date date = new Date();
        this.dateTimePickerFrom.setFormats(format);
        this.dateTimePickerFrom.setTimeFormat(format);
        UIManager.put(CalendarHeaderHandler.uiControllerID, SpinningCalendarHeaderHandler.class.getName());
        this.dateTimePickerFrom.getMonthView().setZoomable(true); //this is needed for custom header
        this.dateTimePickerFrom.setDate(date);

        this.dateTimePickerTo = dateTimePickerTo;
        this.dateTimePickerTo.setFormats(format);
        this.dateTimePickerTo.setTimeFormat(format);
        UIManager.put(CalendarHeaderHandler.uiControllerID, SpinningCalendarHeaderHandler.class.getName());
        this.dateTimePickerTo.getMonthView().setZoomable(true);
        this.dateTimePickerTo.setDate(date);

        this.lblFromDesign = new JLabel("From");
        this.lblToDesign = new JLabel("To");
        this.saveBtnDesign = new JButton("Save");
        this.saveBtnDesign.setMnemonic('S');
        this.saveBtnDesign.setEnabled(false);
        this.openBtnDesign = new JButton("Open");
        this.openBtnDesign.setMnemonic('O');
        this.clearBtnDesign = new JButton("Clear");
        this.clearBtnDesign.setMnemonic('C');
        this.clearBtnDesign.setEnabled(false);
        this.showBtnDesign = new JButton("Show");
        this.showBtnDesign.setMnemonic('H');
        this.collapseBtnDesign = new JButton("Collapse all");
        this.collapseBtnDesign.setMnemonic('A');
        this.collapseBtnDesign.setVisible(false);
        this.generateReportBtnDesign = new JButton("Report");
        this.generateReportBtnDesign.setMnemonic('T');
        this.generateReportBtnDesign.setToolTipText("Generate report");
        this.generateReportBtnDesign.setEnabled(false);

        this.containerChartCardDesign = containerChartCardDesign;

        this.reportSaveComboBox = reportSaveComboBox;


        //Component of the report panel
        this.jspReportH = GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 200);
        this.jspReportV = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 35);
        this.saveBtnPDFReport = new JButton("Save");
        this.saveBtnPDFReport.setMnemonic('S');
        this.saveBtnPDFReport.setEnabled(false);
        this.savedReportCase = savedReportCase;
        this.scrollPanePDF = new JScrollPane();
        this.scrollPanePDF.setWheelScrollingEnabled(true);
        JScrollBar verticalScrollBar = scrollPanePDF.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        JScrollBar horizontalScrollBar = scrollPanePDF.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(16);

        this.add("Design", fillDesignPanel());
        this.add("Report", fillReportPanel());
    }

    private JPanel fillDesignPanel() {
        JPanel jPanel = new JPanel();
        JPanel jpMenu = new JPanel();

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(lblFromDesign);
        datePanel.add(dateTimePickerFrom);
        datePanel.add(lblToDesign);
        datePanel.add(dateTimePickerTo);
        datePanel.add(showBtnDesign);
        datePanel.add(collapseBtnDesign);

        JPanel editBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editBtnPanel.add(saveBtnDesign);
        editBtnPanel.add(reportSaveComboBox);
        editBtnPanel.add(openBtnDesign);
        editBtnPanel.add(clearBtnDesign);
        editBtnPanel.add(generateReportBtnDesign);

        jpMenu.setLayout(new GridLayout(1, 2, 10, 10));
        jpMenu.add(datePanel);
        jpMenu.add(editBtnPanel);

        jspMenuDesign.setTopComponent(jpMenu);
        jspMenuDesign.setBottomComponent(new JScrollPane(containerChartCardDesign));

        jspDesign.setLeftComponent(new JScrollPane(containerCardDesign));
        jspDesign.setRightComponent(jspMenuDesign);

        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row().cellXRemainder(jspDesign).fillXY();
        gbl.done();
        return jPanel;
    }


    private JPanel fillReportPanel() {
        JPanel jPanel = new JPanel();
        JPanel jPanelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jPanelBtn.add(saveBtnPDFReport);

        jspReportH.setLeftComponent(savedReportCase.getJScrollPane());
        jspReportH.setRightComponent(jspReportV);

        jspReportV.setTopComponent(jPanelBtn);
        jspReportV.setBottomComponent(scrollPanePDF);

        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row().cellXRemainder(jspReportH).fillXY();
        gbl.done();

        return jPanel;
    }

    public void refresh() {
        jspDesign.setDividerLocation(200);
    }

    public void setSelectedTab(ReportTabbedPane tabbedPane) {
        switch (tabbedPane) {
            case DESIGN: {
                this.setSelectedIndex(0);
                return;
            }
            case REPORT: {
                this.setSelectedIndex(1);
                return;
            }
        }
    }

}
