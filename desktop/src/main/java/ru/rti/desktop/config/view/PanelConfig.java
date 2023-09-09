package ru.rti.desktop.config.view;

import dagger.Binds;
import dagger.Module;
import javax.inject.Named;
import ru.rti.desktop.view.panel.config.connection.ConnectionPanel;
import ru.rti.desktop.view.panel.config.profile.ProfilePanel;
import ru.rti.desktop.view.panel.config.query.QueryPanel;
import ru.rti.desktop.view.panel.config.task.TaskPanel;
import ru.rti.desktop.view.panel.report.ReportTabsPane;
import ru.rti.desktop.view.panel.template.TemplateConnPanel;
import ru.rti.desktop.view.panel.template.TemplateEditPanel;

@Module
public abstract class PanelConfig {

  @Binds
  @Named("profileConfigPanel")
  public abstract ProfilePanel bindProfilePanel(ProfilePanel profilePanel);

  @Binds
  @Named("taskConfigPanel")
  public abstract TaskPanel bindTaskPanel(TaskPanel taskPanel);

  @Binds
  @Named("connectionConfigPanel")
  public abstract ConnectionPanel bindConnectionPanel(ConnectionPanel connectionPanel);

  @Binds
  @Named("queryConfigPanel")
  public abstract QueryPanel bindQueryPanel(QueryPanel queryPanel);

  @Binds
  @Named("templateConnPanel")
  public abstract TemplateConnPanel bindTemplateConnPanel(TemplateConnPanel templateConnPanel);

  @Binds
  @Named("templateEditPanel")
  public abstract TemplateEditPanel bindTemplateEditPanel(TemplateEditPanel templateEditPanel);

  @Binds
  @Named("reportTaskPanel")
  public abstract ReportTabsPane bindReportTabsPane(ReportTabsPane reportTabsPane);
}
