package ru.rti.desktop.router.listener;

import org.fbase.model.profile.CProfile;
import ru.rti.desktop.model.info.QueryInfo;

public interface ShowLocalHistoryListener {

  void fireOnShowHistory(QueryInfo queryInfo, CProfile cProfile, long begin, long end);
}
