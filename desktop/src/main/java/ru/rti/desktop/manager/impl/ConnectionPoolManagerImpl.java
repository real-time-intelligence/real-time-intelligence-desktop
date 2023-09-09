package ru.rti.desktop.manager.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.dbcp2.BasicDataSource;
import ru.rti.desktop.exception.TimeoutConnectionException;
import ru.rti.desktop.manager.ConnectionPoolManager;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.security.EncryptDecrypt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Log4j2
@Singleton
public class ConnectionPoolManagerImpl implements ConnectionPoolManager {

  private final EncryptDecrypt encryptDecrypt;

  private final Map<Integer, BasicDataSource> dataSourceMap = new HashMap<>();
  private final Map<Integer, List<Connection>> connectionMap = new HashMap<>();

  @Inject
  public ConnectionPoolManagerImpl(EncryptDecrypt encryptDecrypt) {
    this.encryptDecrypt = encryptDecrypt;
  }

  @Override
  public void createDataSource(ConnectionInfo connectionInfo) {
    if (!dataSourceMap.containsKey(connectionInfo.getId())) {
      dataSourceMap.put(connectionInfo.getId(), getDatasource(connectionInfo));
    }
  }

  private BasicDataSource getDatasource(ConnectionInfo connectionInfo) {
    BasicDataSource basicDataSource = null;
    try {
      basicDataSource = new BasicDataSource();
      basicDataSource.setDriverClassLoader(getClassLoader(connectionInfo.getJar()));
      basicDataSource.setDriverClassName(connectionInfo.getDriver());
      basicDataSource.setUrl(connectionInfo.getUrl());
      basicDataSource.setUsername(connectionInfo.getUserName());
      basicDataSource.setPassword(encryptDecrypt.decrypt(connectionInfo.getPassword()));
      basicDataSource.setInitialSize(2);
      basicDataSource.setMaxTotal(3);
      basicDataSource.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(5));

    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
             MalformedURLException e) {
      log.error(e.toString());
    }

    return basicDataSource;
  }

  private ClassLoader getClassLoader(String jar) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, MalformedURLException {
    URL url = new File(jar.trim()).toURI().toURL();
    URLClassLoader ucl = new URLClassLoader(new URL[]{url});
    return ucl;
  }

  @Override
  public Connection getConnection(ConnectionInfo connectionInfo) {
      if (connectionMap.get(connectionInfo.getId()) == null) {
        Connection connection = futureExecutionWithTimeout(5, connectionInfo);
        connectionMap.put(connectionInfo.getId(), List.of(connection));
      } else {
        if (connectionMap.get(connectionInfo.getId()).isEmpty()) {
          Connection connection = futureExecutionWithTimeout(5, connectionInfo);
          connectionMap.put(connectionInfo.getId(), List.of(connection));
        }
      }
    return connectionMap.get(connectionInfo.getId()).stream().findAny().get();
  }

  private Connection futureExecutionWithTimeout(int timeoutSeconds, ConnectionInfo connectionInfo) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Callable<Connection> task = () -> dataSourceMap.get(connectionInfo.getId()).getConnection();

    Future<Connection> future = executor.submit(task);
    try {
      return future.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new TimeoutConnectionException("Timeout " + timeoutSeconds + " sec. is exceed to get data from: " + connectionInfo);
    } catch (Exception e) {
      log.catching(e);
      throw new RuntimeException(e);
    } finally {
      executor.shutdownNow();
    }
  }
}
