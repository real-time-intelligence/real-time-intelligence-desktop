package ru.rti.desktop.model.info.gui;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.rti.desktop.model.view.RangeChartHistory;


/**
 * range - show current saved real time data, in minutes
 */
@Data
@Accessors(chain = true)
@ToString
public class ChartInfo {
  private int id;
  private int range;
  private int pullTimeout;
  private RangeChartHistory rangeHistory;

  private long customBegin;
  private long customEnd;
}
