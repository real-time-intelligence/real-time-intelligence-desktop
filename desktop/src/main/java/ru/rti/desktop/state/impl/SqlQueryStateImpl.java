package ru.rti.desktop.state.impl;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.state.SqlQueryState;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Singleton
public class SqlQueryStateImpl implements SqlQueryState {

  private final Map<ProfileTaskQueryKey, Long> profileTaskQueryKeyMap;

  @Inject
  public SqlQueryStateImpl() {
    this.profileTaskQueryKeyMap = new ConcurrentHashMap<>();
  }

  @Override
  public void initializeLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey, long value) {
    profileTaskQueryKeyMap.put(profileTaskQueryKey, value);
  }

  @Override
  public void setLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey, long value) {
    profileTaskQueryKeyMap.replace(profileTaskQueryKey, value);
  }

  @Override
  public long getLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey) {
    return profileTaskQueryKeyMap.getOrDefault(profileTaskQueryKey, 0L);
  }

  @Override
  public void clear(ProfileTaskQueryKey profileTaskQueryKey) {
    profileTaskQueryKeyMap.remove(profileTaskQueryKey);
  }
}
