package ru.rti.desktop.view.structure.progressbar;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.view.ProgressbarState;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.ProgressbarListener;
import ru.rti.desktop.view.structure.ProgressbarView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Log4j2
@Singleton
public class ProgressbarPresenter implements ProgressbarListener {
  private final ProgressbarView progressbarView;
  private final EventListener eventListener;

  @Inject
  public ProgressbarPresenter(@Named("progressbarView") ProgressbarView progressbarView,
      @Named("eventListener") EventListener eventListener) {
    this.progressbarView = progressbarView;
    this.eventListener = eventListener;

    this.eventListener.addProgressbarListener(this);
  }

  @Override
  public void fireProgressbarVisible(ProgressbarState progressbarState) {
    this.progressbarView.setProgressbarVisible(progressbarState);
  }
}
