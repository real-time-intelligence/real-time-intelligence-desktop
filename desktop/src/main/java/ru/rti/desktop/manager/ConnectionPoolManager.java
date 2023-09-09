package ru.rti.desktop.manager;

import ru.rti.desktop.model.info.ConnectionInfo;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPoolManager {

  void createDataSource(ConnectionInfo connectionInfo);
  Connection getConnection(ConnectionInfo connectionInfo) throws SQLException;
}
