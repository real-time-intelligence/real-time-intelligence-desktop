package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.ProfileTaskQueryKey;

public interface CollectStartStopListener {

  void fireOnStartCollect(ProfileTaskQueryKey profileTaskQueryKey);

  void fireOnStopCollect(ProfileTaskQueryKey profileTaskQueryKey);
}
