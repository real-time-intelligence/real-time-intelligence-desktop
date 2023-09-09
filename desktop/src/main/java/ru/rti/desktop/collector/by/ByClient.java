package ru.rti.desktop.collector.by;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.QueryInfo;

@Log4j2
public class ByClient implements ByTarget {

  private final ProfileTaskQueryKey profileTaskQueryKey;
  private final QueryInfo queryInfo;
  private final Connection connection;

  public ByClient(ProfileTaskQueryKey profileTaskQueryKey, QueryInfo queryInfo, Connection connection) {
    this.profileTaskQueryKey = profileTaskQueryKey;
    this.queryInfo = queryInfo;
    this.connection = connection;
  }

  @Override
  public PreparedStatement getPreparedStatement() throws SQLException {
    return connection.prepareStatement(queryInfo.getText());
  }
}
