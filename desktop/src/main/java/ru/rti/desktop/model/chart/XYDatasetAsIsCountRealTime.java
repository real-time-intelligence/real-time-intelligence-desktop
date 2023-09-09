package ru.rti.desktop.model.chart;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

@Log4j2
public class XYDatasetAsIsCountRealTime {

  @Getter
  private final XYDataset xyDatasetAsIs;
  @Getter
  private final XYDataset xyDatasetCount;

  private final TimeSeries seriesAsIs;
  private final TimeSeries seriesCount;

  public XYDatasetAsIsCountRealTime() {
    this.seriesAsIs = new TimeSeries("As is");
    this.seriesCount = new TimeSeries("Count");

    this.xyDatasetAsIs = new TimeSeriesCollection(seriesAsIs);
    this.xyDatasetCount = new TimeSeriesCollection(seriesCount);
  }

  public void addAsIsCountValue(double x, double yAsIs, double yCount) {
    Millisecond millisecond = getMillisecond(x);

    TimeSeriesDataItem timeSeriesDataItem = this.seriesAsIs.getDataItem(millisecond);
    if (Objects.nonNull(timeSeriesDataItem)) {
      TimeSeriesDataItem timeSeriesCount = this.seriesCount.getDataItem(millisecond);

      this.seriesAsIs.addOrUpdate(millisecond, yAsIs);
      this.seriesCount.addOrUpdate(millisecond, timeSeriesCount.getValue().doubleValue() + yCount);

      return;
    }

    this.seriesAsIs.add(millisecond, yAsIs);
    this.seriesCount.add(millisecond, yCount);
  }

  public void deleteAsIsCountValue(int holdRange) {
    int imax = this.seriesAsIs.getItemCount();

    if (imax <= 0) {
      return;
    }

    TimeSeriesDataItem dataItemMax = this.seriesAsIs.getDataItem(this.seriesAsIs.getItemCount() - 1);

    long begin = dataItemMax.getPeriod().getStart().getTime() - ((long) holdRange * 60 * 1000);

    LocalDateTime dateBeginExpected = LocalDateTime.ofInstant(Instant.ofEpochMilli(begin),
        TimeZone.getDefault().toZoneId());

    for (int i=0; i<(imax - 1); i++) {
      try {
        TimeSeriesDataItem dataItemCurrent = this.seriesAsIs.getDataItem(i);
        LocalDateTime dateBeginCurrent = LocalDateTime.ofInstant(Instant.ofEpochMilli(dataItemCurrent.getPeriod().getStart().getTime()),
            TimeZone.getDefault().toZoneId());

        if (dateBeginCurrent.isBefore(dateBeginExpected)) {
          this.seriesAsIs.delete(dataItemCurrent.getPeriod());
          this.seriesCount.delete(dataItemCurrent.getPeriod());
        } else {
          break;
        }
      } catch (Exception e){
        log.catching(e);
      }
    }
  }

  private Millisecond getMillisecond(double x) {
    LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) x),
        TimeZone.getDefault().toZoneId());

    return new Millisecond(dt.getNano() / 1000000, dt.getSecond(), dt.getMinute(),
        dt.getHour(), dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear());
  }

}
