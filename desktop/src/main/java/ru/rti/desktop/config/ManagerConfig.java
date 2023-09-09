package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import javax.inject.Named;

import ru.rti.desktop.manager.*;
import ru.rti.desktop.manager.impl.*;

@Module
public abstract class ManagerConfig {

  @Binds
  @Named("profileManager")
  public abstract ProfileManager bindProfileManager(ProfileManagerImpl configurationManager);

  @Binds
  @Named("configurationManager")
  public abstract ConfigurationManager bindConfigurationManager(ConfigurationManagerImpl configurationManager);

  @Binds
  @Named("connectionPoolManager")
  public abstract ConnectionPoolManager bindConnectionPoolManager(ConnectionPoolManagerImpl connectionPoolManager);

  @Binds
  @Named("templateManager")
  public abstract TemplateManager bindTemplateManager(TemplateManagerImpl templateManager);

  @Binds
  @Named("reportManager")
  public abstract ReportManager bindReportManager(ReportManagerImpl reportManager);
}
