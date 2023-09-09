package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.router.Router;
import ru.rti.desktop.router.RouterImpl;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.event.EventListenerImpl;

import javax.inject.Named;

@Module
public abstract class RouterConfig {

  @Binds
  @Named("router")
  public abstract Router bindRouter(RouterImpl router);

  @Binds
  @Named("eventListener")
  public abstract EventListener bindEventListener(EventListenerImpl eventListener);
}
