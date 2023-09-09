package ru.rti.desktop.view.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.panel.selectionhandler.EntitySelectionManager;
import org.jfree.chart.panel.selectionhandler.MouseClickSelectionHandler;
import org.jfree.chart.panel.selectionhandler.RectangularHeightRegionSelectionHandler;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.item.IRSUtilities;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer3;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.IDetailPanel;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.extension.DatasetIterator;
import org.jfree.data.extension.DatasetSelectionExtension;
import org.jfree.data.extension.impl.DatasetExtensionManager;
import org.jfree.data.extension.impl.XYCursor;
import org.jfree.data.extension.impl.XYDatasetSelectionExtension;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.SelectionChangeEvent;
import org.jfree.data.general.SelectionChangeListener;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import ru.rti.desktop.helper.ColorHelper;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;

@Log4j2
public class StackedChart implements SelectionChangeListener<XYCursor>, DynamicChart, DetailChart {
    private ChartPanel chartPanel;
    private JFreeChart jFreeChart;
    private XYPlot xyPlot;
    private DateAxis dateAxis;

    private CategoryTableXYDatasetRealTime categoryTableXYDataset;
    private DatasetSelectionExtension<XYCursor> datasetExtension;
    private StackedXYAreaRenderer3 stackedXYAreaRenderer3;

    private BlockContainer blockContainerParent;
    private BlockContainer legendItemContainer;
    private LegendTitle legendTitle;

    @Getter @Setter
    private int legendFontSize;

    private RectangularHeightRegionSelectionHandler selectionHandler;

    private Map<String, Color> colorLinkedHashMap;
    private AtomicInteger counter;

    public StackedChart(ChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        this.jFreeChart = this.chartPanel.getChart();
        this.xyPlot = (XYPlot) this.jFreeChart.getPlot();
        this.dateAxis = (DateAxis) this.xyPlot.getDomainAxis();
        this.categoryTableXYDataset = (CategoryTableXYDatasetRealTime) this.xyPlot.getDataset();
    }

    @Override
    public void initialize(){
        this.counter = new AtomicInteger(0);
        this.colorLinkedHashMap = new LinkedHashMap<>();

        this.datasetExtension = new XYDatasetSelectionExtension(this.categoryTableXYDataset);
        this.datasetExtension.addChangeListener(this);

        this.setStackedXYAreaRenderer3(this.datasetExtension);

        this.xyPlot.setRenderer(this.getStackedXYAreaRenderer3());
        this.xyPlot.getRangeAxis().setLowerBound(0.0);
        this.xyPlot.getRangeAxis().setAutoRange(true);

        this.dateAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));

        this.selectionHandler = new RectangularHeightRegionSelectionHandler();
        this.chartPanel.addMouseHandler(selectionHandler);
        this.chartPanel.addMouseHandler(new MouseClickSelectionHandler());
        this.chartPanel.removeMouseHandler(this.chartPanel.getZoomHandler());

        DatasetExtensionManager dExManager = new DatasetExtensionManager();
        dExManager.registerDatasetExtension(this.datasetExtension);
        this.chartPanel.setSelectionManager(new EntitySelectionManager(this.chartPanel,
                new Dataset[] { this.categoryTableXYDataset}, dExManager));
        this.setLegendTitle();
        this.jFreeChart.addSubtitle(this.legendTitle);
        this.chartPanel.setRangeZoomable(false);
    }

    @Override
    public ChartPanel getChartPanel() { return this.chartPanel; }

    @Override
    public Map<String, Color> getSeriesColorMap() {
        return colorLinkedHashMap;
    }

    @Override
    public void setChartTitle(String titleText){
        this.jFreeChart.setTitle(titleText);
    }

    @Override
    public void setSeriesPaintDynamic(String seriesName){

        if (this.colorLinkedHashMap.entrySet().stream().noneMatch(e -> e.getKey().equalsIgnoreCase(seriesName))){
            try {
                int cnt = counter.getAndIncrement();
                Color color = ColorHelper.getColor(seriesName);
                this.stackedXYAreaRenderer3.setSeriesPaint(cnt, color);
                this.categoryTableXYDataset.saveSeriesValues(cnt, seriesName);
                this.colorLinkedHashMap.put(seriesName, color);
            } catch (Exception ee){
                log.error(ee.toString());
                ee.printStackTrace();
            }
        }
    }

    @Override
    public void setDateAxisWeekAndMore(){
        PeriodAxis domainAxis = new PeriodAxis(" ");
        domainAxis.setTimeZone(TimeZone.getDefault());

        domainAxis.setMajorTickTimePeriodClass(Day.class);
        PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];
        info[0] = new PeriodAxisLabelInfo(Day.class,
                new SimpleDateFormat("d"), new RectangleInsets(2, 2, 2, 2),
                new Font("SansSerif", Font.BOLD, 8), Color.blue, false,
                new BasicStroke(0.0f), Color.lightGray);
        info[1] = new PeriodAxisLabelInfo(Month.class,
                new SimpleDateFormat("MMM"));
        domainAxis.setLabelInfo(info);

        this.xyPlot.setDomainAxis(domainAxis);
    }

    @Override
    public void addSeriesValue(double x, double y, String seriesName){
        this.categoryTableXYDataset.addSeriesValue(x, y, seriesName);
    }

    @Override
    public void deleteSeriesValue(double x, String seriesName){
        this.categoryTableXYDataset.remove(x, seriesName);
    }

    @Override
    public void deleteAllSeriesData(int holdRange) {
        if (holdRange == 0) {
            this.categoryTableXYDataset.clear();
        }
        this.categoryTableXYDataset.deleteValuesFromDataset(holdRange);
    }

    @Override
    public double getEndXValue() {
        double endXValue;

        try {
            endXValue = this.categoryTableXYDataset.getEndXValue(0, categoryTableXYDataset.getItemCount() - 1);
        } catch (Exception e) {
            endXValue = 0D;
        }

        return endXValue;
    }

    private void setLegendTitle() {
        this.legendTitle = new LegendTitle(this.jFreeChart.getPlot());

        this.blockContainerParent = new BlockContainer(new BorderArrangement());
        this.blockContainerParent.setFrame(new BlockBorder(1.0, 1.0, 1.0, 1.0));

        this.legendItemContainer = this.legendTitle.getItemContainer();
        this.legendItemContainer.setPadding(2, 10, 5, 2);

        this.blockContainerParent.add(this.legendItemContainer);
        this.legendTitle.setWrapper(this.blockContainerParent);

        this.legendTitle.setItemFont(new Font(LegendTitle.DEFAULT_ITEM_FONT.getFontName(),
                LegendTitle.DEFAULT_ITEM_FONT.getStyle(), this.getLegendFontSize()));

        this.legendTitle.setPosition(RectangleEdge.RIGHT);
        this.legendTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        this.legendTitle.setSortOrder(SortOrder.DESCENDING);
    }

    private StackedXYAreaRenderer3 getStackedXYAreaRenderer3() {
        return stackedXYAreaRenderer3;
    }

    private void setStackedXYAreaRenderer3(DatasetSelectionExtension<XYCursor> datasetExtension) {

        StandardXYToolTipGenerator standardXYToolTipGenerator = new StandardXYToolTipGenerator
                ("{0} ({1}, {2})",
                        new SimpleDateFormat("HH:mm"),
                        new DecimalFormat("0.0"));
        this.stackedXYAreaRenderer3 = new StackedXYAreaRenderer3(standardXYToolTipGenerator,null);
        this.stackedXYAreaRenderer3.setRoundXCoordinates(true);

        this.xyPlot.setDomainPannable(true);
        this.xyPlot.setRangePannable(true);
        this.xyPlot.setDomainCrosshairVisible(true);
        this.xyPlot.setRangeCrosshairVisible(true);
        datasetExtension.addChangeListener(this.xyPlot);

        IRSUtilities.setSelectedItemFillPaint(this.getStackedXYAreaRenderer3(), datasetExtension, Color.black);
    }

    @Override
    public void addChartListenerReleaseMouse(IDetailPanel l){
        chartPanel.addListenerReleaseMouse(l);
    }

    @Override
    public void removeChartListenerReleaseMouse(IDetailPanel l){
        chartPanel.removeListenerReleaseMouse(l);
    }

    @Override
    public void selectionChanged(SelectionChangeEvent<XYCursor> event) {
        XYDatasetSelectionExtension ext = (XYDatasetSelectionExtension)
                event.getSelectionExtension();
        DatasetIterator<XYCursor> iter = ext.getSelectionIterator(true);
    }

}
