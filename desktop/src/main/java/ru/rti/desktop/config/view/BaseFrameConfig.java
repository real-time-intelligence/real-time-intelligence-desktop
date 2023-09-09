package ru.rti.desktop.config.view;

import dagger.Module;
import dagger.Provides;
import ru.rti.desktop.helper.GUIHelper;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;

@Module
public class BaseFrameConfig {

  @Provides
  @Singleton
  @Named("splitBaseFrame")
  public JSplitPane getJSplitPane() {
    return GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 170);
  }

  @Provides
  @Singleton
  @Named("splitProfileListButtonsAndStatus")
  public JSplitPane getJSplitPaneProfileListButtonsAndStatus() {
    return GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 150);
  }

  @Provides
  @Singleton
  @Named("jPanelProfileStatus")
  public JPanel getJPanelProfileStatus() {
    return new JPanel(new BorderLayout());
  }
}
