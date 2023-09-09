package ru.rti.desktop.collector.by;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.state.SqlQueryState;

@Log4j2
public class ByServer implements ByTarget {

  private final ProfileTaskQueryKey profileTaskQueryKey;
  private final QueryInfo queryInfo;
  private final TableInfo tableInfo;
  private final Connection connection;

  private final SqlQueryState sqlQueryState;

  public ByServer(ProfileTaskQueryKey profileTaskQueryKey, QueryInfo queryInfo, TableInfo tableInfo,
      Connection connection, SqlQueryState sqlQueryState) {
    this.profileTaskQueryKey = profileTaskQueryKey;
    this.queryInfo = queryInfo;
    this.tableInfo = tableInfo;
    this.connection = connection;
    this.sqlQueryState = sqlQueryState;
  }

  @Override
  public PreparedStatement getPreparedStatement() throws SQLException {

    PreparedStatement ps;

    String columnTimestamp = getColumnTimestamp(tableInfo);

      String sqlText = queryInfo.getText()
          + " WHERE " + columnTimestamp + " > ? "
          + " ORDER BY " + getColumnTimestamp(tableInfo);

      ps = connection.prepareStatement(sqlText);

      log.info("By server last timestamp :: " + profileTaskQueryKey);
      log.info(toLocalDateTimeOfEpochMilli(sqlQueryState.getLastTimestamp(profileTaskQueryKey)));

      ps.setTimestamp(1, new Timestamp(sqlQueryState.getLastTimestamp(profileTaskQueryKey)));

    return ps;
  }

  private LocalDateTime toLocalDateTimeOfEpochMilli(long ofEpochMilli) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(ofEpochMilli), TimeZone.getDefault().toZoneId());
  }
}
