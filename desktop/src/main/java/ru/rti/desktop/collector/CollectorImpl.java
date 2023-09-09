package ru.rti.desktop.collector;

import java.sql.Connection;
import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.TProfile;
import ru.rti.desktop.collector.loader.DataLoader;
import ru.rti.desktop.collector.loader.JdbcLoader;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.router.event.EventListener;

@Log4j2
@Singleton
public class CollectorImpl implements Collector, JdbcLoader {

  private final FStore fStore;
  private final EventListener eventListener;

  @Inject
  public CollectorImpl(@Named("localDB") FStore fStore,
                       @Named("eventListener") EventListener eventListener) {
    this.fStore = fStore;
    this.eventListener = eventListener;
  }

  @Override
  public void fillMetadata(QueryInfo queryInfo, TableInfo tableInfo, Connection connection) {
    try {
      TProfile tProfile = fStore.loadJdbcTableMetadata(connection, queryInfo.getText(), tableInfo.getSProfile());

      tableInfo.setTableType(tProfile.getTableType());
      tableInfo.setIndexType(tProfile.getIndexType());
      tableInfo.setCompression(tProfile.getCompression());
      tableInfo.setCProfiles(tProfile.getCProfiles());

      long localDateTime = System.currentTimeMillis();
      long serverDateTime = getSysdate(queryInfo.getDbType().getQuery(), connection, log);

      queryInfo.setDeltaLocalServerTime(localDateTime - serverDateTime);
    } catch (Exception e) {
      log.catching(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void collect(ProfileInfo profileInfo, TaskInfo taskInfo, HashMap<Integer, DataLoader> loaderHashMap) {
    log.info("Collect started..");

    taskInfo.getQueryInfoList().forEach(queryId -> {
      try {
        ProfileTaskQueryKey profileTaskQueryKey =
            new ProfileTaskQueryKey(profileInfo.getId(), taskInfo.getId(), queryId);

        loaderHashMap.get(queryId).initLastTimestamp();
        eventListener.fireOnStartCollect(profileTaskQueryKey);
        loaderHashMap.get(queryId).loadDataJdbc();
        eventListener.fireOnStopCollect(profileTaskQueryKey);
      } catch (Exception e) {
        log.catching(e);
        throw new RuntimeException(e);
      }
    });

    log.info("Collect ended..");
  }

}