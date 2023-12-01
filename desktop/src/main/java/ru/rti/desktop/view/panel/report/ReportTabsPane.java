package ru.rti.desktop.view.panel.report;

import java.awt.Color;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.helper.ReportHelper;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.model.view.ReportTabbedPane;
import ru.rti.desktop.view.panel.DateTimePicker;

@Data
@EqualsAndHashCode(callSuper = false)
@Singleton
public class ReportTabsPane extends JTabbedPane {

    private final JButton saveBtnPDFReport;
    private final JXTaskPaneContainer containerCardDesign;
    private final JSplitPane jspDesign;
    private final JSplitPane jspReportH;
    private final DateTimePicker dateTimePickerFrom;
    private final DateTimePicker dateTimePickerTo;
    private final JLabel lblFromDesign;
    private final JLabel lblToDesign;
    private final JButton saveBtnDesign;
    private final JButton openBtnDesign;
    private final JButton clearBtnDesign;
    private final JButton showBtnDesign;
    private final JButton delBtnDesign;
    private final JButton delBtnReport;
    private final JCheckBox collapseBtnDesign;
    private final JButton generateReportBtnDesign;
    private final JXTableCase savedReportCase;
    private final ReportHelper reportHelper;
    private final JScrollPane jspCardDesign;
    private final JScrollPane scrollPanePDF;
    private String reportPdfPath;
    private final FilesPanel filesPanel;
    private final JTabbedPane jTabbedPaneChart;
    private final JSplitPane jspOpenDesign;
    private final JXTableCase designReportCase;
    private final JLabel designSaveInfo;
    private boolean showedFlag;

    @Inject
    public ReportTabsPane(@Named("dateTimePickerFrom") DateTimePicker dateTimePickerFrom,
                          @Named("dateTimePickerTo") DateTimePicker dateTimePickerTo,
                          @Named("containerCardDesign") JXTaskPaneContainer containerCardDesign,
                          @Named("savedReportCase") JXTableCase savedReportCase,
                          @Named("designReportCase") JXTableCase designReportCase,
                          ReportHelper reportHelper) {

        this.reportHelper = reportHelper;
        this.reportPdfPath = " ";
        this.showedFlag = false;

        //Components of the design panel
        this.jspDesign = GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 200);
        this.jspDesign.setResizeWeight(0);
        this.jspDesign.setResizeWeight(1);

        this.jspOpenDesign = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 150);

        this.containerCardDesign = containerCardDesign;
        this.jspCardDesign = new JScrollPane();
        GUIHelper.setScrolling(jspCardDesign);

        this.designReportCase = designReportCase;

        SimpleDateFormat format = new SimpleDateFormat(reportHelper.getFormatPattern());
        Date date = new Date();

        this.dateTimePickerFrom = dateTimePickerFrom;
        this.dateTimePickerFrom.setFormats(format);
        this.dateTimePickerFrom.setTimeFormat(format);
        UIManager.put(CalendarHeaderHandler.uiControllerID, SpinningCalendarHeaderHandler.class.getName());
        this.dateTimePickerFrom.getMonthView().setZoomable(true);
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
        this.saveBtnDesign.setToolTipText("Save design");
        this.saveBtnDesign.setMnemonic('S');
        this.saveBtnDesign.setEnabled(false);

        this.openBtnDesign = new JButton("Open");
        this.openBtnDesign.setToolTipText("Open saved design");
        this.openBtnDesign.setMnemonic('O');

        this.clearBtnDesign = new JButton("Clear");
        this.clearBtnDesign.setMnemonic('C');
        this.clearBtnDesign.setEnabled(false);

        this.showBtnDesign = new JButton("Show");
        this.showBtnDesign.setMnemonic('H');

        this.collapseBtnDesign = new JCheckBox("Collapse all");
        this.collapseBtnDesign.setMnemonic('A');
        this.collapseBtnDesign.setVisible(false);

        this.generateReportBtnDesign = new JButton("Report");
        this.generateReportBtnDesign.setMnemonic('T');
        this.generateReportBtnDesign.setToolTipText("Generate report");
        this.generateReportBtnDesign.setEnabled(false);

        this.delBtnDesign = new JButton("Delete");
        this.delBtnDesign.setMnemonic('D');
        this.delBtnDesign.setToolTipText("Delete saved design");
        this.delBtnDesign.setEnabled(false);

        this.filesPanel = new FilesPanel();
        this.jTabbedPaneChart = new JTabbedPane();

        this.designSaveInfo = new JLabel();
        this.designSaveInfo.setForeground(Color.RED);

        //Components of the report panel
        this.jspReportH = GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 200);

        this.saveBtnPDFReport = new JButton("Save");
        this.saveBtnPDFReport.setMnemonic('S');
        this.saveBtnPDFReport.setEnabled(false);
        this.delBtnReport = new JButton("Delete");
        this.delBtnReport.setMnemonic('D');
        this.delBtnReport.setToolTipText("Delete saved report");
        this.delBtnReport.setEnabled(false);

        this.savedReportCase = savedReportCase;
        this.scrollPanePDF = new JScrollPane();
        GUIHelper.setScrolling(scrollPanePDF);

        this.add("Design", fillDesignPanel());
        this.add("Report", fillReportPanel());
    }

    private JPanel fillDesignPanel() {
        JPanel jPanel = new JPanel();
        JPanel datePanel = new JPanel();

        PainlessGridBag gblDP = new PainlessGridBag(datePanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        JPanel designPanelInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        designPanelInfo.add(designSaveInfo);

        gblDP.row().cell(lblFromDesign).cell(dateTimePickerFrom)
                .cell(lblToDesign).cell(dateTimePickerTo)
                .cell(showBtnDesign).cell(saveBtnDesign)
                .cell(delBtnDesign) .cell(clearBtnDesign)
                .cell(generateReportBtnDesign)
                .cell(new JLabel()).fillX()
                .cell(designPanelInfo).fillXY();
        gblDP.done();

        JPanel jpLeft = new JPanel(new VerticalLayout());
        jpLeft.setBackground(Color.WHITE);
        jpLeft.add(collapseBtnDesign);
        jpLeft.add(containerCardDesign);

        jspCardDesign.setViewportView(jpLeft);

        collapseBtnDesign.setBackground(Color.WHITE);

        jspOpenDesign.setTopComponent(designReportCase.getJScrollPane());
        jspOpenDesign.setBottomComponent(jspCardDesign);

        jspDesign.setLeftComponent(jspOpenDesign);
        jspDesign.setRightComponent(jTabbedPaneChart);

        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);

        gbl.row().cell(datePanel).fillX();
        gbl.row().cellXRemainder(jspDesign).fillXY();
        gbl.done();
        return jPanel;
    }


    private JPanel fillReportPanel() {
        JPanel jPanel = new JPanel();
        JPanel jPanelBtn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanelBtn.add(saveBtnPDFReport);
        jPanelBtn.add(delBtnReport);

        jspReportH.setLeftComponent(savedReportCase.getJScrollPane());
        jspReportH.setRightComponent(scrollPanePDF);

        PainlessGridBag gbl = new PainlessGridBag(jPanel, GUIHelper.getPainlessGridbagConfiguration(), false);
        gbl.row().cell(jPanelBtn).fillX();
        gbl.row().cellXRemainder(jspReportH).fillXY();
        gbl.done();

        return jPanel;
    }

    public void refresh() {
        jspDesign.setDividerLocation(200);
    }

    public void setSelectedTab(ReportTabbedPane tabbedPane) {
        switch (tabbedPane) {
            case DESIGN -> this.setSelectedIndex(0);
            case REPORT -> this.setSelectedIndex(1);
        }
    }
}
