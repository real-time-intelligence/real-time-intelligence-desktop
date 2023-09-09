package ru.rti.desktop.state;

import ru.rti.desktop.model.ProfileTaskQueryKey;

public interface SqlQueryState {

  void initializeLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey, long value);

  void setLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey, long value);

  long getLastTimestamp(ProfileTaskQueryKey profileTaskQueryKey);

  void clear(ProfileTaskQueryKey profileTaskQueryKey);
}
