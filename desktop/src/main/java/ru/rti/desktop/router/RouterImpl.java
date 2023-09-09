package ru.rti.desktop.router;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.view.*;
import ru.rti.desktop.router.event.EventListener;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;

@Log4j2
@Singleton
public class RouterImpl implements Router {

  private final ScheduledExecutorService executorService;
  private final EventListener eventListener;

  @Inject
  public RouterImpl(@Named("executorService") ScheduledExecutorService executorService,
      @Named("eventListener") EventListener eventListener) {

    this.executorService = executorService;
    this.eventListener = eventListener;
  }

  @Override
  public void runConfigDialog(int profileId) {
    log.info("Run configuration dialog..");

    executorService.submit(() -> {
      eventListener.fireToolbarButtonStateChange(ToolbarButtonState.DISABLE);
      eventListener.fireProgressbarVisible(ProgressbarState.SHOW);

      try {
        eventListener.fireShowConfig(ConfigState.SHOW);
      } finally {
        eventListener.fireProgressbarVisible(ProgressbarState.HIDE);
      }

      eventListener.fireToolbarButtonStateChange(ToolbarButtonState.ENABLE);
    });
  }

  @Override
  public void runTemplateDialog() {
    log.info("Run template dialog..");
    eventListener.fireToolbarButtonStateChange(ToolbarButtonState.DISABLE);
    eventListener.fireProgressbarVisible(ProgressbarState.SHOW);

    try {
      eventListener.fireShowTemplate(TemplateState.SHOW);
    } finally {
      eventListener.fireProgressbarVisible(ProgressbarState.HIDE);
    }

    eventListener.fireToolbarButtonStateChange(ToolbarButtonState.ENABLE);
  }

  @Override
  public void runReportDialog() {
    log.info("Run report dialog..");
    executorService.submit(() -> {
      eventListener.fireToolbarButtonStateChange(ToolbarButtonState.DISABLE);
      eventListener.fireProgressbarVisible(ProgressbarState.SHOW);

      try {
        eventListener.fireShowReport(ReportState.SHOW);
      } finally {
        eventListener.fireProgressbarVisible(ProgressbarState.HIDE);
      }
      eventListener.fireToolbarButtonStateChange(ToolbarButtonState.ENABLE);
    });
  }

  @Override
  public void fireOnSelectProfileOnNavigator(int profileId) {
    eventListener.fireOnSelectProfileOnNavigator(profileId);
  }
}
