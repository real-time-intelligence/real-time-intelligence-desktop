package ru.rti.desktop.config;

import dagger.Component;
import ru.rti.desktop.config.view.PanelConfig;
import ru.rti.desktop.config.presenter.PresenterConfig;
import ru.rti.desktop.config.prototype.profile.WorkspaceProfileComponent;
import ru.rti.desktop.config.prototype.profile.WorkspaceProfileModule;
import ru.rti.desktop.config.view.*;
import ru.rti.desktop.view.BaseFrame;

import javax.inject.Singleton;

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
        ManagerConfig.class,
        FileConfig.class,
        LocalDBConfig.class,
        CacheConfig.class,
        SecurityConfig.class
})
@Singleton
public interface MainComponent {
    BaseFrame createBaseFrame();

    WorkspaceProfileComponent initProfile(WorkspaceProfileModule workspaceProfileModule);
}