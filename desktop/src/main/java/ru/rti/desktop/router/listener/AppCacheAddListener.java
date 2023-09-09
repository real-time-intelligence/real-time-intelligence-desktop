package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.ProfileTaskQueryKey;

public interface AppCacheAddListener {

  void fireOnAddToAppCache(ProfileTaskQueryKey profileTaskQueryKey);
}
