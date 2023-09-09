package ru.rti.desktop.model.info;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.db.DBType;
import ru.rti.desktop.model.local.LoadDataMode;
import ru.rti.desktop.model.sql.GatherDataSql;

@Data
@Accessors(chain = true)
@ToString
public class QueryInfo {
  private int id;
  private String name;
  private String text;
  private String description;

  private GatherDataSql gatherDataSql;
  private LoadDataMode loadDataMode;

  private List<Metric> metricList = Collections.emptyList();

  private DBType dbType;
  private long deltaLocalServerTime;
}
