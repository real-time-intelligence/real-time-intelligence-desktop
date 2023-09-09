package ru.rti.desktop.view.pane;

import ru.rti.desktop.model.view.ProcessType;

import javax.swing.*;

public class ChartJTabbedPane extends JTabbedPane {

  public void setSelectedTab(ProcessType tabbedPane) {
    switch (tabbedPane) {
      case REAL_TIME -> this.setSelectedIndex(0);
      case HISTORY -> this.setSelectedIndex(1);
      case SEARCH -> this.setSelectedIndex(2);
    }
  }
}
