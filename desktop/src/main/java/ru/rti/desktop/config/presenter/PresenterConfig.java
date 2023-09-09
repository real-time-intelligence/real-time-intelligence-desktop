package ru.rti.desktop.config.presenter;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.view.structure.navigator.NavigatorPresenter;
import ru.rti.desktop.view.structure.config.ConfigPresenter;
import ru.rti.desktop.view.structure.progressbar.ProgressbarPresenter;
import ru.rti.desktop.view.structure.report.ReportPresenter;
import ru.rti.desktop.view.structure.toolbar.ToolbarPresenter;
import ru.rti.desktop.view.structure.workspace.WorkspacePresenter;

import javax.inject.Named;

@Module
public abstract class PresenterConfig {

  @Binds
  @Named("workspacePresenter")
  public abstract WorkspacePresenter bindWorkspacePresenter(WorkspacePresenter workspacePresenter);

  @Binds
  @Named("toolbarPresenter")
  public abstract ToolbarPresenter bindToolbarPresenter(ToolbarPresenter toolbarPresenter);

  @Binds
  @Named("progressbarPresenter")
  public abstract ProgressbarPresenter bindProgressbarPresenter(ProgressbarPresenter progressbarPresenter);

  @Binds
  @Named("navigatorPresenter")
  public abstract NavigatorPresenter bindNavigatorPresenter(NavigatorPresenter navigatorPresenter);

  @Binds
  @Named("profilePresenter")
  public abstract ConfigPresenter bindProfilePresenter(ConfigPresenter configPresenter);

  @Binds
  @Named("reportPresenter")
  public abstract ReportPresenter bindReportPresenter(ReportPresenter reportPresenter);
}
