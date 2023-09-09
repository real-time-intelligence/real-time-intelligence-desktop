package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.view.ReportState;

public interface ReportListener {

  void fireShowReport(ReportState reportState);
}
