package ru.rti.desktop.config.view;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.view.structure.*;
import ru.rti.desktop.view.structure.navigator.NavigatorViewImpl;
import ru.rti.desktop.view.structure.config.ConfigViewImpl;
import ru.rti.desktop.view.structure.progressbar.ProgressbarViewImpl;
import ru.rti.desktop.view.structure.report.ReportViewImpl;
import ru.rti.desktop.view.structure.template.TemplateViewImpl;
import ru.rti.desktop.view.structure.toolbar.ToolbarViewImpl;
import ru.rti.desktop.view.structure.workspace.WorkspaceViewImpl;

import javax.inject.Named;

@Module
public abstract class ViewConfig {

  @Binds
  @Named("navigatorView")
  public abstract NavigatorView bindNavigator(NavigatorViewImpl navigatorView);

  @Binds
  @Named("workspaceView")
  public abstract WorkspaceView bindWorkspace(WorkspaceViewImpl workspaceView);

  @Binds
  @Named("toolbarView")
  public abstract ToolbarView bindToolbar(ToolbarViewImpl toolbarView);

  @Binds
  @Named("configView")
  public abstract ConfigView bindConfig(ConfigViewImpl profileView);

  @Binds
  @Named("templateView")
  public abstract TemplateView bindTemplate(TemplateViewImpl templateView);

  @Binds
  @Named("progressbarView")
  public abstract ProgressbarView bindProgressbar(ProgressbarViewImpl progressbarView);

  @Binds
  @Named("reportView")
  public abstract ReportView bindReport(ReportViewImpl reportView);
}
