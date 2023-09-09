package ru.rti.desktop.model.config;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.rti.desktop.model.local.LoadDataMode;
import ru.rti.desktop.model.sql.GatherDataSql;

@EqualsAndHashCode(callSuper = true)
@Data
public class Query extends ConfigEntity {
  private String text;
  private String description;

  private GatherDataSql gatherDataSql;
  private LoadDataMode loadDataMode;

  private List<Metric> metricList = Collections.emptyList();
}
