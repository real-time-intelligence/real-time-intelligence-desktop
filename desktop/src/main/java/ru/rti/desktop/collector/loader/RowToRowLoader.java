package ru.rti.desktop.collector.loader;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import org.fbase.model.profile.TProfile;
import ru.rti.desktop.collector.by.ByTarget;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.state.SqlQueryState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class RowToRowLoader implements DataLoader, JdbcLoader {

  private final ByTarget byTarget;

  private final Connection connection;

  private final ProfileTaskQueryKey profileTaskQueryKey;
  private final TaskInfo taskInfo;
  private final QueryInfo queryInfo;
  private final TableInfo tableInfo;
  private final SqlQueryState sqlQueryState;
  private final FStore fStore;

  private final int fBaseBatchSize = 30000;
  private final int resultSetFetchSize = 10000;

  public RowToRowLoader(ByTarget byTarget, Connection connection, ProfileTaskQueryKey profileTaskQueryKey,
      TaskInfo taskInfo, QueryInfo queryInfo, TableInfo tableInfo, SqlQueryState sqlQueryState, FStore fStore) {
    this.byTarget = byTarget;
    this.connection = connection;
    this.profileTaskQueryKey = profileTaskQueryKey;
    this.taskInfo = taskInfo;
    this.queryInfo = queryInfo;
    this.tableInfo = tableInfo;
    this.sqlQueryState = sqlQueryState;
    this.fStore = fStore;
  }

  @Override
  public void initLastTimestamp() {
    if (sqlQueryState.getLastTimestamp(profileTaskQueryKey) == 0) {
      sqlQueryState.setLastTimestamp(profileTaskQueryKey, getSampleTimeValue(connection, taskInfo, queryInfo));
    }
  }

  @Override
  public void loadDataDirect() {
    try {
      AtomicLong currValue = new AtomicLong();

      TProfile tProfile = fStore.loadJdbcTableMetadata(connection, queryInfo.getText(), tableInfo.getSProfile());

      List<CProfile> cProfiles = tProfile.getCProfiles();

      List<List<Object>> listsColStore = new ArrayList<>();
      cProfiles.forEach(v -> listsColStore.add(v.getColId(), new ArrayList<>()));

      PreparedStatement ps = byTarget.getPreparedStatement();
      ResultSet r = ps.executeQuery();
      r.setFetchSize(resultSetFetchSize);

      final boolean[] isTheSameKey = {false};
      final long[] previousValue = {0};
      AtomicInteger cnt = new AtomicInteger(0);

      while (r.next()) {
        cProfiles.forEach(v -> {
          addToList(listsColStore, v, r);
          if (v.getCsType().isTimeStamp()) {
            try {
              Timestamp dt = (Timestamp) r.getObject(v.getColIdSql());
              if (previousValue[0] == dt.getTime()) {
                isTheSameKey[0] = true;
              } else {
                previousValue[0] = dt.getTime();
              }
              currValue.set(dt.getTime());
            } catch (SQLException sqlException) {
              log.catching(sqlException);
            }
          }
        });

        if (cnt.incrementAndGet() >= fBaseBatchSize) {
          log.info("Flush.. " + previousValue[0] + ":" + isTheSameKey[0]);
          if (isTheSameKey[0]) {
            try {
              fStore.putDataDirect(tProfile.getTableName(), listsColStore);
            } catch (Exception e) {
              log.catching(e);
            }
            listsColStore.clear();
            cProfiles.forEach(v -> listsColStore.add(v.getColId(), new ArrayList<>()));
            cnt.set(0);
          } else {
            isTheSameKey[0] = false;
          }
          log.info("Flush ended.. ");
        }
      }

      if (cnt.get() != 0) {
        try {
          fStore.putDataDirect(tProfile.getTableName(), listsColStore);
        } catch (Exception e) {
          log.catching(e);
        }
      }

      sqlQueryState.setLastTimestamp(profileTaskQueryKey, currValue.longValue());

      r.close();
      ps.close();
    } catch (Exception e) {
      log.catching(e);
    }
  }

  @Override
  public void loadDataJdbc() {
    try {
      PreparedStatement ps = byTarget.getPreparedStatement();
      ps.setFetchSize(resultSetFetchSize);

      ResultSet r = ps.executeQuery();

      long lastTimeStamp = fStore.putDataJdbc(tableInfo.getTableName(), r);

      if (lastTimeStamp != -1) {
        sqlQueryState.setLastTimestamp(profileTaskQueryKey, lastTimeStamp);
      } else {
        sqlQueryState.setLastTimestamp(profileTaskQueryKey,
            sqlQueryState.getLastTimestamp(profileTaskQueryKey) + (taskInfo.getPullTimeout() * 1000L));
      }

      r.close();
      ps.close();
    } catch (Exception e) {
      log.catching(e);
    }
  }

  private void addToList(List<List<Object>> lists, CProfile v, ResultSet r) {
    try {
      lists.get(v.getColId()).add(r.getObject(v.getColIdSql()));
    } catch (SQLException e) {
      log.catching(e);
    }
  }

  private long getSampleTimeValue(Connection connection, TaskInfo taskInfo, QueryInfo queryInfo) {
    long end = getSysdate(queryInfo.getDbType().getQuery(), connection, log);

    return end - (taskInfo.getPullTimeout() * 1000L);
  }

}
