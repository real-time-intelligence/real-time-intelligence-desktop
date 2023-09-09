package ru.rti.desktop.config.view;

import dagger.Module;
import dagger.Provides;
import java.awt.Dimension;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JButton;

import static ru.rti.desktop.model.view.ToolbarAction.*;

@Module
public class ToolbarConfig {

  @Provides
  @Singleton
  @Named("toolbarConfigButton")
  public JButton getConfigButton() {
    JButton jButton = new JButton();
    jButton.setActionCommand(CONFIGURATION.name());
    jButton.setMnemonic('C');
    jButton.setName("Configuration123");
    jButton.setText("Configuration");
    jButton.setPreferredSize(new Dimension(120, 30));
    return jButton;
  }

  @Provides
  @Singleton
  @Named("toolbarTemplateButton")
  public JButton getTemplateButton() {
    JButton jButton = new JButton();
    jButton.setActionCommand(TEMPLATE.name());
    jButton.setMnemonic('T');
    jButton.setText("Templates");
    jButton.setPreferredSize(new Dimension(120, 30));
    return jButton;
  }

  @Provides
  @Singleton
  @Named("toolbarReportButton")
  public JButton getReportButton() {
    JButton jButton = new JButton();
    jButton.setActionCommand(REPORT.name());
    jButton.setMnemonic('R');
    jButton.setText("Report");
    jButton.setPreferredSize(new Dimension(120, 30));
    return jButton;
  }
}
