package ru.rti.desktop.view.structure.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.view.ToolbarButtonState;
import ru.rti.desktop.model.view.ToolbarAction;
import ru.rti.desktop.router.Router;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.ToolbarListener;
import ru.rti.desktop.state.NavigatorState;
import ru.rti.desktop.view.structure.ToolbarView;

@Log4j2
@Singleton
public class ToolbarPresenter implements ActionListener, ToolbarListener {

  private final ToolbarView toolbarView;
  private final NavigatorState navigatorState;
  private final EventListener eventListener;
  private final ProfileManager profileManager;
  private final Router router;

  @Inject
  public ToolbarPresenter(@Named("toolbarView") ToolbarView toolbarView,
                          @Named("navigatorState") NavigatorState navigatorState,
                          @Named("eventListener") EventListener eventListener,
                          @Named("profileManager") ProfileManager profileManager,
                          @Named("router") Router router) {
    this.toolbarView = toolbarView;
    this.navigatorState = navigatorState;
    this.eventListener = eventListener;
    this.profileManager = profileManager;
    this.router = router;

    this.eventListener.addProfileButtonStateListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(ToolbarAction.CONFIGURATION.name())) {
      router.runConfigDialog(navigatorState.getSelectedProfile());
    }

    if (e.getActionCommand().equals(ToolbarAction.TEMPLATE.name())) {
      router.runTemplateDialog();
    }

    if (e.getActionCommand().equals(ToolbarAction.REPORT.name())) {
      router.runReportDialog();
    }
  }

  @Override
  public void fireToolbarButtonStateChange(ToolbarButtonState toolbarButtonState) {
    toolbarView.setProfileButtonState(toolbarButtonState);
  }
}
