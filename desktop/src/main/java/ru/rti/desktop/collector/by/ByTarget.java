package ru.rti.desktop.collector.by;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.model.info.TableInfo;

public interface ByTarget {

  PreparedStatement getPreparedStatement() throws SQLException;

  default String getColumnTimestamp(TableInfo tableInfo) {
    return tableInfo.getCProfiles()
        .stream()
        .filter(f -> f.getCsType().isTimeStamp())
        .findAny()
        .orElseThrow(() -> new NotFoundException("Not found column timestamp: " + tableInfo.getTableName()))
        .getColName();
  }
}
