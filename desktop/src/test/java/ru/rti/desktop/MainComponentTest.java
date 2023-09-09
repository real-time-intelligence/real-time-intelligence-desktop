package ru.rti.desktop;

import dagger.Component;
import javax.inject.Singleton;

import ru.rti.desktop.config.*;
import ru.rti.desktop.config.view.PanelConfig;
import ru.rti.desktop.config.presenter.PresenterConfig;
import ru.rti.desktop.config.view.BaseFrameConfig;
import ru.rti.desktop.config.view.NavigatorConfig;
import ru.rti.desktop.config.view.ConfigurationConfig;
import ru.rti.desktop.config.view.ToolbarConfig;
import ru.rti.desktop.config.view.ViewConfig;

@Component(modules = {
    CollectorConfig.class,
    ExecutorConfig.class,
    BaseFrameConfig.class,
    ViewConfig.class,
    PanelConfig.class,
    NavigatorConfig.class,
    ToolbarConfig.class,
    PresenterConfig.class,
    HandlerConfig.class,
    ConfigurationConfig.class,
    RouterConfig.class,
    StateConfig.class,
    ManagerTestConfig.class,
    FileConfig.class,
    LocalDBConfig.class,
    CacheConfig.class,
    SecurityConfig.class
})
@Singleton
public interface MainComponentTest extends MainComponent {
  void inject(HandlerMock handlerMock);
}
