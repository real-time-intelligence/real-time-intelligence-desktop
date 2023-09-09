package ru.rti.desktop.router.event;

import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.router.listener.*;

public interface EventListener extends ToolbarListener, ConfigListener, TemplateListener, ReportListener,ProgressbarListener,
        WorkspaceListener, ProfileStartStopListener, CollectStartStopListener, ShowLocalHistoryListener,
        AppCacheAddListener, ProfileAddListener {

  void addProfileButtonStateListener(ToolbarListener toolbarListener);
  void addProfileSelectOnNavigator(ToolbarListener toolbarListener);
  void addProfileSelectOnNavigator(WorkspaceListener workspaceListener);
  void addConfigStateListener(ConfigListener configListener);
  void addTemplateStateListener(TemplateListener templateListener);
  void addReportStateListener(ReportListener reportListener);
  void addProgressbarListener(ProgressbarListener progressbarListener);
  void addProfileStartStopListener(ProfileStartStopListener profileStartStopListener);
  void addCollectStartStopListener(ProfileTaskQueryKey profileTaskQueryKey, CollectStartStopListener collectStartStopListener);
  void addShowLocalHistoryListener(ShowLocalHistoryListener showLocalHistoryListener);
  void addAppCacheAddListener(AppCacheAddListener appCacheAddListener);
  void addProfileAddListener(ProfileAddListener profileAddListener);
  <T> void clearListener(Class<T> genericClass);
  void clearListenerByKey(ProfileTaskQueryKey profileTaskQueryKey);
}
