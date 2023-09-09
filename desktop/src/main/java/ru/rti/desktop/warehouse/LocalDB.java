package ru.rti.desktop.warehouse;

import com.sleepycat.je.DatabaseException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import org.fbase.FBase;
import org.fbase.config.FBaseConfig;
import org.fbase.core.FStore;
import org.fbase.exception.BeginEndWrongOrderException;
import org.fbase.exception.EnumByteExceedException;
import org.fbase.exception.GanttColumnNotSupportedException;
import org.fbase.exception.SqlColMetadataException;
import org.fbase.exception.TableNameEmptyException;
import org.fbase.model.output.GanttColumn;
import org.fbase.model.output.StackedColumn;
import org.fbase.model.profile.CProfile;
import org.fbase.model.profile.SProfile;
import org.fbase.model.profile.TProfile;
import org.fbase.sql.BatchResultSet;
import ru.rti.desktop.helper.FilesHelper;
import ru.rti.desktop.warehouse.backend.BerkleyDB;

@Log4j2
@Singleton
public class LocalDB implements FStore {

  private final FilesHelper filesHelper;
  private final FBaseConfig fBaseConfig;
  private final FBase fBase;
  private final FStore fStore;
  private final BerkleyDB berkleyDB;

  @Inject
  public LocalDB(FilesHelper filesHelper) {
    this.filesHelper = filesHelper;
    fBaseConfig = new FBaseConfig().setConfigDirectory(filesHelper.getDatabaseDir()).setBlockSize(16);
    berkleyDB = new BerkleyDB(filesHelper.getDatabaseDir());
    fBase = new FBase(fBaseConfig, berkleyDB.getStore());
    fStore = fBase.getFStore();
  }

  @Override
  public TProfile getTProfile(String tableName) {
    try {
      return fStore.getTProfile(tableName);
    } catch (TableNameEmptyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TProfile loadJdbcTableMetadata(Connection connection, String select, SProfile sProfile) {
    try {
      return fStore.loadJdbcTableMetadata(connection, select, sProfile);
    } catch (SQLException | TableNameEmptyException e) {
      log.catching(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public TProfile loadCsvTableMetadata(String fileName, String csvSplitBy, SProfile sProfile) {
    try {
      return fStore.loadCsvTableMetadata(fileName, csvSplitBy, sProfile);
    } catch (TableNameEmptyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void putDataDirect(String tableName, List<List<Object>> list) {
    try {
      fStore.putDataDirect(tableName, list);
    } catch (Exception e) {
      log.catching(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public long putDataJdbc(String tableName, ResultSet resultSet)
      throws SqlColMetadataException, EnumByteExceedException {
    return fStore.putDataJdbc(tableName, resultSet);
  }

  @Override
  public void putDataJdbcBatch(String tableName, ResultSet resultSet, Integer fBaseBatchSize)
      throws SqlColMetadataException, EnumByteExceedException {
    fStore.putDataJdbcBatch(tableName, resultSet, fBaseBatchSize);
  }

  @Override
  public void putDataCsvBatch(String tableName, String fileName, String csvSplitBy, Integer fBaseBatchSize)
      throws SqlColMetadataException {
    fStore.putDataCsvBatch(tableName, fileName, csvSplitBy, fBaseBatchSize);
  }

  @Override
  public List<StackedColumn> getSColumnListByCProfile(String tableName, CProfile cProfile,
      long begin, long end) throws SqlColMetadataException, BeginEndWrongOrderException {
    return fStore.getSColumnListByCProfile(tableName, cProfile, begin, end);
  }

  @Override
  public List<GanttColumn> getGColumnListTwoLevelGroupBy(String tableName,
      CProfile firstLevelGroupBy, CProfile secondLevelGroupBy, long begin, long end)
      throws SqlColMetadataException, BeginEndWrongOrderException, GanttColumnNotSupportedException {
    return fStore.getGColumnListTwoLevelGroupBy(tableName, firstLevelGroupBy, secondLevelGroupBy, begin, end);
  }

  @Override
  public List<List<Object>> getRawDataByColumn(String tableName, CProfile cProfile, long begin, long end) {
    return fStore.getRawDataByColumn(tableName, cProfile, begin, end);
  }

  @Override
  public List<List<Object>> getRawDataAll(String tableName, long begin, long end) {
    return fStore.getRawDataAll(tableName, begin, end);
  }

  @Override
  public BatchResultSet getBatchResultSet(String tableName, int fetchSize) {
    return fStore.getBatchResultSet(tableName, fetchSize);
  }

  @Override
  public BatchResultSet getBatchResultSet(String tableName, long begin, long end, int fetchSize) {
    return fStore.getBatchResultSet(tableName, begin, end, fetchSize);
  }

  @Override
  public long getLastTimestamp(String tableName, long begin, long end) {
    return fStore.getLastTimestamp(tableName, begin, end);
  }

  @Override
  public void syncBackendDb() {
    if (this.berkleyDB.getStore() != null) {
      this.berkleyDB.getStore().sync();
    }
  }

  @Override
  public void closeBackendDb() {
    if (this.berkleyDB.getStore() != null) {
      try {
        this.berkleyDB.getStore().close();
      } catch (DatabaseException dbe) {
        log.error("Error closing store: " + dbe);
      }
    }
    if (this.berkleyDB.getEnv() != null) {
      try {
        this.berkleyDB.getEnv().close();
      } catch (DatabaseException dbe) {
        log.error("Error closing env: " + dbe);
      }
    }
  }
}