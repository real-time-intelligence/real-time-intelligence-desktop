package ru.rti.desktop.model.config;

import static ru.rti.desktop.model.function.ChartType.LINEAR;
import static ru.rti.desktop.model.function.ChartType.STACKED;
import static ru.rti.desktop.model.function.MetricFunction.AVERAGE;
import static ru.rti.desktop.model.function.MetricFunction.SUM;
import static ru.rti.desktop.model.function.MetricFunction.COUNT;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.model.function.ChartType;
import ru.rti.desktop.model.function.MetricFunction;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metric {
  private int id;
  private String name;
  private Boolean isDefault;

  private CProfile xAxis; // x-axis
  private CProfile yAxis; // y-axis
  private CProfile group; // group
  private MetricFunction metricFunction; // AsIs, SUM, COUNT, AVG etc.
  private ChartType chartType; // linear, stacked

  private List<CProfile> columnGanttList;

  public boolean isStackedYAxisSameCount() {
    return chartType.equals(STACKED)
        & yAxis.equals(group)
        & metricFunction.equals(COUNT);
  }

  public boolean isLinearYAxisSameSum() {
    return chartType.equals(LINEAR)
        & yAxis.equals(group)
        & metricFunction.equals(SUM);
  }

  public boolean isLinearYAxisAvg() {
    return chartType.equals(LINEAR)
        & yAxis.equals(group)
        & metricFunction.equals(AVERAGE);
  }
}
